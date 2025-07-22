package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.LoggerUtil;
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
                
                Данный бот направлен на сбор и информирование о постах ДПС. Эффективная работа и польза бота напрямую зависит от вас. 
                
                Для ознакомления с возможностями бота вы можете использовать команду <b>/help</b>
                
                <b> Для регистрации введите /reg</b>
                
                Версия бота %version%
                """.replace("%version%", Dotenv.load().get("BOT_VERSION")));
        sendMessage.enableHtml(true);

        try {
            TelegramBot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }


}
