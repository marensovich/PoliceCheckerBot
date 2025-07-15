package org.marensovich.Bot.CallbackManager;

import org.marensovich.Bot.CallbackManager.CallBacks.AddPost.PostCancelHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.AddPost.PostConfirmHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.AddPost.PostDPSHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.AddPost.PostPatrolHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.CheckSubscriptionHandler;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

public class CallbackManager {
    private final Map<String, TelegramCallbackHandler> handlers = new HashMap<>();

    public CallbackManager() {
        registerHandlers();
    }

    private void registerHandlers() {
        register(new CheckSubscriptionHandler());
        register(new PostCancelHandler());
        register(new PostConfirmHandler());
        register(new PostDPSHandler());
        register(new PostPatrolHandler());
    }

    private void register(TelegramCallbackHandler handler) {
        handlers.put(handler.getCallbackData(), handler);
    }

    public boolean handleCallback(Update update) throws TelegramApiException {
        if (!update.hasCallbackQuery()) {
            return false;
        }

        String callbackData = update.getCallbackQuery().getData();
        TelegramCallbackHandler handler = handlers.get(callbackData);

        if (handler != null) {
            handler.handle(update);
            return true;
        }

        System.out.println("No handler found for: " + callbackData);
        return false;
    }
}