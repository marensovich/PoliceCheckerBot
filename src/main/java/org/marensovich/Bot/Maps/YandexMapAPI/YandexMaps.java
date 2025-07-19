package org.marensovich.Bot.Maps.YandexMapAPI;

import org.marensovich.Bot.Maps.YandexMapAPI.Utils.YandexMapsURL;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.*;
import org.marensovich.Bot.Utils.LoggerUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class YandexMaps {

    public InputStream getPhoto(float latitude, float longitude,
                                Float spn, String bbox,
                                Integer z, YandexMapSize mapSize,
                                YandexMapScale scale, String pt,
                                String pl, YandexMapLanguage lang,
                                String style, YandexMapTheme theme, YandexMapTypes maptype) throws IOException {
        YandexMapsURL mapsURL = new YandexMapsURL();
        String url_string = mapsURL.generateURL(
                latitude,longitude, spn, bbox, z, mapSize,
                scale, pt, pl, lang, style, theme, maptype
        );

        URL url = new URL(url_string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Accept", "image/png");

        int responseCode = connection.getResponseCode();
        LoggerUtil.logDebug(getClass(), "[DEBUG] HTTP Response Code: " + responseCode);

        if (responseCode != HttpURLConnection.HTTP_OK) {
            String errorMessage;
            try (InputStream errorStream = connection.getErrorStream()) {
                if (errorStream != null) {
                    errorMessage = new String(errorStream.readAllBytes(), StandardCharsets.UTF_8);
                    LoggerUtil.logError(getClass(), "[ERROR] Yandex API Error Response:\n" + errorMessage);
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
            return new ByteArrayInputStream(os.toByteArray());
        }
    }
}
