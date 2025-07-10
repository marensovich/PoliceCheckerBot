package org.marensovich.Bot.CommandsManager.Commands.AdminCommands;

import org.marensovich.Bot.CommandsManager.Command;
import org.telegram.telegrambots.meta.api.objects.Update;

public class GiveSubcribeCommand implements Command {
    @Override
    public String getName() {
        return "/givesub";
    }

    @Override
    public void execute(Update update) {

    }
}
