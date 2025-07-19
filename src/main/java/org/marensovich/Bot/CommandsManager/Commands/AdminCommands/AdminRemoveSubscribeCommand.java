package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import org.jetbrains.annotations.NotNull;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
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
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
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
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        long target_id;
        try {
            target_id = Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            SendMessage errorMsg = new SendMessage();
            errorMsg.setChatId(update.getMessage().getFrom().getId());
            errorMsg.setText("ID пользователя должен быть числом. Пожалуйста, проверьте ввод.");
            errorMsg.setParseMode("Markdown");
            try {
                TelegramBot.getInstance().execute(errorMsg);
            } catch (TelegramApiException ex) {
                TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
                LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            return;
        }

        TelegramBot.getDatabaseManager().resetSub(target_id);

        SendMessage message = sendNotifMessage(target_id, reason);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e){
            TelegramBot.getInstance().sendErrorMessage(target_id, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(target_id);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }

    @NotNull
    private SendMessage sendNotifMessage(long target_id, String reason) {
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
        return message;
    }

}
