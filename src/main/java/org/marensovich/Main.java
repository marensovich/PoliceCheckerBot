package org.marensovich;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.DailyGenMapReset;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


public class Main {
    public static void main(String[] args) {
        try {
            System.setOut(new PrintStream(System.out, true, "UTF-8"));

            DatabaseManager databaseManager = new DatabaseManager();
            if (!databaseManager.testConnection()){
                LoggerUtil.logInfo(Main.class, "Не удалось подключиться к базе данных!");
                System.exit(1);
            }

            LoggerUtil.logInfo(Main.class, "Подключение к базе данных успешно! Бот " + Dotenv.load().get("TELEGRAM_BOT_USERNAME") + " успешно запущен!");
            databaseManager.initializeDatabase();

            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(new TelegramBot(databaseManager));

            new DailyGenMapReset().startDailyReset();

        } catch (UnsupportedEncodingException | TelegramApiException e) {
            throw new RuntimeException("Ошибка запуска бота", e);
        }
    }
}