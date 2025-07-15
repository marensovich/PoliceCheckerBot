package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class AdminRemoveSubscribeCommand implements Command {
    @Override
    public String getName() {
        return "/adelsub";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        String messageText = update.getMessage().getText();
        String[] parts = messageText.split(" ", 3);

        if (parts.length < 3) {
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(update.getMessage().getFrom().getId());
            errorMsg.setText("Пожалуйста, укажите ID пользователя и причину. Например:\n" +
                    getName() + " 12345 Причина");
            errorMsg.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(errorMsg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        String idStr = parts[1];
        String reason = parts[2];

        if (idStr.length() < 5) {
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(update.getMessage().getFrom().getId());
            errorMsg.setText("Введите корректное значение ID пользователя (длина должна быть не менее 5 символов).");
            errorMsg.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(errorMsg);
            } catch (TelegramApiException e){
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        long target_id;
        try {
            target_id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(update.getMessage().getFrom().getId());
            errorMsg.setText("ID пользователя должен быть числом. Пожалуйста, проверьте ввод.");
            errorMsg.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(errorMsg);
            } catch (TelegramApiException ex) {
                throw new RuntimeException(ex);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        TelegramBot.getDatabaseManager().resetSub(target_id);

        String notif = """
                *\uD83D\uDEAB Ваша подписка была отменена или досрочно завершена администратором.*
                *Причина:* %reason%.
                Благодарим за доверие!
                Если у вас есть вопросы или хотите узнать подробности, пожалуйста, обратитесь к администратору в сообщения канала.
                """;

        SendMessage message = new SendMessage();
        message.setChatId(target_id);
        message.setText(notif.replace("%reason%", reason));
        message.enableMarkdown(true);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}
