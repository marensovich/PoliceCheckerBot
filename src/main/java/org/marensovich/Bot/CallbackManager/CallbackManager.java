package org.marensovich.Bot.CallbackManager;

import org.marensovich.Bot.CallbackManager.CallBacks.CheckSubscriptionHandler;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.HashMap;
import java.util.Map;

public class CallbackManager {
    private final Map<String, TelegramCallbackHandler> handlers = new HashMap<>();

    public CallbackManager() {
        registerHandlers();
    }

    private void registerHandlers() {
        register(new CheckSubscriptionHandler());
    }

    private void register(TelegramCallbackHandler handler) {
        handlers.put(handler.getCallbackData(), handler);
    }

    public boolean handleCallback(Update update) {
        if (!update.hasCallbackQuery()) {
            return false;
        }

        String callbackData = update.getCallbackQuery().getData();
        TelegramCallbackHandler handler = handlers.get(callbackData);

        if (handler != null) {
            handler.handle(update);
            return true;
        }
        return false;
    }
}