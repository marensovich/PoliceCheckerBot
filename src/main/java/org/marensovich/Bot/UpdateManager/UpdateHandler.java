package org.marensovich.Bot.UpdateManager;

import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class UpdateHandler {

    public void updateHandler(Update update) throws TelegramApiException {

        if (update.hasMessage() || update.hasCallbackQuery()){
            if (update.hasMessage()){
                if (!TelegramBot.getDatabaseManager().checkAllUsersExists(update.getMessage().getFrom().getId())){
                    TelegramBot.getDatabaseManager().addAllUser(update.getMessage().getFrom().getId());
                }
                if (TelegramBot.getInstance().getCommandManager().hasActiveCommand(update.getMessage().getFrom().getId())){
                    TelegramBot.getInstance().getCommandManager().executeCommand(update);
                }
                if (update.getMessage().hasText()){
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
            }
            if (update.hasCallbackQuery()) {
                Long userId = update.getCallbackQuery().getFrom().getId();

                // 1. Регистрируем пользователя если нужно
                if (!TelegramBot.getDatabaseManager().checkAllUsersExists(userId)) {
                    TelegramBot.getDatabaseManager().addAllUser(userId);
                }

                // 2. Логирование для отладки
                System.out.println("Processing callback: " + update.getCallbackQuery().getData());

                // 3. Обрабатываем callback
                boolean handled = TelegramBot.getInstance().getCallbackManager().handleCallback(update);

                if (!handled) {
                    System.out.println("No handler found for callback: " + update.getCallbackQuery().getData());
                    // Можно отправить сообщение об ошибке пользователю
                    SendMessage errorMsg = new SendMessage();
                    errorMsg.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
                    errorMsg.setText("Команда не распознана, попробуйте ещё раз");
                    try {
                        TelegramBot.getInstance().execute(errorMsg);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
