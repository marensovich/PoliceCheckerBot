package org.marensovich.Bot.CallbackManager.CallBacks.Help;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.HelpCommand;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class HelpHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return HelpCommand.CALLBACK_HELP;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        HelpCommand helpCommand = new HelpCommand();
        helpCommand.handleHelpCallback(update);
    }
}
