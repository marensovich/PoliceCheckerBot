package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements Command {
    @Override
    public String getName() {
        return "/help";
    }

    @Override
    public void execute(Update update) {
        DatabaseManager databaseManager = TelegramBot.getInstance().getDatabaseManager();
        if (databaseManager.checkUserIsAdmin(update.getMessage().getFrom().getId())) {
            String helpMessage = "<b>Помощь /help</b>\n\n" +
                    "Добро пожаловать! Этот бот предназначен для удобного отслеживания информации о постах ДПС\n" +
                    "Ниже представлены основные команды:\n\n" +
                    "<b>Основные команды:</b>\n" +
                    "• <code>/start</code> — начать работу с ботом\n" +
                    "• <code>/help</code> — вывести это сообщение\n" +
                    "• <code>/settings</code> — настроить параметры аккаунта\n" +
                    "• <code>/reg</code> — зарегистрироваться в системе\n" +
                    "• <code>/subscribe</code> — информация о подписках\n" +
                    "• <code>/getID</code> — получить ваш ID\n\n" +
                    "<b>Админские команды:</b>\n" +
                    "• <code>/agivesub &lt;user_id&gt; &lt;vip/premium&gt;</code> — выдать подписку пользователю\n" +
                    "• <code>/adelsub &lt;user_id&gt; &lt;reason&gt;</code> — снять подписку у пользователя\n" +
                    "• <code>/auserinfo &lt;user_id&gt;</code> — получить информацию о пользователе\n\n" +
                    "Если у вас есть вопросы, обращайтесь к администратору - @marensovich";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(helpMessage);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<InlineKeyboardButton> checkRow = new ArrayList<>();
            InlineKeyboardButton checkButton = new InlineKeyboardButton();
            checkButton.setText("✔ Написать");
            checkButton.setUrl("https://t.me/marensovich");
            checkRow.add(checkButton);
            keyboard.add(checkRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            String helpMessage = "<b>Помощь /help</b>\n\n" +
                    "Добро пожаловать! Этот бот предназначен для удобного отслеживания информации о постах ДПС\n" +
                    "Ниже представлены основные команды:\n\n" +
                    "<b>Основные команды:</b>\n" +
                    "• <code>/start</code> — начать работу с ботом\n" +
                    "• <code>/help</code> — вывести это сообщение\n" +
                    "• <code>/settings</code> — настроить параметры аккаунта\n" +
                    "• <code>/reg</code> — зарегистрироваться в системе\n" +
                    "• <code>/subscribe</code> — информация о подписках\n" +
                    "• <code>/getID</code> — получить ваш ID\n\n" +
                    "Если у вас есть вопросы, обращайтесь к администратору - @marensovich";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(helpMessage);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<InlineKeyboardButton> checkRow = new ArrayList<>();
            InlineKeyboardButton checkButton = new InlineKeyboardButton();
            checkButton.setText("✔ Написать");
            checkButton.setUrl("https://t.me/marensovich");
            checkRow.add(checkButton);
            keyboard.add(checkRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
