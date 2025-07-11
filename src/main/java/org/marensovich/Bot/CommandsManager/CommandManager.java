package org.marensovich.Bot.CommandsManager;

import org.marensovich.Bot.CommandsManager.Commands.*;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.GiveSubscribeCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.RemoveSubscribeCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.UserInfoCommand;
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

        register(new GiveSubscribeCommand());
        register(new RemoveSubscribeCommand());
        register(new UserInfoCommand());
    }

    private void register(Command command) {
        commands.put(command.getName(), command);
    }

    public boolean executeCommand(Update update) {
        if (update.getMessage().getText().contains(" ")){
            String[] text = update.getMessage().getText().split(" ");
            if (text[0].equals("/givesub") ||
                text[0].equals("/delsub") ||
                text[0].equals("/userinfo")){
                Command command = commands.get(text[0]);
                if (command != null) {
                    command.execute(update);
                    return true;
                }
                return false;
            }
        }
        String text = update.getMessage().getText();
        Command command = commands.get(text);
        if (command != null) {
            command.execute(update);
            return true;
        }
        return false;
    }
}
