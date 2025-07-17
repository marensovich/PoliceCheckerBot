package org.marensovich.Bot.CallbackManager.CallBacks.News;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.AdminCommands.AdminNewsCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class ConfirmHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return AdminNewsCommand.CALLBACK_CONFIRM;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        AdminNewsCommand command = (AdminNewsCommand) TelegramBot.getInstance()
                .getCommandManager()
                .getActiveCommand(userId);

        if (command != null) {
            command.handleNewsConfirm(update);
        }
    }
}
