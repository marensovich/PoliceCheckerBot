package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Data.PolicePost;
import org.marensovich.Bot.Data.UserInfo;
import org.marensovich.Bot.Maps.MapUtils.Distance;
import org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers.MarkerColor;
import org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers.MarkerSize;
import org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers.MarkerStyle;
import org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers.YandexMapsMarkers;
import org.marensovich.Bot.Maps.YandexMapAPI.Utils.YandexMapsURL;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.*;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexMaps;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
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

    /**
     * Кол-во страниц
     */
    private static final int PAGE_SIZE = 5;

    /**
     * Установка переменных для текста callbackов
     */
    public static final String CALLBACK_PREFIX = "post_";
    public static final String CALLBACK_NEXT_PAGE = CALLBACK_PREFIX + "next";
    public static final String CALLBACK_PREV_PAGE = CALLBACK_PREFIX + "prev";
    public static final String CALLBACK_POST_DETAIL = CALLBACK_PREFIX + "detail";
    public static final String CALLBACK_BACK_TO_LIST = CALLBACK_PREFIX + "back";
    public static final String CALLBACK_SEND_LOCATION = CALLBACK_PREFIX + "location";
    public static final String CALLBACK_PAGE_INFO = CALLBACK_PREFIX + "info";
    public static final String CALLBACK_POST_PHOTO= CALLBACK_PREFIX + "photo";

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();
    private Integer lastMessageId = null;

    @Override
    public String getName() {
        return "/getpost";
    }

    /**
     * Запуск команды через /getpost
     * @param update
     */
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
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(userId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            cleanupUserState(userId);
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод запуска команды, реагирующий на поступающий update с геолокацией
     * @param update
     * @param location
     */
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
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            cleanupUserState(userId);
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getChatId(), "Ошибка при обработке местоположения");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getChatId());
        }
    }

    /**
     * Обработка для состояния AWAITING_LOCATION
     * @param update
     * @param userState
     */
    private void handleAwaitingLocation(Update update, UserState userState) {
        if (update.getMessage().hasLocation()) {
            executeLocation(update, update.getMessage().getLocation());
        } else {
            requestLocation(update.getMessage().getChatId());
        }
    }

    /**
     * Обработка нажатия на клавиши навигации
     * @param update
     * @param callbackData
     */
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
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            cleanupUserState(userId);
            TelegramBot.getInstance().sendErrorMessage(userId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);

        }
    }

    /**
     * Метод для формирования сообщения с подробной информацией о посте
     * @param update
     * @param postId
     */
    public void showPostDetails(Update update, long postId) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long userId = update.getCallbackQuery().getFrom().getId();
        try {

            UserState userState = getUserState(userId);

            userState.lastListPage = userState.currentPage;

            List<PolicePost> posts = getNearbyPosts(
                    userState.userLocation.getLatitude(),
                    userState.userLocation.getLongitude()
            );

            PolicePost post = posts.stream()
                    .filter(p -> p.id == postId)
                    .findFirst()
                    .orElse(null);

            if (post == null) {
                TelegramBot.getInstance().sendErrorMessage(chatId, "Пост не найден");
                return;
            }

            String details = formatPostDetails(post, userState);
            InlineKeyboardMarkup keyboard = createBackKeyboard(postId);
            editOrSendMessage(chatId, details, keyboard);

        } catch (Exception e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(userId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
        }
    }

    /**
     * Обработка нажатия на кнопку "назад" в меню с подробной информацией о посте
     * @param update
     */
    public void handleBackToList(Update update) {
        try {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            long userId = update.getCallbackQuery().getFrom().getId();
            UserState userState = getUserState(userId);

            showPostsPage(chatId, userState, userState.lastListPage);
        } catch (Exception e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getMessage().getChatId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getMessage().getChatId());
        }
    }

    /**
     * Создание формата сообщения с подробной информацией о посте
     * @param post
     * @param userState
     * @return
     */
    private String formatPostDetails(PolicePost post, UserState userState) {
        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        ZonedDateTime postTime = post.registrationTime.toLocalDateTime()
                .atZone(ZoneOffset.UTC)
                .withZoneSameInstant(moscowZone);

        ZonedDateTime now = ZonedDateTime.now(moscowZone);

        Duration duration = Duration.between(postTime, now);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyyy");
        String fullDateTime = postTime.format(formatter);

        String timeAgo;
        if (duration.toDays() > 0) {
            timeAgo = duration.toDays() + " " + pluralize(duration.toDays(), "день", "дня", "дней");
        } else if (duration.toHours() > 0) {
            timeAgo = duration.toHours() + " " + pluralize(duration.toHours(), "час", "часа", "часов");
        } else if (duration.toMinutes() > 0) {
            timeAgo = duration.toMinutes() + " " + pluralize(duration.toMinutes(), "минуту", "минуты", "минут");
        } else {
            timeAgo = duration.getSeconds() + " " + pluralize(duration.getSeconds(), "секунду", "секунды", "секунд");
        }

        return String.format(
                "\uD83D\uDD0D <b>Информация поста:</b>\n\n" +
                        "<b>Тип:</b> %s%s\n" +
                        "<b>Дата:</b> %s (%s назад) по МСК\n" +
                        "<b>Расстояние:</b> %.3s км\n" +
                        "<b>Комментарий:</b> %s\n\n" +
                        "<b>Координаты:</b> %.6f, %.6f",
                post.postType,
                post.expired ? " (Неактуален)" : "",
                fullDateTime,
                timeAgo,
                post.distance,
                post.comment.isEmpty() ? "Отсутствует" : post.comment,
                post.latitude,
                post.longitude
        );
    }

    private String pluralize(long number, String one, String few, String many) {
        if (number % 10 == 1 && number % 100 != 11) {
            return one;
        }
        if (number % 10 >= 2 && number % 10 <= 4 && (number % 100 < 10 || number % 100 >= 20)) {
            return few;
        }
        return many;
    }

    /**
     * Метод для создания нового или редактирования старого сообщения
     * @param chatId
     * @param text
     * @param keyboard
     */
    private void editOrSendMessage(long chatId, String text, InlineKeyboardMarkup keyboard) {
        try {
            if (lastMessageId != null) {
                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(String.valueOf(chatId));
                editMessage.setMessageId(lastMessageId);
                editMessage.setText(text);
                editMessage.setReplyMarkup(keyboard);
                editMessage.enableHtml(true);
                TelegramBot.getInstance().execute(editMessage);
            } else {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText(text);
                message.enableHtml(true);
                message.setReplyMarkup(keyboard);
                Message sentMessage = TelegramBot.getInstance().execute(message);
                lastMessageId = sentMessage.getMessageId();
            }
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            throw new RuntimeException(e);
        }
    }

    /**
     * Создание клавиатуры для сообщения с информацией о посте
     * @param postId
     * @return
     */
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

    /**
     * Обработка нажатия кнопки "отправить геолокацию" при просмотре подробностей поста
     * @param update
     * @param postId
     */
    public void sendPostLocation(Update update, long postId) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long userId = update.getCallbackQuery().getFrom().getId();
        try {

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
                TelegramBot.getInstance().sendErrorMessage(chatId, "Пост не найден");
                return;
            }

            SendLocation locationMessage = new SendLocation();
            locationMessage.setChatId(String.valueOf(chatId));
            locationMessage.setLatitude(post.latitude);
            locationMessage.setLongitude(post.longitude);

            TelegramBot.getInstance().execute(locationMessage);

            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
        } catch (Exception e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод показа главного сообщения с кнопками
     * @param chatId
     * @param userState
     * @param page
     * @throws TelegramApiException
     */
    private void showPostsPage(long chatId, UserState userState, int page){
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

            InlineKeyboardMarkup keyboard = createPostsKeyboard(posts, page, chatId);

            if (lastMessageId == null) {
                SendMessage message = new SendMessage();
                message.setChatId(String.valueOf(chatId));
                message.setText("\uD83D\uDE94 Ближайшие посты ДПС:");
                message.setReplyMarkup(keyboard);
                message.enableHtml(true);
                Message sentMessage = TelegramBot.getInstance().execute(message);
                lastMessageId = sentMessage.getMessageId();
            } else {
                EditMessageText editMessage = new EditMessageText();
                editMessage.setChatId(String.valueOf(chatId));
                editMessage.setMessageId(lastMessageId);
                editMessage.setText("\uD83D\uDE94 Ближайшие посты ДПС:");
                editMessage.enableHtml(true);
                editMessage.setReplyMarkup(keyboard);
                TelegramBot.getInstance().execute(editMessage);
            }
        } catch (SQLException | TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод создания кнопок для постов
     * @param posts
     * @param page
     * @return
     */
    private InlineKeyboardMarkup createPostsKeyboard(List<PolicePost> posts, int page, long chatid) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, posts.size());

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
        rows.add(List.of(
                InlineKeyboardButton.builder()
                        .text(String.format("Страница %d", page + 1))
                        .callbackData(CALLBACK_PAGE_INFO)
                        .build()
        ));
        UserInfo user = TelegramBot.getDatabaseManager().getUserInfo(chatid);
        if (!user.getSubscribe().equals("none")){
            rows.add(List.of(
                    InlineKeyboardButton.builder()
                            .text(String.format("Карта", page + 1))
                            .callbackData(CALLBACK_POST_PHOTO)
                            .build()
            ));
        }
        addNavigationButtons(rows, posts.size(), page);
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    /**
     * Метод добавления навигационных кнопок
     * @param rows
     * @param totalPosts
     * @param currentPage
     */
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

    /**
     * Получение списка постов
     * @param centerLat
     * @param centerLon
     * @return
     * @throws SQLException
     */
    private List<PolicePost> getNearbyPosts(double centerLat, double centerLon) throws SQLException {
        return TelegramBot.getDatabaseManager().getFilteredPolicePosts(centerLat, centerLon, 10);
    }

    /**
     * Обработка события при котором ни один пост в радиусе 10 км не был найден
     * @param chatId
     */
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
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Метод отправки фотографии карты с точками постов
     * @param update
     */
    public void handleSendMap(Update update) {
        UserState userState = getUserState(update.getCallbackQuery().getFrom().getId());
        UserInfo userInfo = TelegramBot.getDatabaseManager().getUserInfo(update.getCallbackQuery().getFrom().getId());

        if (userInfo.subscribe.equals("vip")){
            if (!(userInfo.genMap <= TelegramBot.getDatabaseManager().getIntValueBotData("limit_map_generation_VIP"))){
                TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "\uD83D\uDEAB Лимит создания карт для вашей подписки исчерпан. \nПодробнее в /userinfo");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
                return;
            }
        } else if (userInfo.subscribe.equals("premium")){
            if (!(userInfo.genMap <= TelegramBot.getDatabaseManager().getIntValueBotData("limit_map_generation_PREMIUM"))) {
                TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "\uD83D\uDEAB Лимит создания карт для вашей подписки исчерпан. \nПодробнее в /userinfo");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
                return;
            }
        } else if (userInfo.subscribe.equals("none")) {
            if (!(userInfo.genMap <= TelegramBot.getDatabaseManager().getIntValueBotData("limit_map_generation_NONE"))){
                TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "\uD83D\uDEAB Лимит создания карт для вашей подписки исчерпан. \nПодробнее в /userinfo");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
                return;
            }
        }

        List<PolicePost> posts = null;
        try {
            posts = getNearbyPosts(
                    userState.userLocation.getLatitude(),
                    userState.userLocation.getLongitude()
            );
        } catch (SQLException e) {
            TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        YandexMapsMarkers yandexMapsMarkers = new YandexMapsMarkers();

        for (PolicePost post : posts) {
            yandexMapsMarkers.addMarker(post.longitude, post.latitude, MarkerStyle.PM2, MarkerColor.RED, MarkerSize.LARGE);
        }
        yandexMapsMarkers.addMarker(userState.userLocation.getLongitude(), userState.userLocation.getLatitude(), MarkerStyle.PM2, MarkerColor.BLUE, MarkerSize.LARGE);

        InputStream is = null;
        try {
            is = new YandexMaps().getPhoto(Float.parseFloat(userState.userLocation.getLongitude().toString()),
                    Float.parseFloat(userState.userLocation.getLatitude().toString()),
                    null,
                    null,
                    null,
                    YandexMapSize.Large,
                    YandexMapScale.SCALE_1,
                    yandexMapsMarkers.generatePtParameter(),
                    null,
                    YandexMapLanguage.valueOf(userInfo.yandexLang),
                    null,
                    YandexMapTheme.valueOf(userInfo.yandexTheme),
                    YandexMapTypes.valueOf(userInfo.yandexMaptype));
        } catch (IOException e) {
            TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(update.getCallbackQuery().getFrom().getId());
        sendPhoto.setPhoto(new InputFile(is, "map.png"));

        try {
            Message msg = TelegramBot.getInstance().execute(sendPhoto);
            if (msg != null){
                try {
                    TelegramBot.getDatabaseManager().incrementGenMap(update.getCallbackQuery().getFrom().getId());
                } catch (SQLException e) {
                    TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                    TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
                    LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(update.getCallbackQuery().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getFrom().getId());
    }

    /**
     * Отправка запроса геолокации
     * @param chatId
     */
    private void requestLocation(long chatId) {
        try {



            SendMessage message = new SendMessage();
            message.setChatId(String.valueOf(chatId));
            message.setText("📍 Отправьте вашу геопозицию для поиска ближайших постов:");

            ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
            keyboardMarkup.setResizeKeyboard(true);
            keyboardMarkup.setOneTimeKeyboard(true);

            KeyboardRow row = new KeyboardRow();
            KeyboardButton locationButton = new KeyboardButton("Отправить местоположение");
            locationButton.setRequestLocation(true);
            row.add(locationButton);

            keyboardMarkup.setKeyboard(List.of(row));
            message.setReplyMarkup(keyboardMarkup);

            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    /**
     * Возвращает состояния пользователя
     * @param userId
     * @return
     */
    public UserState getUserState(Long userId) {
        return userStates.computeIfAbsent(userId, k -> new UserState());
    }

    /**
     * Очищает состояние пользователя
     * @param userId
     */
    private void cleanupUserState(Long userId) {
        userStates.remove(userId);
        lastMessageId = null;
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }
}