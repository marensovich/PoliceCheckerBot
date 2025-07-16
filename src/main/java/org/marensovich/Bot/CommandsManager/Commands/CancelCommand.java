package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CancelCommand implements Command {
    @Override
    public String getName() {
        return "/cancel";
    }

    @Override
    public void execute(Update update) {
        if (TelegramBot.getInstance().getCommandManager().hasActiveCommand(update.getMessage().getFrom().getId())){
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            SendMessage msg = new SendMessage();
            msg.setChatId(update.getMessage().getChatId().toString());
            msg.setText("Активная команда была удалена.");
            try {
                TelegramBot.getInstance().execute(msg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        SendMessage msg = new SendMessage();
        msg.setChatId(update.getMessage().getChatId().toString());
        msg.setText("Активные команды с вашей стороны ботом не обрабатываются");
        try {
            TelegramBot.getInstance().execute(msg);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
