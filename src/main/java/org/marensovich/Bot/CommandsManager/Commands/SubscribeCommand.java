package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class SubscribeCommand implements Command {
    @Override
    public String getName() {
        return "/subscribe";
    }

    @Override
    public void execute(Update update) {

        String reply = "* Здравствуйте! \uD83C\uDF89  *\n" +
                "Рады представить наши подписки — *VIP* и *Premium*, которые откроют для вас новые возможности и сделают использование бота еще удобнее и приятнее.  \n" +
                "\n" +
                "\uD83D\uDC8E *VIP-подписка* — всего за 499 рублей.  \n" +
                "Она даст вам доступ к [преимущества VIP], позволяя максимально эффективно использовать все функции бота и получать больше пользы.  \n" +
                "\n" +
                "\uD83D\uDE80 *Premium-подписка* — всего за 999 рублей.  \n" +
                "Это расширенный пакет с [преимущества Premium], который откроет перед вами дополнительные возможности и обеспечит лучший опыт работы с ботом.  \n" +
                "\n" +
                "*Для покупки подписки свяжитесь с пользователем: @marensovich. Начинайте сообщение с пересылки ответа бота на команду /getID, так диалог пойдет проще и быстрее.*  \n" +
                "Выберите подходящий тариф и наслаждайтесь всеми преимуществами! Если есть вопросы — пишите, мы всегда рады помочь!";

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setParseMode("Markdown");
        sendMessage.setText(reply);


        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> checkRow = new ArrayList<>();
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText("✔ Написать");
        checkButton.setUrl("https://t.me/marensovich");
        checkRow.add(checkButton);
        keyboard.add(checkRow);

        keyboardMarkup.setKeyboard(keyboard);
        sendMessage.setReplyMarkup(keyboardMarkup);

        try {
            TelegramBot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
