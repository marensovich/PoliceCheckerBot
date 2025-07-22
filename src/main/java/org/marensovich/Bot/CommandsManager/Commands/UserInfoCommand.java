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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UserInfoCommand implements Command {

    record LangOption(String displayName, String apiValue) {}
    private static final List<LangOption> LANGUAGES = List.of(
            new LangOption("Русский язык", "ru_RU"),
            new LangOption("Английский язык", "en_US"),
            new LangOption("Украинский язык", "uk_UA"),
            new LangOption("Турецкий язык", "tr_TR")
    );

    record MapOption(String displayName, String apiValue) {}
    private static final List<MapOption> MAP_TYPES = List.of(
            new MapOption("Обычная карта", "map"),
            new MapOption("Транспортная карта", "transit"),
            new MapOption("Автомобильная карта", "driving"),
            new MapOption("Административная карта", "admin")
    );

    record ThemeOption(String displayName, String apiValue) {}
    private static final List<ThemeOption> THEMES = List.of(
            new ThemeOption("Темная", "dark"),
            new ThemeOption("Светлая", "light")
    );

    record SubscribeOption(String displayName, String apiValue) {}
    private static final List<SubscribeOption> SUBSCRIBE_OPTIONS = List.of(
            new SubscribeOption("Нет", "none"),
            new SubscribeOption("VIP", "vip"),
            new SubscribeOption("PREMIUM", "premium")
    );

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
        } else if (userData.subscribe.equals("none")){
            limitgenmap = userData.genMap + "/" + TelegramBot.getDatabaseManager().getIntValueBotData("limit_map_generation_NONE");;
        } else {
            limitgenmap = "Недоступно";
        }

        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyyy")
                .withZone(moscowZone);

        String message = String.format(
                          """
                        <b>📋 Информация о пользователе:</b>
                        
                        <b> 🆔 ID: </b>%s
                        <b> 🌐 Язык: </b>%s
                        <b> 🎨 Тема: </b>%s
                        <b> 🗺️ Тип карты: </b>%s
                        <b> 🔔 Подписка: </b>%s
                        <b> ⏰ Подписка истекает: </b>%s
                        <b> 🗺️ Генерация карты: </b>%s (Обнуление счетчика в 00:00 по МСК)
                        <b> 📝 Зарегистрирован: </b>%s""",
                userData.getUserId(),
                LANGUAGES.stream()
                        .filter(option -> option.apiValue().equals(userData.getYandexLang()))
                        .findFirst()
                        .map(LangOption::displayName)
                        .orElse(userData.getYandexLang()),
                THEMES.stream()
                        .filter(option -> option.apiValue().equals(userData.getYandexTheme()))
                        .findFirst()
                        .map(ThemeOption::displayName)
                        .orElse(userData.getYandexTheme()),
                MAP_TYPES.stream()
                        .filter(option -> option.apiValue().equals(userData.getYandexMaptype()))
                        .findFirst()
                        .map(MapOption::displayName)
                        .orElse(userData.getYandexMaptype()),
                SUBSCRIBE_OPTIONS.stream()
                        .filter(option -> option.apiValue().equals(userData.getSubscribe()))
                        .findFirst()
                        .map(SubscribeOption::displayName)
                        .orElse(userData.getSubscribe()),
                (userData.getSubscriptionExpiration() != null)
                        ? userData.getSubscriptionExpiration().toLocalDateTime().format(formatter) +
                        formatTimeRemaining(userData.getSubscriptionExpiration().toLocalDateTime())
                        : "Нет",
                limitgenmap,
                userData.getRegistrationTime().toInstant()
                        .atZone(moscowZone)
                        .format(formatter)
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


    private String formatTimeRemaining(LocalDateTime expirationDate) {
        Duration remaining = Duration.between(LocalDateTime.now(), expirationDate);
        long days = remaining.toDays();
        long hours = remaining.toHours() % 24;

        if (days > 0 && hours > 0) {
            return String.format(" (через %d %s и %d %s)",
                    days, pluralizeDays(days),
                    hours, pluralizeHours(hours));
        } else if (days > 0) {
            return String.format(" (через %d %s)",
                    days, pluralizeDays(days));
        } else if (hours > 0) {
            return String.format(" (через %d %s)",
                    hours, pluralizeHours(hours));
        } else {
            return " (истекает сегодня)";
        }
    }

    private String pluralizeDays(long days) {
        if (days % 10 == 1 && days % 100 != 11) return "день";
        if (days % 10 >= 2 && days % 10 <= 4 && (days % 100 < 10 || days % 100 >= 20)) return "дня";
        return "дней";
    }

    private String pluralizeHours(long hours) {
        if (hours % 10 == 1 && hours % 100 != 11) return "час";
        if (hours % 10 >= 2 && hours % 10 <= 4 && (hours % 100 < 10 || hours % 100 >= 20)) return "часа";
        return "часов";
    }

}
