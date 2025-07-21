package org.marensovich.Bot.CommandsManager;

import org.marensovich.Bot.CommandsManager.Commands.*;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminGiveSubscribeCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminNewsCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminRemoveSubscribeCommand;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminUserInfoCommand;
import org.marensovich.Bot.DatabaseManager;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CommandManager {
    /**
     * Обьект для хранения всех команд
     */
    private final Map<String, Command> commands = new HashMap<>();
    /**
     * Обьект для хранения админ команд
     */
    private final Map<String, Command> adminCommands = new HashMap<>();
    /**
     * Обьект для хранения активных команд пользователей
     */
    private final Map<Long, Command> activeCommands = new HashMap<>();

    public CommandManager() {
        registerCommands();
    }

    /**
     * Регистрация команд
     */
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
        register(new GetPostCommand());

        registerAdmin(new AdminGiveSubscribeCommand());
        registerAdmin(new AdminRemoveSubscribeCommand());
        registerAdmin(new AdminUserInfoCommand());
        registerAdmin(new AdminNewsCommand());
    }

    /**
     * Регистрация пользовательской команды
     * @param command
     */
    private void register(Command command) {
        commands.put(command.getName(), command);
    }

    /**
     * Регистрация админ-команды
     * @param command
     */
    private void registerAdmin(Command command) {
        adminCommands.put(command.getName(), command);
    }

    /**
     * Исполнение команд
     * @param update
     * @return
     */
    public boolean executeCommand(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText() && !update.getMessage().hasLocation()) {
            return false;
        }

        long userId = update.getMessage().getFrom().getId();

        // 1. Сначала проверяем активные команды
        if (hasActiveCommand(userId)) {
            Command activeCommand = activeCommands.get(userId);

            // 1.1. Обработка локации для активных команд
            if (update.getMessage().hasLocation()) {
                if (activeCommand instanceof AddPostCommand) {
                    ((AddPostCommand) activeCommand).execute(update);
                    return true;
                } else if (activeCommand instanceof GetPostCommand) {
                    ((GetPostCommand) activeCommand).executeLocation(update, update.getMessage().getLocation());
                    return true;
                }
            }

            // 1.2. Обработка текста для активных команд
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();

                // Обработка команды /cancel
                if (text.equals("/cancel")) {
                    new CancelCommand().execute(update);
                    return true;
                }

                // Обработка в зависимости от типа активной команды
                if (activeCommand instanceof AddPostCommand) {
                    AddPostCommand addPostCommand = (AddPostCommand) activeCommand;
                    if (addPostCommand.getUserState(userId).isAwaitingComment()) {
                        activeCommand.execute(update);
                        return true;
                    }
                } else if (activeCommand instanceof GetPostCommand) {
                    GetPostCommand getPostCommand = (GetPostCommand) activeCommand;
                    if (getPostCommand.getUserState(userId).isAwaitingLocation()) {
                        activeCommand.execute(update);
                        return true;
                    }
                } else if (activeCommand instanceof AdminNewsCommand) {
                    AdminNewsCommand adminNewsCommand = (AdminNewsCommand) activeCommand;
                    if (adminNewsCommand.getUserState(userId).isAwaitingText()) {
                        activeCommand.execute(update);
                        return true;
                    }
                }

                // Если активная команда, но не обработали текст выше
                sendActiveCommandMessage(update.getMessage().getChatId(), activeCommand.getName());
                return true;
            }

            return true;
        }

        // 2. Обработка локации без активной команды
        if (update.getMessage().hasLocation() && !hasActiveCommand(userId)) {
            new GetPostCommand().executeLocation(update, update.getMessage().getLocation());
            return true;
        }

        // 3. Обработка текстовых команд
        if (update.getMessage().hasText()) {
            String messageText = update.getMessage().getText().trim();
            String[] parts = messageText.split(" ");
            String commandKey = parts[0];

            // Проверка регистрации пользователя
            DatabaseManager databaseManager = TelegramBot.getDatabaseManager();
            boolean isRegistered = databaseManager.checkUsersExists(userId);

            if (!isRegistered && !isAllowedUnauthorizedCommand(commandKey)) {
                sendMessage(update.getMessage().getChatId(),
                        "\uD83D\uDCE8 Пожалуйста, зарегистрируйтесь для использования этой команды. Используйте /reg для регистрации.");
                return true;
            }

            // Проверка админских команд
            if (adminCommands.containsKey(commandKey)) {
                if (!databaseManager.checkUserIsAdmin(userId)) {
                    sendMessage(update.getMessage().getChatId(), "У вас нет прав доступа к этой команде!");
                    return true;
                }
                Command command = adminCommands.get(commandKey);
                command.execute(update);
                return true;
            }

            // Обработка обычных команд
            if (commands.containsKey(commandKey)) {
                commands.get(commandKey).execute(update);
                return true;
            }
        }

        return false;
    }

    /**
     * Вспомогательные методы
     * @param command
     * @return
     */
    private boolean isAllowedUnauthorizedCommand(String command) {
        return command.equals("/start") || command.equals("/help") ||
                command.equals("help") || command.equals("/cancel") ||
                command.equals("/reg");
    }

    private void sendActiveCommandMessage(Long chatId, String commandName) {
        String reply = """
        Бот обрабатывает отправленную вами команду %command%
        
        В случае если это вы хотите прекратить выполнение команды - отправьте /cancel
        """;
        sendMessage(chatId, reply.replace("%command%", commandName));
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(chatId.toString());
        msg.setText(text);
        try {
            TelegramBot.getInstance().execute(msg);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Установка активной команды для пользователя
     * @param userId
     * @param command
     */
    public void setActiveCommand(Long userId, Command command) {
        LoggerUtil.logInfo(getClass(), "Команда " + command.getName() + " закреплена за пользователем " + userId);
        activeCommands.put(userId, command);
    }

    /**
     * Удаления активной команды для пользователя
     * @param userId
     */
    public void unsetActiveCommand(Long userId) {
        LoggerUtil.logInfo(getClass(), "Команда, закрепленная за пользователем " + userId + " удалена");
        activeCommands.remove(userId);
    }

    /**
     * Проверка наличия активной команды пользователя
     * @param userId
     * @return
     */
    public boolean hasActiveCommand(Long userId) {
        return activeCommands.containsKey(userId);
    }

    /**
     * Получения активной команды пользователя
     * @param userId
     * @return
     */
    public Command getActiveCommand(Long userId) {
        return activeCommands.get(userId);
    }

}