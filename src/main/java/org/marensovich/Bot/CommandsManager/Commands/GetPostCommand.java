package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Data.PolicePost;
import org.marensovich.Bot.Maps.MapUtils.Distance;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GetPostCommand implements Command {

    private enum State {
        AWAITING_LOCATION,
        SHOWING_RESULTS
    }

    public static class UserState {
        State currentState;
        Location userLocation;
        int currentPage;
        int lastListPage;

        UserState() {
            this.currentState = State.AWAITING_LOCATION;
            this.currentPage = 0;
            this.lastListPage = 0;
        }


        public boolean isAwaitingLocation() {
            return currentState == State.AWAITING_LOCATION;
        }
    }

    private static final int PAGE_SIZE = 5;
    public static final String CALLBACK_PREFIX = "post_";
    public static final String CALLBACK_NEXT_PAGE = CALLBACK_PREFIX + "next";
    public static final String CALLBACK_PREV_PAGE = CALLBACK_PREFIX + "prev";
    public static final String CALLBACK_POST_DETAIL = CALLBACK_PREFIX + "detail";
    public static final String CALLBACK_BACK_TO_LIST = CALLBACK_PREFIX + "back";
    public static final String CALLBACK_SEND_LOCATION = CALLBACK_PREFIX + "location";


    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private Integer lastMessageId = null;

    @Override
    public String getName() {
        return "/getpost";
    }

    @Override
    public void execute(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        UserState userState = getUserState(userId);

        if (!TelegramBot.getInstance().getCommandManager().hasActiveCommand(userId)) {
            TelegramBot.getInstance().getCommandManager().setActiveCommand(userId, this);
        }

        try {
            switch (userState.currentState) {
                case AWAITING_LOCATION:
                    handleAwaitingLocation(update, userState);
                    break;
                case SHOWING_RESULTS:
                    // Обработка текстовых команд при просмотре результатов
                    break;
            }
        } catch (Exception e) {
            cleanupUserState(userId);
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка: " + e.getMessage());
        }
    }

    public void executeLocation(Update update, Location location) {
        Long userId = update.getMessage().getFrom().getId();
        if (!TelegramBot.getInstance().getCommandManager().hasActiveCommand(userId)) {
            TelegramBot.getInstance().getCommandManager().setActiveCommand(userId, this);
        }
        UserState userState = getUserState(userId);

        try {
            userState.userLocation = location;
            userState.currentState = State.SHOWING_RESULTS;
            showPostsPage(update.getMessage().getChatId(), userState, 0);
        } catch (Exception e) {
            cleanupUserState(userId);
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка при обработке местоположения");
        }
    }

    private void handleAwaitingLocation(Update update, UserState userState) {
        if (update.getMessage().hasLocation()) {
            executeLocation(update, update.getMessage().getLocation());
        } else {
            requestLocation(update.getMessage().getChatId());
        }
    }

    public void handlePageNavigation(Update update, String callbackData) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            String[] parts = callbackData.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Неверный формат callback данных");
            }
            int newPage = Integer.parseInt(parts[1]);
            showPostsPage(chatId, userState, newPage);
        } catch (Exception e) {
            cleanupUserState(userId);
            sendErrorMessage(chatId, "Ошибка навигации: " + e.getMessage());
        }
    }

    public void showPostDetails(Update update, long postId) {  // параметр postId определен здесь
        try {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();
            UserState userState = getUserState(userId);

            // Сохраняем текущую страницу перед переходом к деталям
            userState.lastListPage = userState.currentPage;

            // Находим пост в уже загруженных данных
            List<PolicePost> posts = getNearbyPosts(
                    userState.userLocation.getLatitude(),
                    userState.userLocation.getLongitude()
            );

            PolicePost post = posts.stream()
                    .filter(p -> p.id == postId)  // используем параметр postId
                    .findFirst()
                    .orElse(null);

            if (post == null) {
                sendErrorMessage(chatId, "Пост не найден");
                return;
            }

            // Формируем и отправляем сообщение с деталями
            String details = formatPostDetails(post, userState);
            InlineKeyboardMarkup keyboard = createBackKeyboard(postId);
            editOrSendMessage(chatId, details, keyboard);

        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(update.getCallbackQuery().getMessage().getChatId(),
                    "Ошибка при отображении деталей");
        }
    }

    public void handleBackToList(Update update) {
        try {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();
            UserState userState = getUserState(userId);

            // Возвращаемся на последнюю сохраненную страницу списка
            showPostsPage(chatId, userState, userState.lastListPage);
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(update.getCallbackQuery().getMessage().getChatId(),
                    "Ошибка при возврате к списку");
        }
    }

    private String formatPostDetails(PolicePost post, UserState userState) {
        return String.format(
                "🔍 Детали поста:\n\n" +
                        "Тип: %s%s\n" +
                        "Дата: %s\n" +
                        "Расстояние: %s\n" +
                        "Комментарий: %s\n\n" +
                        "Координаты: %.6f, %.6f",
                post.postType,
                post.expired ? " (Неактуален)" : "",
                post.registrationTime.toLocalDateTime(),
                post.distance,
                post.comment,
                post.latitude,
                post.longitude
        );
    }

    private void editOrSendMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            if (lastMessageId != null) {
                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(String.valueOf(chatId));
                editMessage.setMessageId(lastMessageId);
                editMessage.setText(text);
                editMessage.setReplyMarkup(keyboard);
                TelegramBot.getInstance().execute(editMessage);
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.setReplyMarkup(keyboard);
                Message sentMessage = TelegramBot.getInstance().execute(message);
                lastMessageId = sentMessage.getMessageId();
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private InlineKeyboardMarkup createBackKeyboard(long postId) {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("📍 Отправить локацию")
                                        .callbackData("post_location:" + postId)
                                        .build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("↩️ Назад к списку")
                                        .callbackData(CALLBACK_BACK_TO_LIST)
                                        .build()
                        )
                ))
                .build();
    }

    public void sendPostLocation(Update update, long postId) {
        try {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();
            UserState userState = getUserState(userId);

            List<PolicePost> posts = getNearbyPosts(
                    userState.userLocation.getLatitude(),
                    userState.userLocation.getLongitude()
            );

            PolicePost post = posts.stream()
                    .filter(p -> p.id == postId)
                    .findFirst()
                    .orElse(null);

            if (post == null) {
                sendErrorMessage(chatId, "Пост не найден");
                return;
            }

            SendLocation locationMessage = new SendLocation();
            locationMessage.setChatId(String.valueOf(chatId));
            locationMessage.setLatitude(post.latitude);
            locationMessage.setLongitude(post.longitude);

            TelegramBot.getInstance().execute(locationMessage);

            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
        } catch (Exception e) {
            e.printStackTrace();
            sendErrorMessage(update.getCallbackQuery().getMessage().getChatId(),
                    "Ошибка при отправке местоположения поста");
        }
    }

    private void showPostsPage(long chatId, UserState userState, int page) throws TelegramApiException {
        try {
            userState.currentPage = page;
            List<PolicePost> posts = getNearbyPosts(
                    userState.userLocation.getLatitude(),
                    userState.userLocation.getLongitude()
            );

            if (posts.isEmpty()) {
                sendNoPostsMessage(chatId);
                return;
            }

            InlineKeyboardMarkup keyboard = createPostsKeyboard(posts, page);

            if (lastMessageId == null) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("Ближайшие посты ДПС:");
                message.setReplyMarkup(keyboard);
                Message sentMessage = TelegramBot.getInstance().execute(message);
                lastMessageId = sentMessage.getMessageId();
            } else {
                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(String.valueOf(chatId));
                editMessage.setMessageId(lastMessageId);
                editMessage.setText("Ближайшие посты ДПС:");
                editMessage.setReplyMarkup(keyboard);
                TelegramBot.getInstance().execute(editMessage);
            }
        } catch (SQLException e) {
            throw new TelegramApiException("Ошибка при получении данных: " + e.getMessage());
        }
    }

    private InlineKeyboardMarkup createPostsKeyboard(List<PolicePost> posts, int page) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, posts.size());

        // Добавляем посты
        for (int i = start; i < end; i++) {
            PolicePost post = posts.get(i);
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(String.format("%s%s (%s)",
                                    post.expired ? "[Неактуален] " : "",
                                    post.postType,
                                    post.distance))
                            .callbackData(CALLBACK_POST_DETAIL + ":" + post.id)
                            .build()
            ));
        }

        // Добавляем кнопку страницы
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text(String.format("Страница %d", page + 1))
                        .callbackData("page_info")
                        .build()
        ));
        
        // Добавляем навигацию
        addNavigationButtons(rows, posts.size(), page);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private void addNavigationButtons(List<List<InlineKeyboardButton>> rows,
                                      int totalPosts, int currentPage) {
        if (totalPosts <= PAGE_SIZE) return;

        List<InlineKeyboardButton> navRow = new ArrayList<>();
        int totalPages = (int) Math.ceil((double) totalPosts / PAGE_SIZE);

        if (currentPage > 0) {
            navRow.add(InlineKeyboardButton.builder()
                    .text("← Назад")
                    .callbackData(CALLBACK_PREV_PAGE + ":" + (currentPage - 1))
                    .build());
        }

        if (currentPage < totalPages - 1) {
            navRow.add(InlineKeyboardButton.builder()
                    .text("Вперед →")
                    .callbackData(CALLBACK_NEXT_PAGE + ":" + (currentPage + 1))
                    .build());
        }

        if (!navRow.isEmpty()) {
            rows.add(navRow);
        }
    }

    private List<PolicePost> getNearbyPosts(double centerLat, double centerLon) throws SQLException {
        return TelegramBot.getDatabaseManager().getFilteredPolicePosts(centerLat, centerLon, 10);
    }

    private void sendNoPostsMessage(long chatId) {
        try {
            if (lastMessageId != null) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setChatId(String.valueOf(chatId));
                deleteMessage.setMessageId(lastMessageId);
                TelegramBot.getInstance().execute(deleteMessage);
                lastMessageId = null;
            }

            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("🚫 В радиусе 10 км посты не обнаружены");
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void requestLocation(long chatId) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("📍 Отправьте ваше местоположение для поиска ближайших постов:");
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendErrorMessage(long chatId, String errorText) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("⚠️ " + errorText);
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public UserState getUserState(Long userId) {
        return userStates.computeIfAbsent(userId, k -> new UserState());
    }

    private void cleanupUserState(Long userId) {
        userStates.remove(userId);
        lastMessageId = null;
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }
}