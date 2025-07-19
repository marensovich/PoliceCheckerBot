package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Data.UserInfo;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapTheme;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.awt.*;

public class UserInfoCommand implements Command {
    @Override
    public String getName() {
        return "/userinfo";
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        TelegramBot.getInstance().getCommandManager().setActiveCommand(userId, this);

        DatabaseManager databaseManager = TelegramBot.getDatabaseManager();
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
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            return;
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
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
    }
}
