package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public class AdminNewsCommand implements Command {
    @Override
    public String getName() {
        return "/news";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        long[] userIds = TelegramBot.getDatabaseManager().getAllUsers();
        for (long userId : userIds) {
            System.out.println("Обработка пользователя с ID: " + userId);
        }

        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}
