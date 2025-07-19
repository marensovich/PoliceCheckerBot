package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CallbackManager.CallbackManager;
import org.marensovich.Bot.CommandsManager.CommandManager;
import org.marensovich.Bot.UpdateManager.UpdateHandler;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;
    private final DatabaseManager databaseManager;
    private final CommandManager commandManager;
    private final CallbackManager callbackManager;

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.commandManager = new CommandManager();
        this.callbackManager = new CallbackManager();
        instance = this;
    }

    public static TelegramBot getInstance() {
        if (instance == null) throw new IllegalStateException("Бот не инициализирован");
        return instance;
    }

    public static DatabaseManager getDatabaseManager() { return instance.databaseManager; }

    public CallbackManager getCallbackManager() { return callbackManager; }

    public CommandManager getCommandManager() { return commandManager; }

    @Override
    public void onUpdateReceived(Update update) {
        UpdateHandler updateHandler = new UpdateHandler();
        try {
            updateHandler.updateHandler(update);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }


    public InputFile getPhotoFromResources(String filename) {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new IllegalArgumentException("File not found in resources: " + filename);
        }
        InputFile inputFile = new InputFile();
        inputFile.setMedia(inputStream, filename);
        return inputFile;
    }

    @Override
    public String getBotUsername() { return Dotenv.load().get("TELEGRAM_BOT_USERNAME"); }

    @Override
    public String getBotToken() { return Dotenv.load().get("TELEGRAM_BOT_TOKEN"); }

    public void sendErrorMessage(Long chatId, String text) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId.toString());
            message.setText(text);
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}