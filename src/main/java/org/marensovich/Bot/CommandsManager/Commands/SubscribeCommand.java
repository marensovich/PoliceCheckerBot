package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
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
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);

        String reply = """
                * Здравствуйте! \uD83C\uDF89  *
                Рады представить наши подписки — *VIP* и *Premium*, которые откроют для вас новые возможности и сделают использование бота еще удобнее и приятнее. \s
                
                \uD83D\uDC8E *VIP-подписка* — всего за 499 рублей. \s
                Она даст вам доступ к созданию карт с точками, где расположены ДПС (до 5 в день), позволяя максимально эффективно использовать все функции бота и получать больше пользы. \s
                
                \uD83D\uDE80 *Premium-подписка* — всего за 999 рублей. \s
                Это расширенный пакет с [преимущества Premium], который откроет перед вами дополнительные возможности и обеспечит лучший опыт работы с ботом. \s
                
                *Для покупки обратитесь в сообщения канала. Начинайте сообщение с копирования ответа бота на команду /getID, так диалог пойдет проще и быстрее.* \s
                Выберите подходящий тариф и наслаждайтесь всеми преимуществами! Если есть вопросы — пишите, мы всегда рады помочь!""";

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        List<InlineKeyboardButton> checkRow = new ArrayList<>();
        InlineKeyboardButton checkButton = new InlineKeyboardButton();
        checkButton.setText("✔ Написать");
        checkButton.setUrl(Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_LINK"));
        checkRow.add(checkButton);
        keyboard.add(checkRow);
        keyboardMarkup.setKeyboard(keyboard);

        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(update.getMessage().getChatId().toString());
        sendPhoto.setCaption(reply);
        sendPhoto.setParseMode("Markdown");
        sendPhoto.setReplyMarkup(keyboardMarkup);
        sendPhoto.setPhoto(TelegramBot.getInstance().getPhotoFromResources("images/buy_sub_photo.jpg"));
        try {
            TelegramBot.getInstance().execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}
