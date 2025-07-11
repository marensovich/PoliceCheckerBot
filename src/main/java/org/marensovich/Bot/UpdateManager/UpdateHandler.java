package org.marensovich.Bot.UpdateManager;

import org.marensovich.Bot.CallbackManager.CallbackManager;
import org.marensovich.Bot.CommandsManager.CommandManager;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UpdateHandler {

    public void updateHandler(Update update){

        CommandManager commandManager = new CommandManager();
        CallbackManager callbackManager = new CallbackManager();


        if (update.hasMessage() || update.hasCallbackQuery()){
            if (update.hasMessage()){
                if (!TelegramBot.getDatabaseManager().checkAllUsersExists(update.getMessage().getFrom().getId())){
                    TelegramBot.getDatabaseManager().addAllUser(update.getMessage().getFrom().getId());
                }
                if (update.getMessage().getText().startsWith("/")){
                    if (!commandManager.executeCommand(update)) {
                        String text = "Команда не распознана, проверьте правильность написания команды. \n\nКоманды с доп. параметрами указаны отдельной графой в информации. Подробнее в /help.";
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
                callbackManager.handleCallback(update);
            }
        }
    }

}
