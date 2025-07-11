package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class AddPostCommand implements Command {

    private static class UserSession {
        enum State {
            WAITING_FOR_LOCATION,
            WAITING_FOR_CONFIRM_LOCATION,
            WAITING_FOR_POST_TYPE,
            WAITING_FOR_CONFIRM_COMPLETE,
            COMPLETED
        }

        State currentState = State.WAITING_FOR_LOCATION;
        String location = null;
        String postType = null;
    }

    private static final Map<Long, UserSession> userSessions = new HashMap<>();

    @Override
    public String getName() {
        return "/post";
    }

    @Override
    public void execute(Update update) {
        Long userId = update.getMessage() != null ? update.getMessage().getFrom().getId()
                : update.getCallbackQuery().getFrom().getId();
        Long chatId = null;

        if (update.hasMessage()) {
            chatId = update.getMessage().getChatId();
        } else if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        }

        UserSession session = userSessions.getOrDefault(userId, new UserSession());
        userSessions.put(userId, session);

        if (update.hasMessage() && update.getMessage().isCommand() && "/post".equals(update.getMessage().getText())) {
            session = new UserSession();
            userSessions.put(userId, session);
            sendMessage(chatId, "Пожалуйста, отправьте геолокацию.");
            session.currentState = UserSession.State.WAITING_FOR_LOCATION;
            return;
        }

        if (update.hasCallbackQuery()) {
            handleCallback(update.getCallbackQuery(), userId, chatId);
            return;
        }

        switch (session.currentState) {
            case WAITING_FOR_LOCATION:
                if (update.hasMessage() && update.getMessage().hasLocation()) {
                    Location loc = update.getMessage().getLocation();
                    session.location = "Lat: " + loc.getLatitude() + ", Lon: " + loc.getLongitude();
                    session.currentState = UserSession.State.WAITING_FOR_CONFIRM_LOCATION;

                    sendMessage(chatId, "Это правильное место?", createYesNoKeyboard("location_yes_", userId));
                } else {
                    sendMessage(chatId, "Пожалуйста, отправьте геолокацию.");
                }
                break;

            case WAITING_FOR_POST_TYPE:
                String text = update.getMessage().getText();
                if ("Пост ДПС".equalsIgnoreCase(text) || "Патрулька".equalsIgnoreCase(text)) {
                    session.postType = text;
                    session.currentState = UserSession.State.WAITING_FOR_CONFIRM_COMPLETE;

                    sendMessage(chatId, "Точно все?", createYesNoKeyboard("all_yes_", userId));
                } else {
                    sendMessage(chatId, "Пожалуйста, выберите тип поста:\n- Пост ДПС\n- Патрулька");
                }
                break;
            default:
                break;
        }
    }

    private void handleCallback(CallbackQuery callback, Long userId, Long chatId) {
        String data = callback.getData();

        UserSession session = userSessions.get(userId);
        if (session == null) return;

        if (data.startsWith("location_yes_")) {
            // Пользователь подтвердил место
            session.currentState = UserSession.State.WAITING_FOR_POST_TYPE;
            sendMessage(chatId, "Выберите тип поста:\n- Пост ДПС\n- Патрулька");
        } else if (data.startsWith("location_no_")) {
            // Пользователь передумал — снова просим отправить локацию
            session.currentState = UserSession.State.WAITING_FOR_LOCATION;
            sendMessage(chatId, "Пожалуйста, отправьте геолокацию еще раз.");
        } else if (data.startsWith("all_yes_")) {
            // Пользователь подтвердил все — сохраняем и завершаем
            userSessions.remove(userId);
            // Тут можно добавить сохранение данных в БД

            sendMessage(chatId, "Данные успешно сохранены!");
        } else if (data.startsWith("all_no_")) {
            // Пользователь хочет внести изменения — начинаем заново или уточняем что именно
            UserSession newSession = new UserSession();
            userSessions.put(userId, newSession);

            sendMessage(chatId, "Повторите ввод типа поста:\n- Пост ДПС\n- Патрулька");
        }
    }

    private InlineKeyboardMarkup createYesNoKeyboard(String prefix, Long userId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        InlineKeyboardButton yesBtn = new InlineKeyboardButton();
        yesBtn.setText("Да");
        yesBtn.setCallbackData(prefix + userId);

        InlineKeyboardButton noBtn = new InlineKeyboardButton();
        noBtn.setText("Нет");
        noBtn.setCallbackData(prefix + userId);

        rows.add(Arrays.asList(yesBtn, noBtn));

        markup.setKeyboard(rows);
        return markup;
    }

    private void sendMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}