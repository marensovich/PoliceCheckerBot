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
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        String[] parts = update.getMessage().getText().split(" ");
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        DatabaseManager databaseManager = TelegramBot.getDatabaseManager();

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
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
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
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
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
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                return;
            }
        }

        String limitgenmap;
        if (userData.subscribe.equals("vip")){
            limitgenmap = userData.genMap + "/" + TelegramBot.getDatabaseManager().getIntValueBotData("limit_map_generation_VIP");
        } else if (userData.subscribe.equals("premium")){
            limitgenmap = userData.genMap + "/" + TelegramBot.getDatabaseManager().getIntValueBotData("limit_map_generation_PREMIUM");
        } else {
            limitgenmap = "Недоступно";
        }

        String message = String.format(
                """
              <b>📋 Информация о пользователе:</b>
              <b> 🆔 ID: </b>%s
              <b> 🌐 Язык: </b>%s
              <b> 🎨 Тема: </b>%s
              <b> 🗺️ Тип карты: </b>%s
              <b> 🔔 Подписка: </b>%s
              <b> 🗺️ Генерация карты: </b>%s
              <b> 📝 Зарегистрирован: </b>%s
              <b> 💳 Тип подписки: </b>%s
              <b> ⏰ Истекает подписка: </b>%s""",
                userData.getUserId(),
                userData.getYandexLang(),
                userData.getYandexTheme(),
                userData.getYandexMaptype(),
                userData.getSubscribe(),
                limitgenmap,
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
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}