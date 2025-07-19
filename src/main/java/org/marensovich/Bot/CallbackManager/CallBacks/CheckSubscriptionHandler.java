package org.marensovich.Bot.CallbackManager.CallBacks;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.RegisterCommand;
import org.marensovich.Bot.CommandsManager.Commands.SettingsCommand;
import org.marensovich.Bot.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class CheckSubscriptionHandler implements TelegramCallbackHandler {
    @Override
    public String getCallbackData() {
        return RegisterCommand.CALLBACK_CONFIRM;
    }

    @Override
    public void handle(Update update) {
        String channelId = Dotenv.load().get("TELEGRAM_CHANNEL_NEWS_ID");
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            GetChatMember getChatMember = new GetChatMember();
            getChatMember.setChatId(channelId);
            getChatMember.setUserId(userId);

            ChatMember userMember = TelegramBot.getInstance().execute(getChatMember);

            SendMessage response = new SendMessage();
            response.setChatId(chatId.toString());

            if (isMember(userMember.getStatus())) {
                response.setText("✅ Спасибо за подписку! Теперь вы можете пользоваться ботом.");
            } else {
                response.setText("❌ Вы еще не подписаны на канал. Пожалуйста, подпишитесь и попробуйте снова.");
            }

            TelegramBot.getInstance().execute(response);

            RegisterCommand command = (RegisterCommand) TelegramBot.getInstance()
                    .getCommandManager()
                    .getActiveCommand(userId);

            if (command != null) {
                command.registerUser(userId, chatId);
            }
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private boolean isMember(String status) {
        return "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
    }
}