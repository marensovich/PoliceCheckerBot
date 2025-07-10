package org.marensovich.Bot.CommandsManager;

import org.marensovich.Bot.CommandsManager.Commands.*;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.GiveSubcribeCommand;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();

    public CommandManager() {
        registerCommands();
    }

    private void registerCommands() {
        register(new StartCommand());
        register(new HelpCommand());
        register(new TestCommand());
        register(new SettingsCommand());
        register(new RegisterCommand());
        register(new SubscribeCommand());
        register(new GetIDCommand());

        register(new GiveSubcribeCommand());
    }

    private void register(Command command) {
        commands.put(command.getName(), command);
    }

    public boolean executeCommand(Update update) {
        String text = update.getMessage().getText();
        Command command = commands.get(text);
        if (command != null) {
            command.execute(update);
            return true;
        }
        return false;
    }
}
