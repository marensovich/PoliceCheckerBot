package org.marensovich.Bot.UpdateManager;

import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UpdateHandler {

    public void updateHandler(Update update){

        if (update.hasMessage() || update.hasCallbackQuery()){
            if (update.hasMessage()){
                if (!TelegramBot.getDatabaseManager().checkAllUsersExists(update.getMessage().getFrom().getId())){
                    TelegramBot.getDatabaseManager().addAllUser(update.getMessage().getFrom().getId());
                }
                if (TelegramBot.getInstance().getCommandManager().hasActiveCommand(update.getMessage().getFrom().getId())){
                    TelegramBot.getInstance().getCommandManager().executeCommand(update);
                }
                if (update.getMessage().getText().startsWith("/")){
                    if (!TelegramBot.getInstance().getCommandManager().executeCommand(update)) {
                        String text = "Команда не распознана, проверьте правильность написания команды. \n\n" +
                                "Команды с доп. параметрами указаны отдельной графой в информации. Подробнее в /help.";
                        SendMessage message = new SendMessage();
                        message.setChatId(update.getMessage().getChatId().toString());
                        message.setText(text);
                        try {
                            TelegramBot.getInstance().execute(message);
                        } catch (TelegramApiException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (update.hasCallbackQuery()){
                if (!TelegramBot.getDatabaseManager().checkAllUsersExists(update.getCallbackQuery().getFrom().getId())){
                    TelegramBot.getDatabaseManager().addAllUser(update.getCallbackQuery().getFrom().getId());
                }
                TelegramBot.getInstance().getCallbackManager().handleCallback(update);
            }
        }
    }

}
