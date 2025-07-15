package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class RegisterCommand implements Command {

    private static final String CHANNEL_ID = Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_ID");
    private static final String CHANNEL_LINK = Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_LINK");

    @Override
    public String getName() {
        return "/reg";
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        TelegramBot.getInstance().getCommandManager().setActiveCommand(userId, this);

        try {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(CHANNEL_ID);
            getChatMember.setUserId(userId);
            ChatMember userMember = TelegramBot.getInstance().execute(getChatMember);

            if (isMember(userMember.getStatus())) {
                registerUser(userId, chatId);
            } else {
                sendSubscriptionRequest(chatId);
            }
        } catch (TelegramApiException e) {
            sendErrorMessage(chatId, "⚠️ Ошибка при проверке подписки. Попробуйте позже.");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            e.printStackTrace();
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }

    private boolean isMember(String status) {
        return "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
    }

    private void sendSubscriptionRequest(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("📢 *Требуется подписка*\n\n" +
                "Для продолжения работы с ботом необходимо подписаться на наш канал.\n" +
                "После подписки нажмите кнопку *\"Проверить подписку\"*");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> subscribeRow = new ArrayList<>();
        InlineKeyboardButton subscribeButton = new InlineKeyboardButton();
        subscribeButton.setText("Подписаться на канал ▼");
        subscribeButton.setUrl(CHANNEL_LINK);
        subscribeRow.add(subscribeButton);
        keyboard.add(subscribeRow);

        List<InlineKeyboardButton> checkRow = new ArrayList<>();
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText("Проверить подписку ✅");
        checkButton.setCallbackData("check_subscription");
        checkRow.add(checkButton);
        keyboard.add(checkRow);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void registerUser(Long userId, Long chatId) {
        try {
            DatabaseManager dbManager = TelegramBot.getInstance().getDatabaseManager();

            if (dbManager.checkUsersExists(userId)) {
                sendSuccessMessage(chatId, "ℹ️ Вы уже зарегистрированы!");
            } else {
                dbManager.addUser(userId);
                sendSuccessMessage(chatId, "✅ Регистрация прошла успешно!\nТеперь вам открыты часть функций бота, подробнее в /help.");
            }
        } catch (Exception e) {
            sendErrorMessage(chatId, "⚠️ Ошибка при регистрации. Попробуйте позже.");
            e.printStackTrace();
        }
    }

    private void sendSuccessMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setParseMode("Markdown");
        TelegramBot.getInstance().execute(message);
    }

    private void sendErrorMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}