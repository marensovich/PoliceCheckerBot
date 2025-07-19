package org.marensovich.Bot.CallbackManager.CallBacks.Post.AddPost;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.AddPostCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PostPatrolHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return AddPostCommand.CALLBACK_PATROL;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        String callbackData = update.getCallbackQuery().getData();
        if (callbackData.equals(AddPostCommand.CALLBACK_PATROL)) {

            Long userId = update.getCallbackQuery().getFrom().getId();
            AddPostCommand command = (AddPostCommand) TelegramBot.getInstance()
                    .getCommandManager()
                    .getActiveCommand(userId);

            if (command != null) {
                command.handlePostType(update, callbackData);
            }
        }
    }
}