package org.marensovich.Bot.CallbackManager;

import org.marensovich.Bot.CallbackManager.CallBacks.CheckSubscriptionHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.News.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Post.AddPost.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Post.GetPost.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Lang.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Maptype.*;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Theme.ThemeDarkHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Theme.ThemeLightHandler;
import org.marensovich.Bot.CallbackManager.CallBacks.Settings.Theme.ThemeMenuHandler;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.Map;

/**
 * Обработчик callback
 */
public class CallbackManager {
    /**
     * Хранение callback для чистого сравнения
     */
    private final Map<String, TelegramCallbackHandler> handlers = new HashMap<>();
    /**
     * Хранение callback для сравнения по префиксу
     */
    private final Map<String, TelegramCallbackHandler> prefixHandlers = new HashMap<>();

    public CallbackManager() {
        registerHandlers();
    }

    /**
     * Регистрация callback
     */
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


        // Обработчики команды /news
        register(new CancelHandler());
        register(new ConfirmHandler());
        register(new Type1Handler());
        register(new Type2Handler());
        register(new Type3Handler());


        // Обработчики для /getpost
        register(new PageInfoHandler());
        register(new PhotoHandler());
        registerPrefix(new NextPageHandler());
        registerPrefix(new BackPageHandler());
        registerPrefix(new PostDetailHandler());
        registerPrefix(new BackToPageHandler());
        registerPrefix(new SendLocationHandler());

    }

    /**
     * Регистрация callback - префиксов
     * @param handler
     */
    private void registerPrefix(TelegramCallbackHandler handler) { prefixHandlers.put(handler.getCallbackData(), handler); }

    /**
     * Регистрация callback
     * @param handler
     */
    private void register(TelegramCallbackHandler handler) {
        handlers.put(handler.getCallbackData(), handler);
    }

    /**
     * Обработка callback`ов
     * @param update
     * @return
     * @throws TelegramApiException
     */
    public boolean handleCallback(Update update) throws TelegramApiException {
        if (!update.hasCallbackQuery()) {
            return false;
        }
        String callbackData = update.getCallbackQuery().getData();
        /**
         * Обработка callback для прямого сравнения
         */
        TelegramCallbackHandler handler = handlers.get(callbackData);
        if (handler != null) {
            handler.handle(update);
            return true;
        }

        /**
         * Обработка callback для сравнения по префиксу
         */
        for (Map.Entry<String, TelegramCallbackHandler> entry : prefixHandlers.entrySet()) {
            if (callbackData.startsWith(entry.getKey())) {
                entry.getValue().handle(update);
                return true;
            }
        }

        LoggerUtil.logInfo(getClass(), "No handler found for: " + callbackData);
        return false;
    }
}