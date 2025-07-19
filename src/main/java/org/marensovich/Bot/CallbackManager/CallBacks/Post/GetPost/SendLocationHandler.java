package org.marensovich.Bot.CallbackManager.CallBacks.Post.GetPost;

import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.GetPostCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class SendLocationHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return GetPostCommand.CALLBACK_SEND_LOCATION;
    }

    @Override
    public void handle(Update update) throws TelegramApiException {
        Long userId = update.getCallbackQuery().getFrom().getId();
        GetPostCommand command = (GetPostCommand) TelegramBot.getInstance()
                .getCommandManager()
                .getActiveCommand(userId);


        String[] parts = update.getCallbackQuery().getData().split(":");
        if (parts.length < 2) {
            return; // или обработка ошибки
        }

        long postId = Long.parseLong(parts[1]);

        if (command != null) {
            command.sendPostLocation(update, postId);
        }
    }
}