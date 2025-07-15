package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
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

    private enum State {
        AWAITING_LOCATION,
        AWAITING_TYPE,
        AWAITING_CONFIRMATION
    }

    private static class UserState {
        State currentState;
        Location postLocation;
        String postType;

        UserState() {
            this.currentState = State.AWAITING_LOCATION;
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
        if (!TelegramBot.getInstance().getCommandManager().hasActiveCommand(userId)){
            TelegramBot.getInstance().getCommandManager().setActiveCommand(userId, this);
        }
        try {
            switch (userState.currentState) {
                case AWAITING_LOCATION:
                    handleLocationStage(update, userState);
                    break;
                case AWAITING_CONFIRMATION:
                    handleTextConfirmation(update, userState);
                    break;
            }
        } catch (Exception e) {
            cleanupUserState(userId);
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка: " + e.getMessage());
        }
    }

    public void handlePostType(Update update, String callbackData) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        try {
            if (callbackData.equals(CALLBACK_DPS)) {
                userState.postType = "Пост ДПС";
            } else if (callbackData.equals(CALLBACK_PATROL)) {
                userState.postType = "Патрульная машина";
            }
            userState.currentState = State.AWAITING_CONFIRMATION;

            SendMessage message = new SendMessage();
            message.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
            message.setText(getConfirmationMessage(userState));
            message.setReplyMarkup(getConfirmationKeyboard());
            TelegramBot.getInstance().execute(message);
        } catch (Exception e) {
            cleanupUserState(userId);
            sendErrorMessage(update.getCallbackQuery().getMessage().getChatId(), "Ошибка выбора типа: " + e.getMessage());
        }
    }

    public void handlePostConfirm(Update update) {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        try {
            savePostToDatabase(userId, userState.postLocation, userState.postType);
            sendSuccessMessage(update.getCallbackQuery().getMessage().getChatId());
            cleanupUserState(userId);
        } catch (Exception e) {
            sendErrorMessage(update.getCallbackQuery().getMessage().getChatId(), "Ошибка сохранения: " + e.getMessage());
            cleanupUserState(userId);
        }
    }

    public void handlePostCancel(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        sendCancelMessage(update.getCallbackQuery().getMessage().getChatId());
        cleanupUserState(userId);
    }

    private UserState getUserState(Long userId) {
        return userStates.computeIfAbsent(userId, k -> new UserState());
    }

    private void cleanupUserState(Long userId) {
        userStates.remove(userId);
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }

    private void handleLocationStage(Update update, UserState userState) throws TelegramApiException {
        if (update.getMessage().hasLocation()) {
            userState.postLocation = update.getMessage().getLocation();
            userState.currentState = State.AWAITING_TYPE;

            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getChatId().toString());
            message.setText("Выберите тип поста:");
            message.setReplyMarkup(getPostTypeKeyboard());
            TelegramBot.getInstance().execute(message);
        } else {
            requestLocation(update.getMessage().getChatId());
        }
    }

    private void handleTextConfirmation(Update update, UserState userState) throws TelegramApiException {
        String text = update.getMessage().getText();
        if (text.equalsIgnoreCase("да")) {
            savePostToDatabase(update.getMessage().getFrom().getId(), userState.postLocation, userState.postType);
            sendSuccessMessage(update.getMessage().getChatId());
            cleanupUserState(update.getMessage().getFrom().getId());
        } else if (text.equalsIgnoreCase("нет")) {
            sendCancelMessage(update.getMessage().getChatId());
            cleanupUserState(update.getMessage().getFrom().getId());
        }
    }

    private void requestLocation(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Пожалуйста, поделитесь вашей геопозицией:");

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton locationButton = new KeyboardButton("Отправить местоположение");
        locationButton.setRequestLocation(true);
        row.add(locationButton);

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setOneTimeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);
        TelegramBot.getInstance().execute(message);
    }

    private InlineKeyboardMarkup getPostTypeKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(InlineKeyboardButton.builder()
                .text("Пост ДПС")
                .callbackData(CALLBACK_DPS)
                .build());

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(InlineKeyboardButton.builder()
                .text("Патрульная машина")
                .callbackData(CALLBACK_PATROL)
                .build());

        rows.add(row1);
        rows.add(row2);
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup getConfirmationKeyboard() {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder()
                .text("Подтвердить")
                .callbackData(CALLBACK_CONFIRM)
                .build());
        row.add(InlineKeyboardButton.builder()
                .text("Отменить")
                .callbackData(CALLBACK_CANCEL)
                .build());

        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    private String getConfirmationMessage(UserState userState) {
        return String.format(
                "Подтвердите создание поста:\n\n" +
                        "Тип: %s\n" +
                        "Координаты: %.6f, %.6f\n\n" +
                        "Все верно?",
                userState.postType,
                userState.postLocation.getLatitude(),
                userState.postLocation.getLongitude()
        );
    }

    private void savePostToDatabase(Long userId, Location location, String postType) {
        System.out.printf("Создан пост: userId=%d, type=%s, lat=%.6f, lon=%.6f%n",
                userId, postType, location.getLatitude(), location.getLongitude());
    }

    private void sendSuccessMessage(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Пост успешно создан!");
        TelegramBot.getInstance().execute(message);
    }

    private void sendCancelMessage(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Создание поста отменено");
        TelegramBot.getInstance().execute(message);
    }

    private void sendErrorMessage(Long chatId, String errorText) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(errorText);
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}