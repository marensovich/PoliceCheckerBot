package org.marensovich.Bot.CommandsManager;

import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Интерфейс команды
 */
public interface Command {
    String getName();
    void execute(Update update);
}
