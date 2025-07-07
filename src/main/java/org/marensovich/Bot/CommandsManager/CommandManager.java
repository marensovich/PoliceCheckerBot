package org.marensovich.Bot.CommandsManager;

import org.marensovich.Bot.CommandsManager.Commands.HelpCommand;
import org.marensovich.Bot.CommandsManager.Commands.SettingsCommand;
import org.marensovich.Bot.CommandsManager.Commands.StartCommand;
import org.marensovich.Bot.CommandsManager.Commands.TestCommand;
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
    }

    private void register(Command command) {
        commands.put(command.getName(), command);
    }

    public boolean executeCommand(Update update) {
        String text = update.getMessage().getText();
        if (text.trim().equals("/start") || text.trim().equals("/help") || text.trim().equals("/settings"))
        {
            Command command = commands.get(text);
            if (command != null) {
                command.execute(update);
                return true;
            }
        }
        return false;
    }
}
