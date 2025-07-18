package org.marensovich.Bot.CallbackManager.CallBacks.Post.GetPost;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.GetPostCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BackToPageHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return GetPostCommand.CALLBACK_BACK_TO_LIST ;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        GetPostCommand command = (GetPostCommand) TelegramBot.getInstance()
                .getCommandManager()
                .getActiveCommand(userId);

        if (command != null) {
            command.handleBackToList(update);
        }
    }
}