package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
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

    DatabaseManager databaseManager;

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
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
            if (update.getMessage().getText().startsWith("/") &&
                    !update.getMessage().getText().equals("/start") &&
                    !update.getMessage().getText().equals("/help") &&
                    !update.getMessage().getText().equals("/test")) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(update.getMessage().getChatId().toString());
                sendMessage.setText("Я не знаю такую команду. \nВведите /help чтобы узнать о моих возможностях.");
                try {
                    execute(sendMessage);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().getText().equals("/test")) {
                try {
                    YandexMapsURL mapsURL = new YandexMapsURL();
                    String url_string = mapsURL.generateURL(
                            36.946066f,55.389249f,
                            0.05f, null,
                            18, YandexMapSize.Large,
                            YandexMapScale.SCALE_1,
                            null,
                            null, YandexMapLanguage.ru_RU,
                            null, YandexMapTheme.Dark, YandexMapTypes.transit
                    );

                    URL url = new URL(url_string);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                    connection.setRequestProperty("Accept", "image/png");

                    int responseCode = connection.getResponseCode();
                    System.out.println("[DEBUG] HTTP Response Code: " + responseCode);

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        String errorMessage;
                        try (InputStream errorStream = connection.getErrorStream()) {
                            if (errorStream != null) {
                                errorMessage = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                                System.out.println("[ERROR] Yandex API Error Response:\n" + errorMessage);
                                if (errorMessage.contains("<error>")) {
                                    errorMessage = errorMessage.split("<message>")[1].split("</message>")[0];
                                }
                            } else {
                                errorMessage = "No error details provided by server";
                            }
                        }

                        String userFriendlyMessage = switch (responseCode) {
                            case 400 -> "Ошибка в параметрах запроса: " + errorMessage;
                            case 403 -> "Проблема с API-ключом: " + errorMessage;
                            case 429 -> "Слишком много запросов. Попробуйте позже.";
                            default -> "Ошибка сервера (код " + responseCode + "): " + errorMessage;
                        };
                        throw new IOException(userFriendlyMessage);
                    }

                    try (InputStream inputStream = connection.getInputStream()) {
                        BufferedImage image = ImageIO.read(inputStream);
                        if (image == null) {
                            throw new IOException("Не удалось прочитать изображение карты");
                        }
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ImageIO.write(image, "png", os);
                        InputStream is = new ByteArrayInputStream(os.toByteArray());

                        SendPhoto sendPhoto = new SendPhoto();
                        sendPhoto.setChatId(update.getMessage().getChatId().toString());
                        sendPhoto.setPhoto(new InputFile(is, "map.png"));
                        execute(sendPhoto);
                    }
                } catch (Exception e) {
                    System.err.println("[FATAL] Error processing map request:");
                    e.printStackTrace();

                    SendMessage errorMsg = new SendMessage();
                    errorMsg.setChatId(update.getMessage().getChatId().toString());
                    errorMsg.setText("❌ Ошибка при получении карты:\n" + e.getMessage());
                    try {
                        execute(errorMsg);
                    } catch (TelegramApiException ex) {
                        throw new RuntimeException(ex);
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
                if (update.getMessage().getText().equals("/help")) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(update.getMessage().getChatId().toString());
                    sendMessage.setText("Информация о боте скоро будет");
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (update.getMessage().getText().equals("/start")) {
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(update.getMessage().getChatId().toString());
                    sendMessage.setText("Бот пока в разработке, следите за новостями.");
                    try {
                        execute(sendMessage);
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
