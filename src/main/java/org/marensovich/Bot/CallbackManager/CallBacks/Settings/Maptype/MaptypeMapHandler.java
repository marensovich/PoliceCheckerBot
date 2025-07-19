package org.marensovich.Bot.CallbackManager.CallBacks.Settings.Maptype;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.SettingsCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MaptypeMapHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return SettingsCommand.CALLBACK_MAPTYPE_MAP;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        SettingsCommand command = (SettingsCommand) TelegramBot.getInstance()
                .getCommandManager()
                .getActiveCommand(userId);

        if (command != null) {
            command.handleOptionSelected(update, "maptype", "map");
        }
    }
}