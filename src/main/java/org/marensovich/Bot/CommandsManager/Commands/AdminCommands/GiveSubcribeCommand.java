package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.Data.SubscribeTypes;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class GiveSubcribeCommand implements Command {
    @Override
    public String getName() {
        return "/givesub";
    }

    @Override
    public void execute(Update update) {
        String[] parts = update.getMessage().getText().split(" ");
        if (parts[1].length() < 5) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getFrom().getId());
            message.setText("Введите корректное значение ID пользователя.");
            message.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(message);
            } catch (TelegramApiException e){
                throw new RuntimeException(e);
            }
        }
        long target_id = Long.parseLong(parts[1]);
        String sub = parts[2];

        DatabaseManager databaseManager = new DatabaseManager();
        databaseManager.addSub(target_id, SubscribeTypes.fromString(parts[2]));

        Timestamp expAt = databaseManager.getExpAtForUser(target_id);
        String formattedDate = "";

        if (expAt != null) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            formattedDate = df.format(expAt);
        } else {
            formattedDate = "неизвестна";
        }
        String notif = "🎉 Ваша подписка успешно обновлена! 🎉\n\n" +
                "Статус вашей подписки теперь активен до *" + formattedDate + "*.\n\n" +
                "Благодарим за доверие!\n" +
                "Если у вас есть вопросы или потребуется помощь — обращайтесь к @marensovich";
        SendMessage message = new SendMessage();
        message.setChatId(target_id);
        message.setText(notif);
        message.enableMarkdown(true);
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
    }
}
