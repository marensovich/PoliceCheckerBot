package org.marensovich.Bot.CallbackManager;

import org.telegram.telegrambots.meta.api.objects.Update;

public interface TelegramCallbackHandler  {
    String getCallbackData();
    void handle(Update update);
}