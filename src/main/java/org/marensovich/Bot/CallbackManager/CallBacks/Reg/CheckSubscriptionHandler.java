package org.marensovich.Bot.CallbackManager.CallBacks.Reg;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.CallbackManager.TelegramCallbackHandler;
import org.marensovich.Bot.CommandsManager.Commands.RegisterCommand;
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
        String[] CHANNEL_IDS = Dotenv.load().get("TELEGRAM_REQUIRED_SUB_CHANNELS").split(",\\s*");
        Long userId = update.getCallbackQuery().getFrom().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        try {
            boolean isSubscribedToAll = true;
            for (String channelId : CHANNEL_IDS) {
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(channelId.trim());
                getChatMember.setUserId(userId);
                ChatMember userMember = TelegramBot.getInstance().execute(getChatMember);

                if (!isMember(userMember.getStatus())) {
                    isSubscribedToAll = false;
                    break;
                }
            }

            SendMessage response = new SendMessage();
            response.setChatId(chatId.toString());

            if (isSubscribedToAll) {
                response.setText("✅ Спасибо за подписку! Теперь вы можете пользоваться ботом.");

                RegisterCommand command = (RegisterCommand) TelegramBot.getInstance()
                        .getCommandManager()
                        .getActiveCommand(userId);

                if (command != null) {
                    command.registerUser(userId, chatId);
                }
            } else {
                response.setText("❌ Вы еще не подписаны на все необходимые каналы. Пожалуйста, подпишитесь и попробуйте снова.");
            }

            TelegramBot.getInstance().execute(response);
        } catch (TelegramApiException e) {
            TelegramBot.getInstance().sendErrorMessage(chatId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            e.printStackTrace();
        }
    }

    private boolean isMember(String status) {
        return "member".equals(status) || "administrator".equals(status) || "creator".equals(status);
    }
}