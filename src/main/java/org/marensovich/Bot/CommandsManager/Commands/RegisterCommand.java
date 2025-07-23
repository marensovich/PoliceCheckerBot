package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class RegisterCommand implements Command {

    public static final String CALLBACK_CONFIRM = "check_subscription";

    private static final Map<String, String> CHANNELS = initChannels();

    private static Map<String, String> initChannels() {
        Map<String, String> channels = new LinkedHashMap<>();
        String[] ids = Dotenv.load().get("TELEGRAM_REQUIRED_SUB_CHANNELS").split(",\\s*");
        String[] links = Dotenv.load().get("TELEGRAM_REQUIRED_SUB_CHANNELS_LINK").split(",\\s*");

        if (ids.length != links.length) {
            throw new IllegalStateException("Количество ID каналов и ссылок не совпадает");
        }

        Set<String> uniqueLinks = new HashSet<>();
        for (int i = 0; i < ids.length; i++) {
            if (!uniqueLinks.add(links[i].trim())) {
                throw new IllegalStateException("Обнаружены дублирующиеся ссылки: " + links[i]);
            }
            channels.put(ids[i].trim(), links[i].trim());
        }

        return Collections.unmodifiableMap(channels);
    }

    public static Map<String, String> getChannels() {
        return CHANNELS;
    }

    public static String getChannelLink(String channelId) {
        return CHANNELS.get(channelId);
    }

    @Override
    public String getName() {
        return "/reg";
    }

    @Override
    public void execute(Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = update.getMessage().getFrom().getId();

        TelegramBot.getInstance().getCommandManager().setActiveCommand(userId, this);

        try {
            boolean isSubscribedToAll = true;
            for (String channelId : CHANNELS.keySet()) {
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(channelId);
                getChatMember.setUserId(userId);
                ChatMember userMember = TelegramBot.getInstance().execute(getChatMember);

                if (!isMember(userMember.getStatus())) {
                    isSubscribedToAll = false;
                    break;
                }
            }

            if (isSubscribedToAll) {
                registerUser(userId, chatId);
            } else {
                sendSubscriptionRequest(chatId);
            }
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
            throw new RuntimeException(e);
        }
    }

    private boolean isMember(String status) {
        return "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
    }

    private void sendSubscriptionRequest(Long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText("📢 *Требуется подписка*\n\n" +
                "Для продолжения работы с ботом необходимо подписаться на наши каналы.\n" +
                "После подписки нажмите кнопку *\"Проверить подписку\"*");
        message.setParseMode("Markdown");

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map.Entry<String, String> entry : CHANNELS.entrySet()) {
            List<InlineKeyboardButton> channelRow = new ArrayList<>();
            InlineKeyboardButton channelButton = new InlineKeyboardButton();
            channelButton.setText("Подписаться на канал " + getChannelName(entry.getKey()));
            channelButton.setUrl(entry.getValue());
            channelRow.add(channelButton);
            keyboard.add(channelRow);
        }

        List<InlineKeyboardButton> checkRow = new ArrayList<>();
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText("Проверить подписку ✅");
        checkButton.setCallbackData(CALLBACK_CONFIRM);
        checkRow.add(checkButton);
        keyboard.add(checkRow);

        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(chatId);
            throw new RuntimeException(e);
        }
    }

    public String getChannelName(String channelId) {
        try {
            GetChat getChat = new GetChat();
            getChat.setChatId(channelId);
            Chat chat = TelegramBot.getInstance().execute(getChat);
            return chat.getTitle();
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Не удалось получить имя канала: " + e.getMessage());
            return "наш канал";
        }
    }

    public void registerUser(Long userId, Long chatId) {
        try {
            if (TelegramBot.getDatabaseManager().checkUsersExists(userId)) {
                sendSuccessMessage(chatId, "ℹ️ Вы уже зарегистрированы!");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            } else {
                TelegramBot.getDatabaseManager().addUser(userId);
                sendSuccessMessage(chatId, "<b>✅ Регистрация прошла успешно!</b>\nТеперь вам открыты часть функций бота, подробнее в /help.");
                TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            }
        } catch (Exception e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
            
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            throw new RuntimeException(e);
        }
    }

    private void sendSuccessMessage(Long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.enableHtml(true);
        TelegramBot.getInstance().execute(message);
    }
}