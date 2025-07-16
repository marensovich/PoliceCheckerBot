package org.marensovich.Bot.CallbackManager;

import org.marensovich.Bot.CallbackManager.CallBacks.AddPost.*;
import org.marensovich.Bot.CallbackManager.CallBacks.CheckSubscriptionHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Lang.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Maptype.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Theme.ThemeDarkHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Theme.ThemeLightHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Theme.ThemeMenuHandler;
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
        //Обработчик команды /reg
        register(new CheckSubscriptionHandler());

        // Обработчики команды /post
        register(new PostCancelHandler());
        register(new PostConfirmHandler());
        register(new PostDPSHandler());
        register(new PostPatrolHandler());
        register(new NoCommentHandler());

        // Обработчики команды /settings
        register(new BackHandler());
        register(new SaveHandler());
        register(new QuitHandler());

        register(new ThemeDarkHandler());
        register(new ThemeLightHandler());

        register(new MaptypeAdminHandler());
        register(new MaptypeDrivingHandler());
        register(new MaptypeTransitHandler());
        register(new MaptypeMapHandler());

        register(new LangRuRuHandler());
        register(new LangEnUsHandler());
        register(new LangUkUaHandler());
        register(new LangTrTrHandler());

        register(new ThemeMenuHandler());
        register(new MaptypeMenuHandler());
        register(new LangMenuHandler());

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