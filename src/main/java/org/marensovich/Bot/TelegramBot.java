package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CallbackManager.CallbackManager;
import org.marensovich.Bot.CommandsManager.CommandManager;
import org.marensovich.Bot.UpdateManager.UpdateHandler;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;
    private final DatabaseManager databaseManager;

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        CallbackManager callbackManager = new CallbackManager();
        CommandManager commandManager = new CommandManager();
        instance = this;
    }

    public static TelegramBot getInstance() {
        if (instance == null) throw new IllegalStateException("Бот не инициализирован");
        return instance;
    }

    public static DatabaseManager getDatabaseManager() { return instance.databaseManager; }

    @Override
    public void onUpdateReceived(Update update) {
        UpdateHandler updateHandler = new UpdateHandler();
        updateHandler.updateHandler(update);
    }
    @Override
    public String getBotUsername() {
        return Dotenv.load().get("TELEGRAM_BOT_USERNAME");
    }

    @Override
    public String getBotToken() {
        return Dotenv.load().get("TELEGRAM_BOT_TOKEN");
    }
}
