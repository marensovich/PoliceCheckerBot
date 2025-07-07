package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.telegram.telegrambots.meta.api.objects.Update;

public class SettingsCommand implements Command {
    @Override
    public String getName() {
        return "/settings";
    }

    @Override
    public void execute(Update update) {

    }
}
