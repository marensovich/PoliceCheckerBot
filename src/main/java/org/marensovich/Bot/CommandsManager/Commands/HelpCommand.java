package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
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
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        DatabaseManager databaseManager = TelegramBot.getDatabaseManager();
        boolean isRegistered = databaseManager.checkUsersExists(update.getMessage().getFrom().getId());

        if (!isRegistered) {
            String helpMessage = """
                    <b>Помощь /help</b>
                    
                    Добро пожаловать! Этот бот предназначен для удобного отслеживания информации о постах ДПС
                    Для расширения списка доступных команд - зарегистрируйтесь через /reg\s
                    Ниже представлены основные команды:
                    
                    <b>Основные команды:</b>
                    • <code>/start</code> — начать работу с ботом
                    • <code>/help</code> — вывести это сообщение
                    • <code>/reg</code> — зарегистрироваться в системе
                    • <code>/cancel</code> — отменить активную команду
                    Если у вас есть вопросы, обращайтесь в сообщения канала""";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(helpMessage);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<InlineKeyboardButton> checkRow = new ArrayList<>();
            InlineKeyboardButton checkButton = new InlineKeyboardButton();
            checkButton.setText("✔ Написать");
            checkButton.setUrl(Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_LINK"));
            checkRow.add(checkButton);
            keyboard.add(checkRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        if (databaseManager.checkUserIsAdmin(update.getMessage().getFrom().getId())) {
            String helpMessage = """
                    <b>Помощь /help</b>
                    
                    Добро пожаловать! Этот бот предназначен для удобного отслеживания информации о постах ДПС
                    Ниже представлены основные команды:
                    
                    <b>Основные команды:</b>
                    • <code>/start</code> — начать работу с ботом
                    • <code>/help</code> — вывести это сообщение
                    • <code>/settings</code> — настроить параметры аккаунта
                    • <code>/cancel</code> — отменить активную команду
                    • <code>/reg</code> — зарегистрироваться в системе
                    • <code>/subscribe</code> — информация о подписках
                    • <code>/getID</code> — получить ваш ID
                    
                    <b>Админские команды:</b>
                    • <code>/agivesub &lt;user_id&gt; &lt;vip/premium&gt;</code> — выдать подписку пользователю
                    • <code>/adelsub &lt;user_id&gt; &lt;reason&gt;</code> — снять подписку у пользователя
                    • <code>/auserinfo &lt;user_id&gt;</code> — получить информацию о пользователе
                    
                    Если у вас есть вопросы, обращайтесь в сообщения канала""";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(helpMessage);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<InlineKeyboardButton> checkRow = new ArrayList<>();
            InlineKeyboardButton checkButton = new InlineKeyboardButton();
            checkButton.setText("✔ Написать");
            checkButton.setUrl(Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_LINK"));
            checkRow.add(checkButton);
            keyboard.add(checkRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
        } else {
            String helpMessage = """
                    <b>Помощь /help</b>
                    
                    Добро пожаловать! Этот бот предназначен для удобного отслеживания информации о постах ДПС
                    Ниже представлены основные команды:
                    
                    <b>Основные команды:</b>
                    • <code>/start</code> — начать работу с ботом
                    • <code>/help</code> — вывести это сообщение
                    • <code>/settings</code> — настроить параметры аккаунта
                    • <code>/cancel</code> — отменить активную команду
                    • <code>/reg</code> — зарегистрироваться в системе
                    • <code>/subscribe</code> — информация о подписках
                    • <code>/getID</code> — получить ваш ID
                    
                    Если у вас есть вопросы, обращайтесь в сообщения канала""";
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId().toString());
            sendMessage.setParseMode("HTML");
            sendMessage.setText(helpMessage);

            InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

            List<InlineKeyboardButton> checkRow = new ArrayList<>();
            InlineKeyboardButton checkButton = new InlineKeyboardButton();
            checkButton.setText("✔ Написать");
            checkButton.setUrl(Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_LINK"));
            checkRow.add(checkButton);
            keyboard.add(checkRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
        }
    }
}
