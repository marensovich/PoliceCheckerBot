package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.CommandManager;
import org.marensovich.Bot.YandexMapAPI.*;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;
    private final DatabaseManager databaseManager;
    private final CommandManager commandManager;

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.commandManager = new CommandManager();
        instance = this;
    }

    public static TelegramBot getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Бот не инициализирован");
        }
        return instance;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() || update.hasCallbackQuery()){
            if (update.hasMessage()){
                if (!databaseManager.checkAllUsersExists(update.getMessage().getFrom().getId())){
                    databaseManager.addAllUser(update.getMessage().getFrom().getId());
                }
            }
            if (update.hasCallbackQuery()){
                if (!databaseManager.checkAllUsersExists(update.getCallbackQuery().getFrom().getId())){
                    databaseManager.addAllUser(update.getCallbackQuery().getFrom().getId());
                }
            }
        }
        if (update.hasMessage()) {
            if (update.getMessage().getText().startsWith("/")){
                if (!commandManager.executeCommand(update)) {
                    SendMessage message = new SendMessage();
                    message.setChatId(update.getMessage().getChatId().toString());
                    message.setText("Команда не распознана. Используйте /help для списка команд.");
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (update.hasCallbackQuery()) {
                String callbackData = update.getCallbackQuery().getData();
                if (callbackData.startsWith("callback_post")) {
                    SendLocation sendLocation = new SendLocation();
                    sendLocation.setLatitude(55.389249);
                    sendLocation.setLongitude(36.946066);
                    sendLocation.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                    try {
                        execute(sendLocation);

                        AnswerCallbackQuery answer = new AnswerCallbackQuery();
                        answer.setCallbackQueryId(update.getCallbackQuery().getId());

                        execute(answer);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
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
