package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class AddPostCommand implements Command {

    @Override
    public String getName() {
        return "/post";
    }

    @Override
    public void execute(Update update) {
        if (update.hasMessage() && update.getMessage().hasLocation()) {
            // Если получена геопозиция
            handleReceivedLocation(update);
        } else {
            // Запрос геопозиции
            requestLocation(update);
        }
    }

    private void requestLocation(Update update) {
        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText("Пожалуйста, поделитесь вашей геопозицией:");

        // Создаем клавиатуру с кнопкой для отправки местоположения
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton locationButton = new KeyboardButton("Отправить местоположение");
        locationButton.setRequestLocation(true);
        row.add(locationButton);

        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setOneTimeKeyboard(true);

        message.setReplyMarkup(keyboardMarkup);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleReceivedLocation(Update update) {
        Location location = update.getMessage().getLocation();
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        SendMessage message = new SendMessage();
        message.setChatId(update.getMessage().getChatId().toString());
        message.setText(String.format("Ваши координаты:\nШирота: %.6f\nДолгота: %.6f", latitude, longitude));

        message.setReplyMarkup(null);

        try {
            TelegramBot.getInstance().execute(message);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}