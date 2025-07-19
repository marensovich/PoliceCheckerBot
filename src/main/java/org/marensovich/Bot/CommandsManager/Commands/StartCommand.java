package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class StartCommand implements Command {
    @Override
    public String getName() {
        return "/start";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText("""
                <b> Добро пожаловать. </b> 
                
                Данный бот направлен на сбор и информирование о постах ДПС. 
                Эффективная работа и польза бота напрямую зависит от вас. С возможностями бота вы можете ознакомиться в /help
                """);
        sendMessage.enableHtml(true);
        try {
            TelegramBot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }
}
