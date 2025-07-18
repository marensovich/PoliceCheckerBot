package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GetPostCommand implements Command {
    @Override
    public String getName() {
        return "/getpost";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("/getpost executed");
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }

    public void executeLocation(Update update, Location location) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getFrom().getId());
        message.setText("/getpost executed");
        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e){
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}
