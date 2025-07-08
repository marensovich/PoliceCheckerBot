package org.marensovich.Bot.CommandsManager;

import org.marensovich.Bot.CommandsManager.Commands.*;
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
    }

    private void register(Command command) {
        commands.put(command.getName(), command);
    }

    public boolean executeCommand(Update update) {
        String text = update.getMessage().getText();
        if (text.trim().equals("/start") || text.trim().equals("/help") || text.trim().equals("/settings") || text.trim().equals("/reg") || text.trim().equals("/test"))
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
