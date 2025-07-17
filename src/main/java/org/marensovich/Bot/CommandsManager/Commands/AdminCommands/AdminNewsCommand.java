package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class AdminNewsCommand implements Command {

    public static final String CALLBACK_PREFIX = "news_";
    public static final String CALLBACK_TYPE_1 = CALLBACK_PREFIX + "type_1";
    public static final String CALLBACK_TYPE_2 = CALLBACK_PREFIX + "type_2";
    public static final String CALLBACK_TYPE_3 = CALLBACK_PREFIX + "type_3";
    public static final String CALLBACK_CONFIRM = CALLBACK_PREFIX + "confirm";
    public static final String CALLBACK_CANCEL = CALLBACK_PREFIX + "cancel";

    private enum State {
        AWAITING_TYPE,
        AWAITING_TEXT,
        AWAITING_CONFIRMATION
    }

    public static class UserState {
        State currentState;
        int newsType;
        String newsText;

        UserState() {
            this.currentState = State.AWAITING_TYPE;
        }

        public boolean isAwaitingText() {
            return currentState == State.AWAITING_TEXT;
        }
    }

    private final Map<Long, UserState> userStates = new HashMap<>();

    @Override
    public String getName() {
        return "/news";
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
                case AWAITING_TYPE:
                    sendNewsTypeOptions(update.getMessage().getChatId());
                    break;
                case AWAITING_TEXT:
                    handleNewsText(update, userState);
                    break;
                case AWAITING_CONFIRMATION:
                    break;
            }
        } catch (Exception e) {
            cleanupUserState(userId);
            sendErrorMessage(update.getMessage().getChatId(), "Ошибка: " + e.getMessage());
        }
    }

    public void handleNewsType(Update update, String callbackData) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        switch (callbackData) {
            case CALLBACK_TYPE_1:
                userState.newsType = 1;
                break;
            case CALLBACK_TYPE_2:
                userState.newsType = 2;
                break;
            case CALLBACK_TYPE_3:
                userState.newsType = 3;
                break;
        }

        userState.currentState = State.AWAITING_TEXT;
        requestNewsText(update.getCallbackQuery().getMessage().getChatId(),
                update.getCallbackQuery().getMessage().getMessageId());
    }

    public void handleNewsConfirm(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        UserState userState = getUserState(userId);

        sendNewsToUsers(userState);
        sendSuccessMessage(update.getCallbackQuery().getMessage().getChatId(),
                update.getCallbackQuery().getMessage().getMessageId());
        cleanupUserState(userId);
    }

    public void handleNewsCancel(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        sendCancelMessage(update.getCallbackQuery().getMessage().getChatId(),
                update.getCallbackQuery().getMessage().getMessageId());
        cleanupUserState(userId);
    }

    private void sendNewsTypeOptions(Long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("Выберите таблицу БД для рассылки:");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(createInlineButton("All_Users", CALLBACK_TYPE_1));
        keyboard.add(row1);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(createInlineButton("Users", CALLBACK_TYPE_2));
        keyboard.add(row2);

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        row3.add(createInlineButton("Обе таблицы", CALLBACK_TYPE_3));
        keyboard.add(row3);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        TelegramBot.getInstance().execute(message);
    }

    private void requestNewsText(Long chatId, Integer messageId) throws TelegramApiException {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setText("Введите текст новости, которую хотите разослать:");
        TelegramBot.getInstance().execute(editMessage);
    }

    private void handleNewsText(Update update, UserState userState) throws TelegramApiException {
        userState.newsText = update.getMessage().getText();
        userState.currentState = State.AWAITING_CONFIRMATION;
        sendConfirmation(update.getMessage().getChatId(), userState);
    }

    private void sendConfirmation(Long chatId, UserState userState) throws TelegramApiException {
        String recipientDescription = getRecipientDescription(userState.newsType);

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(String.format(
                "Вы уверены, что хотите отправить эту новость %s?\n\n%s",
                recipientDescription,
                userState.newsText
        ));

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton("✅ Подтвердить", CALLBACK_CONFIRM));
        row.add(createInlineButton("❌ Отменить", CALLBACK_CANCEL));
        keyboard.add(row);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        TelegramBot.getInstance().execute(message);
    }

    private void sendNewsToUsers(UserState userState) {
        long[] userIds = getRecipients(userState.newsType);

        SendMessage message = new SendMessage();
        message.setChatId(Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_ID"));
        message.setText("<b> Рассылка от Администратора </b>\n\n" + userState.newsText);
        message.enableHtml(true);
        try {
            TelegramBot.getInstance().execute(message);
            Thread.sleep(100);
        } catch (Exception e) {
            System.err.println("Ошибка при отправке новости в группу" + Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_ID") + ": " + e.getMessage());
        }

        for (long userId : userIds) {
            SendMessage newsMessage = new SendMessage();
            newsMessage.setChatId(String.valueOf(userId));
            newsMessage.setText("<b> Рассылка от Администратора </b>\n\n" + userState.newsText);
            newsMessage.enableHtml(true);
            try {
                TelegramBot.getInstance().execute(newsMessage);
                Thread.sleep(100);
            } catch (Exception e) {
                System.err.println("Ошибка при отправке новости пользователю " + userId + ": " + e.getMessage());
            }
        }
    }

    private void sendSuccessMessage(Long chatId, Integer messageId) throws TelegramApiException {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setText("✅ Новость успешно отправлена!");
        TelegramBot.getInstance().execute(editMessage);
    }

    private void sendCancelMessage(Long chatId, Integer messageId) throws TelegramApiException {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(chatId.toString());
        editMessage.setMessageId(messageId);
        editMessage.setText("❌ Отправка новости отменена");
        TelegramBot.getInstance().execute(editMessage);
    }

    private String getRecipientDescription(int newsType) {
        switch (newsType) {
            case 1: return "всем пользователям из All_Users";
            case 2: return "всем пользователям из Users";
            case 3: return "всем пользователям из обоих таблиц";
            default: return "";
        }
    }

    private long[] getRecipients(int newsType) {
        switch (newsType) {
            case 1: return TelegramBot.getDatabaseManager().getAllUsersTableIDs();
            case 2: return TelegramBot.getDatabaseManager().getUsersTableIDs();
            case 3: return TelegramBot.getDatabaseManager().getAllUsers();
            default: return new long[0];
        }
    }

    private InlineKeyboardButton createInlineButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }

    public UserState getUserState(Long userId) {
        return userStates.computeIfAbsent(userId, k -> new UserState());
    }

    private void cleanupUserState(Long userId) {
        userStates.remove(userId);
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }

    private void sendErrorMessage(Long chatId, String errorText) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText("⚠️ " + errorText);
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}