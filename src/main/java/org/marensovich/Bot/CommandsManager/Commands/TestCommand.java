package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.YandexMapAPI.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TestCommand implements Command {
    @Override
    public String getName() {
        return "/test";
    }

    @Override
    public void execute(Update update) {
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
                TelegramBot.getInstance().execute(sendPhoto);
            }
        } catch (Exception e) {
            System.err.println("[FATAL] Error processing map request:");
            e.printStackTrace();

            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(update.getMessage().getChatId().toString());
            errorMsg.setText("❌ Ошибка при получении карты:\n" + e.getMessage());
            try {
                TelegramBot.getInstance().execute(errorMsg);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
