package org.marensovich.Bot.CommandsManager.Commands;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Utils.ChangelogParser;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

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


        String changelog = "";
        try {
            changelog = ChangelogParser.fetchChangelog();
        } catch (IOException e) {
            throw new RuntimeException("Не удалось загрузить changelog", e);
        }

        String changes = ChangelogParser.getChangesForVersion(changelog, Dotenv.load().get("BOT_VERSION"));

        SendMessage info = new SendMessage();
        info.enableMarkdown(true);
        info.setChatId(update.getMessage().getFrom().getId().toString());
        info.setText(
                "Информация по последнему обновлению v." + Dotenv.load().get("BOT_VERSION") + ":\n\n" +
                changes +
                "\n\n [Подробней можете ознакомиться по ссылке](" + ChangelogParser.getChangelogUrl() + ")"
        );
        info.enableMarkdown(true);
        info.disableWebPagePreview();

        try {
            TelegramBot.getInstance().execute(sendMessage);
            TelegramBot.getInstance().execute(info);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e);
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }

        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
    }


}
