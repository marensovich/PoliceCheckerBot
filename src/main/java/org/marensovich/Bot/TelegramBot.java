package org.marensovich.Bot;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.CommandManager;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {

    private static TelegramBot instance;
    private final DatabaseManager databaseManager;
    private final CommandManager commandManager;

    public TelegramBot(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.commandManager = new CommandManager();
        instance = this;
    }

    public static TelegramBot getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Бот не инициализирован");
        }
        return instance;
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
            if (update.getMessage().getText().startsWith("/")){
                if (!commandManager.executeCommand(update)) {
                    String text = "Команда не распознана, проверьте правильность написания команды. \n\nКоманды с доп. параметрами указаны отдельной графой в информации. Подробнее в /help.";
                    SendMessage message = new SendMessage();
                    message.setChatId(update.getMessage().getChatId().toString());
                    message.setText(text);
                    try {
                        execute(message);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
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
