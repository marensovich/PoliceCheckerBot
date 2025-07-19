package org.marensovich.Bot.CallbackManager.CallBacks.Post.GetPost;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.GetPostCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class PageInfoHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return GetPostCommand.CALLBACK_PAGE_INFO;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        return;
    }
}
