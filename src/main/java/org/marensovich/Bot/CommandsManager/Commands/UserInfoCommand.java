package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Data.UserInfo;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UserInfoCommand implements Command {
    @Override
    public String getName() {
        return "/userinfo";
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        DatabaseManager databaseManager = new DatabaseManager();

        UserInfo userData;
        userData = databaseManager.getUserInfo(userId);
        if (userData == null) {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);
            sendMessage.setText("Пользователь с таким ID не найден.");
            sendMessage.setParseMode("HTML");
            try {
                TelegramBot.getInstance().execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }

    String message = String.format(
                "<b>📋 Информация о пользователе:</b>\n" +
                        "<b> 🆔 ID: </b>%d\n" +
                        "<b> 🌐 Язык: </b>%s\n" +
                        "<b> 🎨 Тема: </b>%s\n" +
                        "<b> 🗺️ Тип карты: </b>%s\n" +
                        "<b> 🔔 Подписка: </b>%s\n" +
                        "<b> 🗺️ Генерация карты: </b>%d\n" +
                        "<b> 📝 Зарегистрирован: </b>%s\n" +
                        "<b> 💳 Тип подписки: </b>%s\n" +
                        "<b> ⏰ Истекает подписка: </b>%s",
                userData.getUserId(),
                userData.getYandexLang(),
                userData.getYandexTheme(),
                userData.getYandexMaptype(),
                userData.getSubscribe(),
                userData.getGenMap(),
                userData.getRegistrationTime().toString(),
                (userData.getSubscribeType() != null) ? userData.getSubscribeType() : "Нет",
                (userData.getSubscriptionExpiration() != null) ? userData.getSubscriptionExpiration().toString() : "Нет"
        );

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setParseMode("HTML");
        try {
            TelegramBot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
