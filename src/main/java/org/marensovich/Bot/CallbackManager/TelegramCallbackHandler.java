package org.marensovich.Bot.CallbackManager;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface TelegramCallbackHandler  {
    String getCallbackData();
    void handle(Update update) throws TelegramApiException;
}