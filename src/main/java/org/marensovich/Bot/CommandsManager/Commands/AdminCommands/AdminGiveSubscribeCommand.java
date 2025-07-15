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

public class AdminGiveSubscribeCommand implements Command {
    @Override
    public String getName() {
        return "/agivesub";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        String messageText = update.getMessage().getText();
        String[] parts = messageText.split(" ");

        if (parts.length < 3) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getFrom().getId());
            message.setText("Пожалуйста, укажите ID пользователя и тип подписки. Например: " + getName() + " 12345 vip");
            message.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(message);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        if (parts[1].length() < 5) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getFrom().getId());
            message.setText("Введите корректное значение ID пользователя (длина должна быть не менее 5 символов).");
            message.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(message);
            } catch (TelegramApiException e){
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        long target_id;
        try {
            target_id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getFrom().getId());
            message.setText("ID пользователя должен быть числом. Пожалуйста, проверьте ввод.");
            message.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(message);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        String subscribeTypeStr = parts[2];
        SubscribeTypes subscribeType;
        try {
            subscribeType = SubscribeTypes.fromString(subscribeTypeStr);
        } catch (IllegalArgumentException e) {
            SendMessage message = new SendMessage();
            message.setChatId(update.getMessage().getFrom().getId());
            message.setText("Некорректный тип подписки. Пожалуйста, используйте допустимые значения.");
            message.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(message);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        DatabaseManager databaseManager = TelegramBot.getDatabaseManager();
        databaseManager.addSub(target_id, subscribeType);

        Timestamp expAt = databaseManager.getExpAtForUser(target_id);
        String formattedDate = "";

        if (expAt != null) {
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            formattedDate = df.format(expAt);
        } else {
            formattedDate = "неизвестна";
        }

        String notif = """
                🎉 Ваша подписка успешно обновлена! 🎉
                Статус вашей подписки теперь активен до *%formattedDate%*.
                Благодарим за доверие!
                Если у вас есть вопросы или потребуется помощь — обращайтесь в сообщения канала.
                """;

        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(target_id);
        replyMessage.setText(notif.replace("%formattedDate%", formattedDate));
        replyMessage.enableMarkdown(true);

        try {
            TelegramBot.getInstance().execute(replyMessage);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}
