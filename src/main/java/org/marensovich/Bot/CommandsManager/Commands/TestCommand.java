package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.*;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexMaps;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.InputStream;

public class TestCommand implements Command {
    @Override
    public String getName() {
        return "/test";
    }

    @Override
    public void execute(Update update) {
        try {
            YandexMaps yandexMaps = new YandexMaps();
            InputStream is = yandexMaps.getPhoto(36.946066f, 55.389249f, 0.05f, null,
                    18, YandexMapSize.Large, YandexMapScale.SCALE_1, null, null,
                    YandexMapLanguage.ru_RU, null, YandexMapTheme.dark, YandexMapTypes.transit);

            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(update.getMessage().getChatId().toString());
            sendPhoto.setPhoto(new InputFile(is, "map.png"));
            TelegramBot.getInstance().execute(sendPhoto);

        } catch (Exception e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            LoggerUtil.logError(getClass(), "[FATAL] Error processing map request:");
            e.printStackTrace();

            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(update.getMessage().getChatId().toString());
            errorMsg.setText("❌ Ошибка при получении карты:\n" + e.getMessage());
            try {
                TelegramBot.getInstance().execute(errorMsg);
            } catch (TelegramApiException ex) {
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + ex.getMessage());
                e.printStackTrace();
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                throw new RuntimeException(ex);
            }
        }
    }
}

