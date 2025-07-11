package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Data.UserInfo;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AdminUserInfoCommand implements Command {
    @Override
    public String getName() {
        return "/auserinfo";
    }

    @Override
    public void execute(Update update) {
        String[] parts = update.getMessage().getText().split(" ");
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        DatabaseManager databaseManager = new DatabaseManager();

        UserInfo userData;

        if (parts.length < 2) {
            userData = databaseManager.getUserInfo(userId);
            if (userData == null) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Ваши данные не найдены.");
                sendMessage.setParseMode("HTML");
                try {
                    TelegramBot.getInstance().execute(sendMessage);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }
        } else {
            String arg = parts[1];
            long targetUserId;
            try {
                targetUserId = Long.parseLong(arg);
            } catch (NumberFormatException e) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(chatId);
                sendMessage.setText("Некорректный ID пользователя.");
                sendMessage.setParseMode("HTML");
                try {
                    TelegramBot.getInstance().execute(sendMessage);
                } catch (TelegramApiException ex) {
                    throw new RuntimeException(ex);
                }
                return;
            }

            userData = databaseManager.getUserInfo(targetUserId);
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
        }

        String message = String.format(
                "<b>📋 Информация о пользователе:</b>\n" +
                        "<b> 🆔 ID: </b>%d\n" +
                        "<b> 🌐 Язык: </b>%s\n" +
                        "<b> 🎨 Тема: </b>%s\n" +
                        "<b> 🗺️ Тип карты: </b>%s\n" +
                        "<b> 🛡️ Админ: </b>%s\n" +
                        "<b> 🔔 Подписка: </b>%s\n" +
                        "<b> 🗺️ Генерация карты: </b>%d\n" +
                        "<b> 📝 Зарегистрирован: </b>%s\n" +
                        "<b> 💳 Тип подписки: </b>%s\n" +
                        "<b> ⏰ Истекает подписка: </b>%s",
                userData.getUserId(),
                userData.getYandexLang(),
                userData.getYandexTheme(),
                userData.getYandexMaptype(),
                userData.isAdmin() ? "Да" : "Нет",
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