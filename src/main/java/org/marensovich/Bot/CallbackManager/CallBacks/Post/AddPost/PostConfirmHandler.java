package org.marensovich.Bot.CallbackManager.CallBacks.Post.AddPost;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.AddPostCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PostConfirmHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return AddPostCommand.CALLBACK_CONFIRM;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        AddPostCommand command = (AddPostCommand) TelegramBot.getInstance()
                .getCommandManager()
                .getActiveCommand(userId);

        if (command != null) {
            command.handlePostConfirm(update);
        }
    }
}