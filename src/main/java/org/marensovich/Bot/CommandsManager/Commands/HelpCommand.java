package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements Command {

    public static final String CALLBACK_HELP = "callback_help";

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
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
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
                    • <code>/getid</code> — получить ваш ID
                    • <code>/post</code> — добавить информацию о посте
                    • <code>/getpost</code> — получить информацию о посте
                    
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

            List<InlineKeyboardButton> helpRow = new ArrayList<>();
            InlineKeyboardButton helpButton = new InlineKeyboardButton();
            helpButton.setText("Как пользоваться ботом?");
            helpButton.setCallbackData(CALLBACK_HELP);
            helpRow.add(helpButton);
            keyboard.add(helpRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
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
                    • <code>/getid</code> — получить ваш ID
                    • <code>/post</code> — добавить информацию о посте
                    • <code>/getpost</code> — получить информацию о посте
                    
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

            List<InlineKeyboardButton> helpRow = new ArrayList<>();
            InlineKeyboardButton helpButton = new InlineKeyboardButton();
            helpButton.setText("Как пользоваться ботом?");
            helpButton.setCallbackData(CALLBACK_HELP);
            helpRow.add(helpButton);
            keyboard.add(helpRow);

            keyboardMarkup.setKeyboard(keyboard);
            sendMessage.setReplyMarkup(keyboardMarkup);
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
        }
    }

    public void handleHelpCallback(Update update){
        SendPhoto step1 = new SendPhoto();
        step1.setChatId(update.getCallbackQuery().getFrom().getId());
        step1.setCaption("""
                <b>1 Шаг: </b> Введите команду /post чтобы добавить экипаж, который вы встретили, на карты.
                """);
        step1.setParseMode("HTML");
        step1.setPhoto(TelegramBot.getInstance().getPhotoFromResources("images/help_1.jpg"));

        SendPhoto step2 = new SendPhoto();
        step2.setChatId(update.getCallbackQuery().getFrom().getId());
        step2.setCaption("""
                <b>2 Шаг: </b> Введите команду /getpost чтобы открыть список постов в вашем округе.
                """);
        step2.setParseMode("HTML");
        step2.setPhoto(TelegramBot.getInstance().getPhotoFromResources("images/help_2.jpg"));

        try {
            TelegramBot.getInstance().execute(step1);
            TelegramBot.getInstance().execute(step2);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            throw new RuntimeException(e);
        }
    }
}
