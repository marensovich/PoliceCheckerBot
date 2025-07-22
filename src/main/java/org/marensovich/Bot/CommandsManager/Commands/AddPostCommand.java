package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class AddPostCommand implements Command {

    public static final String CALLBACK_PREFIX = "post_";
    public static final String CALLBACK_DPS = CALLBACK_PREFIX + "dps";
    public static final String CALLBACK_PATROL = CALLBACK_PREFIX + "patrol";
    public static final String CALLBACK_CONFIRM = CALLBACK_PREFIX + "confirm";
    public static final String CALLBACK_CANCEL = CALLBACK_PREFIX + "cancel";
    public static final String CALLBACK_NO_COMMENT = CALLBACK_PREFIX + "no_comment";

    private enum State {
        AWAITING_LOCATION,
        AWAITING_TYPE,
        AWAITING_COMMENT,
        AWAITING_CONFIRMATION
    }

    public static class UserState {
        State currentState;
        Location postLocation;
        String postType;
        String comment;

        UserState() {
            this.currentState = State.AWAITING_LOCATION;
            this.comment = null;
        }

        public boolean isAwaitingComment() {
            return currentState == State.AWAITING_COMMENT;
        }

    }

    private final Map<Long, UserState> userStates = new HashMap<>();

    @Override
    public String getName() {
        return "/post";
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
                    handleLocationStage(update, userState);
                    break;
                case AWAITING_COMMENT:
                    handleCommentStage(update, userState);
                    break;
                case AWAITING_CONFIRMATION:
                    handleTextConfirmation(update, userState);
                    break;
            }
        } catch (Exception e) {
            TelegramBot.getInstance().sendErrorMessage(userId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            cleanupUserState(userId);
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getChatId(), "Ошибка: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void handlePostType(Update update, String callbackData) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        if (callbackData.equals(CALLBACK_DPS)) {
            userState.postType = "Пост ДПС";
        } else if (callbackData.equals(CALLBACK_PATROL)) {
            userState.postType = "Патрульная машина";
        }

        userState.currentState = State.AWAITING_COMMENT;
        requestComment(update.getCallbackQuery().getMessage().getChatId());
    }

    public void handleSkipComment(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        userState.comment = "Отсутствует";
        userState.currentState = State.AWAITING_CONFIRMATION;
        sendConfirmationMessage(update.getCallbackQuery().getMessage().getChatId(), userState);
    }

    public void handlePostConfirm(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        savePostToDatabase(userId, userState.postLocation, userState.postType, userState.comment);
        sendSuccessMessage(update.getCallbackQuery().getMessage().getChatId());
        cleanupUserState(userId);
    }

    public void handlePostCancel(Update update){
        Long userId = update.getCallbackQuery().getFrom().getId();
        sendCancelMessage(update.getCallbackQuery().getMessage().getChatId());
        cleanupUserState(userId);
    }

    private void handleLocationStage(Update update, UserState userState){
        if (update.getMessage().hasLocation()) {
            userState.postLocation = update.getMessage().getLocation();
            userState.currentState = State.AWAITING_TYPE;
            askForPostType(update.getMessage().getChatId());
        } else {
            requestLocation(update.getMessage().getChatId());
        }
    }

    private void handleCommentStage(Update update, UserState userState){
        if (update.getMessage().hasText()) {
            userState.comment = update.getMessage().getText();
            userState.currentState = State.AWAITING_CONFIRMATION;
            sendConfirmationMessage(update.getMessage().getChatId(), userState);
        }
    }

    private void handleTextConfirmation(Update update, UserState userState){
        String text = update.getMessage().getText();
        if (text.equalsIgnoreCase("да")) {
            savePostToDatabase(update.getMessage().getFrom().getId(),
                    userState.postLocation, userState.postType, userState.comment);
            sendSuccessMessage(update.getMessage().getChatId());
            cleanupUserState(update.getMessage().getFrom().getId());
        } else if (text.equalsIgnoreCase("нет")) {
            sendCancelMessage(update.getMessage().getChatId());
            cleanupUserState(update.getMessage().getFrom().getId());
        }
    }

    private void askForPostType(Long chatId){
        SendMessage removeKeyboard = new SendMessage();
        removeKeyboard.setChatId(chatId.toString());
        removeKeyboard.setText("Обработка данных...");
        removeKeyboard.setReplyMarkup(ReplyKeyboardRemove.builder()
                .removeKeyboard(true)
                .build());
        try {
            TelegramBot.getInstance().execute(removeKeyboard);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("\uD83D\uDDC2\uFE0F Выберите тип поста:");
        message.setReplyMarkup(getPostTypeKeyboard());
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void requestComment(Long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("✍\uFE0F Пожалуйста, введите комментарий к посту (необязательно):");
        message.setReplyMarkup(getSkipCommentKeyboard());
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void sendConfirmationMessage(Long chatId, UserState userState){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(getConfirmationMessage(userState));
        message.enableHtml(true);
        message.setReplyMarkup(getConfirmationKeyboard());
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void requestLocation(Long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("\uD83D\uDCCDОтметьте точку на карте, где расположены сотрудники ДПС используя геолокацию:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        KeyboardButton locationButton = new KeyboardButton("Отправить местоположение");
        locationButton.setRequestLocation(true);
        row.add(locationButton);

        keyboardMarkup.setKeyboard(List.of(row));
        message.setReplyMarkup(keyboardMarkup);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private InlineKeyboardMarkup getPostTypeKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("Пост ДПС")
                                        .callbackData(CALLBACK_DPS)
                                        .build()
                        ),
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("Патрульная машина")
                                        .callbackData(CALLBACK_PATROL)
                                        .build()
                        )
                ))
                .build();
    }

    private InlineKeyboardMarkup getSkipCommentKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("Пропустить")
                                        .callbackData(CALLBACK_NO_COMMENT)
                                        .build()
                        )
                ))
                .build();
    }

    private InlineKeyboardMarkup getConfirmationKeyboard() {
        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(
                        List.of(
                                InlineKeyboardButton.builder()
                                        .text("Подтвердить")
                                        .callbackData(CALLBACK_CONFIRM)
                                        .build(),
                                InlineKeyboardButton.builder()
                                        .text("Отменить")
                                        .callbackData(CALLBACK_CANCEL)
                                        .build()
                        )
                ))
                .build();
    }

    public UserState getUserState(Long userId) {
        return userStates.computeIfAbsent(userId, k -> new UserState());
    }

    private void cleanupUserState(Long userId) {
        userStates.remove(userId);
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }

    private String getConfirmationMessage(UserState userState) {
        String commentStatus = userState.comment != null ?
                userState.comment : "Комментарий отсутствует";

        return String.format(
                "\uD83D\uDEC2 <b>Подтвердите создание поста:</b>\n\n" +
                        "<b>Тип:</b> %s\n" +
                        "<b>Координаты:</b> %.6f, %.6f\n" +
                        "<b>Комментарий:</b> %s\n\n" +
                        "<b>Все верно?</b>",
                userState.postType,
                userState.postLocation.getLatitude(),
                userState.postLocation.getLongitude(),
                commentStatus
        );
    }

    private void savePostToDatabase(Long userId, Location location, String postType, String comment) {
        System.out.printf(
                "Создан пост: userId=%d, type=%s, lat=%.6f, lon=%.6f, comment=%s%n",
                userId, postType, location.getLatitude(), location.getLongitude(),
                comment
        );
        TelegramBot.getDatabaseManager().addPolicePost(userId, location, postType, comment);
    }

    private void sendSuccessMessage(Long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("<b>✅ Пост успешно создан!</b>");
        message.enableHtml(true);
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void sendCancelMessage(Long chatId){
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("❌ Создание поста отменено");
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}