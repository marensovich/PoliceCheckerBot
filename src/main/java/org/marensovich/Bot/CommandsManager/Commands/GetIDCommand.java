package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class GetIDCommand implements Command {
    @Override
    public String getName() {
        return "/getID";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);

        String reply = """
                Ваш ID пользователя: *@id*
                Ваш username: *@username*
                """;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(reply
                .replace("@id", update.getMessage().getFrom().getId().toString())
                .replace("@username", update.getMessage().getFrom().getUserName())
        );
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setParseMode("Markdown");
        try {
            TelegramBot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e){
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }


}
