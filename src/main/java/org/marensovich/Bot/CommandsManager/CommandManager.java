package org.marensovich.Bot.CommandsManager;

import org.marensovich.Bot.CommandsManager.Commands.*;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminGiveSubscribeCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminRemoveSubscribeCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminUserInfoCommand;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, Command> adminCommands = new HashMap<>();
    private final Map<Long, Command> activeCommands = new HashMap<>();

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
        register(new UserInfoCommand());
        register(new AddPostCommand());
        register(new CancelCommand());

        registerAdmin(new AdminGiveSubscribeCommand());
        registerAdmin(new AdminRemoveSubscribeCommand());
        registerAdmin(new AdminUserInfoCommand());
    }

    private void register(Command command) {
        commands.put(command.getName(), command);
    }

    private void registerAdmin(Command command) {
        adminCommands.put(command.getName(), command);
    }

    public boolean executeCommand(Update update) {
        long userId = update.getMessage().getFrom().getId();

        if (update.hasMessage() && !update.getMessage().hasText()) {
            Command activeCommand = activeCommands.get(userId);
            activeCommand.execute(update);
            return true;

        } else {
            if (hasActiveCommand(userId)) {
                String reply = """
                        Бот обрабатывает отправленную вами команду %command%
        
                        В случае если это вы хотите прекратить выполнение команды - отправьте /cancel
                        """;
                SendMessage msg = new SendMessage();
                msg.setChatId(update.getMessage().getChatId());
                msg.setText(reply.replace("%command%", getActiveCommand(userId).getName()));
                try {
                    TelegramBot.getInstance().execute(msg);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        String messageText = update.getMessage().getText().trim();
        String[] parts = messageText.split(" ");
        String commandKey = parts[0];

        DatabaseManager databaseManager = TelegramBot.getDatabaseManager();

        boolean isRegistered = databaseManager.checkUsersExists(userId);

        if (!isRegistered && !commandKey.equals("/start") && !commandKey.equals("/help") && !commandKey.equals("help") && !commandKey.equals("/cancel")) {
            SendMessage msg = new SendMessage();
            msg.setChatId(update.getMessage().getChatId());
            msg.setText("Пожалуйста, зарегистрируйтесь для использования этой команды. Используйте /reg для регистрации.");
            try {
                TelegramBot.getInstance().execute(msg);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return true;
        }
        if (adminCommands.containsKey(commandKey)) {
            if (!databaseManager.checkUserIsAdmin(userId)) {
                SendMessage sendMessage = new SendMessage();
                sendMessage.setText("У вас нет прав доступа к этой команде!");
                sendMessage.setChatId(update.getMessage().getChatId());
                try {
                    TelegramBot.getInstance().execute(sendMessage);
                } catch (TelegramApiException e){
                    throw new RuntimeException(e);
                }
                return true;
            }
            Command command = adminCommands.get(commandKey);
            if (command != null) {
                command.execute(update);
                return true;
            }
            return false;
        }
        Command command = commands.get(commandKey);
        if (command != null) {
            command.execute(update);
            return true;
        } else {
            return false;
        }
    }

    public void setActiveCommand(Long userId, Command command) {
        System.out.println("Команда " + command.getName() + " закреплена за пользователем " + userId);
        activeCommands.put(userId, command);
    }

    public void unsetActiveCommand(Long userId) {
        System.out.println("Команда, закрепленная за пользователем " + userId + " удалена");
        activeCommands.remove(userId);
    }

    public boolean hasActiveCommand(Long userId) {
        return activeCommands.containsKey(userId);
    }

    public Command getActiveCommand(Long userId) {
        return activeCommands.get(userId);
    }

}