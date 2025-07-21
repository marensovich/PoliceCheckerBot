package org.marensovich.Bot.CommandsManager.Commands;

import org.marensovich.Bot.CommandsManager.Command;
import org.marensovich.Bot.TelegramBot;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapLanguage;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapTheme;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.YandexMapTypes;
import org.marensovich.Bot.Utils.LoggerUtil;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SettingsCommand implements Command {

    public static final String CALLBACK_THEME = "settings_theme";
    public static final String CALLBACK_MAPTYPE = "settings_maptype";
    public static final String CALLBACK_LANG = "settings_lang";
    public static final String CALLBACK_QUIT = "settings_quit";
    public static final String CALLBACK_BACK = "settings_back";
    public static final String CALLBACK_SAVE = "settings_save";

    public static final String CALLBACK_THEME_DARK = "theme_dark";
    public static final String CALLBACK_THEME_LIGHT = "theme_light";

    public static final String CALLBACK_MAPTYPE_MAP = "maptype_map";
    public static final String CALLBACK_MAPTYPE_TRANSIT = "maptype_transit";
    public static final String CALLBACK_MAPTYPE_DRIVING = "maptype_driving";
    public static final String CALLBACK_MAPTYPE_ADMIN = "maptype_admin";

    public static final String CALLBACK_LANG_RU_RU = "lang_ru_RU";
    public static final String CALLBACK_LANG_EN_US = "lang_en_US";
    public static final String CALLBACK_LANG_UK_UA = "lang_uk_UA";
    public static final String CALLBACK_LANG_TR_TR = "lang_tr_TR";




    private record LangOption(String callback, String displayName, String apiValue) {}
    private static final List<LangOption> LANGUAGES = List.of(
            new LangOption(CALLBACK_LANG_RU_RU, "Русский язык", "ru_RU"),
            new LangOption(CALLBACK_LANG_EN_US, "Английский язык", "en_US"),
            new LangOption(CALLBACK_LANG_UK_UA, "Украинский язык", "uk_UA"),
            new LangOption(CALLBACK_LANG_TR_TR, "Турецкий язык", "tr_TR")
    );

    private record MapOption(String callback, String displayName, String apiValue) {}
    private static final List<MapOption> MAP_TYPES = List.of(
            new MapOption(CALLBACK_MAPTYPE_MAP, "Обычная карта", "map"),
            new MapOption(CALLBACK_MAPTYPE_TRANSIT, "Транспортная карта", "transit"),
            new MapOption(CALLBACK_MAPTYPE_DRIVING, "Автомобильная карта", "driving"),
            new MapOption(CALLBACK_MAPTYPE_ADMIN, "Административная карта", "admin")
    );

    private record ThemeOption(String callback, String displayName, String apiValue) {}
    private static final List<ThemeOption> THEMES = List.of(
            new ThemeOption(CALLBACK_THEME_DARK, "Темная", "dark"),
            new ThemeOption(CALLBACK_THEME_LIGHT, "Светлая", "light")
    );

    private String currentDisplayTheme;
    private String currentDisplayMapType;
    private String currentDisplayLang;

    private String currentApiTheme;
    private String currentApiMapType;
    private String currentApiLang;

    @Override
    public String getName() {
        return "/settings";
    }

    @Override
    public void execute(Update update) {
        TelegramBot.getInstance().getCommandManager().setActiveCommand(update.getMessage().getFrom().getId(), this);

        getSettings(update.getMessage().getFrom().getId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId().toString());
        sendMessage.setText(getSettingsText(update.getMessage().getFrom().getId()));
        sendMessage.setReplyMarkup(getSettingsKeyboard(update.getMessage().getFrom().getId()));

        try {
            TelegramBot.getInstance().execute(sendMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
    }

    private void getSettings(long userId) {
        Map<String, String> settings = TelegramBot.getDatabaseManager().getUserSettings(userId);

        currentApiTheme = settings.getOrDefault("yandex_theme", "light");
        currentApiMapType = settings.getOrDefault("yandex_maptype", "map");
        currentApiLang = settings.getOrDefault("yandex_lang", "ru_RU");

        currentDisplayTheme = THEMES.stream()
                .filter(theme -> theme.apiValue().equals(currentApiTheme))
                .findFirst()
                .map(ThemeOption::displayName)
                .orElse("Светлая"); // значение по умолчанию

        currentDisplayMapType = MAP_TYPES.stream()
                .filter(map -> map.apiValue().equals(currentApiMapType))
                .findFirst()
                .map(MapOption::displayName)
                .orElse("Обычная карта");

        currentDisplayLang = LANGUAGES.stream()
                .filter(lang -> lang.apiValue().equals(currentApiLang))
                .findFirst()
                .map(LangOption::displayName)
                .orElse("Русский язык");
    }


    private String getSettingsText(long userId) {
        getSettings(userId);
        return "Текущие настройки:\n\n" +
                "Тема: " + currentDisplayTheme + "\n" +
                "Тип карты: " + currentDisplayMapType + "\n" +
                "Язык: " + currentDisplayLang + "\n\n" +
                "Выберите параметр для изменения:";
    }

    private InlineKeyboardMarkup getSettingsKeyboard(long userId) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Map<String, String> dbSettings = TelegramBot.getDatabaseManager().getUserSettings(userId);
        String dbTheme = dbSettings.getOrDefault("yandex_theme", "light");
        String dbMapType = dbSettings.getOrDefault("yandex_maptype", "map");
        String dbLang = dbSettings.getOrDefault("yandex_lang", "ru_RU");

        String currentThemeApi = THEMES.stream()
                .filter(t -> t.displayName().equals(currentDisplayTheme))
                .findFirst()
                .map(ThemeOption::apiValue)
                .orElse("light");

        String currentMapTypeApi = MAP_TYPES.stream()
                .filter(m -> m.displayName().equals(currentDisplayMapType))
                .findFirst()
                .map(MapOption::apiValue)
                .orElse("map");

        String currentLangApi = LANGUAGES.stream()
                .filter(m -> m.displayName().equals(currentDisplayLang))
                .findFirst()
                .map(LangOption::apiValue)
                .orElse("map");

        boolean hasChanges = !dbTheme.equals(currentThemeApi)
                || !dbMapType.equals(currentMapTypeApi)
                || !dbLang.equals(currentLangApi);

        List<InlineKeyboardButton> themeRow = new ArrayList<>();
        InlineKeyboardButton themeButton = new InlineKeyboardButton("Тема");
        themeButton.setCallbackData(CALLBACK_THEME);
        themeRow.add(themeButton);
        rows.add(themeRow);

        List<InlineKeyboardButton> mapTypeRow = new ArrayList<>();
        InlineKeyboardButton mapTypeButton = new InlineKeyboardButton("Тип карты");
        mapTypeButton.setCallbackData(CALLBACK_MAPTYPE);
        mapTypeRow.add(mapTypeButton);
        rows.add(mapTypeRow);

        List<InlineKeyboardButton> langRow = new ArrayList<>();
        InlineKeyboardButton langButton = new InlineKeyboardButton("Язык");
        langButton.setCallbackData(CALLBACK_LANG);
        langRow.add(langButton);
        rows.add(langRow);

        if (hasChanges) {
            List<InlineKeyboardButton> saveRow = new ArrayList<>();
            InlineKeyboardButton saveButton = new InlineKeyboardButton("✅ Сохранить");
            saveButton.setCallbackData(CALLBACK_SAVE);
            saveRow.add(saveButton);
            rows.add(saveRow);
        }

        List<InlineKeyboardButton> quitRow = new ArrayList<>();
        InlineKeyboardButton quitButton = new InlineKeyboardButton("Выйти из настроек");
        quitButton.setCallbackData(CALLBACK_QUIT);
        quitRow.add(quitButton);
        rows.add(quitRow);

        markup.setKeyboard(rows);
        return markup;
    }

    public void handleThemeCallback(Update update) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (ThemeOption option : THEMES) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            if (option.apiValue().equals(currentApiTheme)) {
                button.setText(option.displayName() + " ✓");
            } else {
                button.setText(option.displayName());
            }
            button.setCallbackData(option.callback());
            row.add(button);
            rows.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("← Назад");
        backButton.setCallbackData(CALLBACK_BACK);
        backRow.add(backButton);
        rows.add(backRow);

        List<InlineKeyboardButton> quitRow = new ArrayList<>();
        InlineKeyboardButton quitButton = new InlineKeyboardButton("Выйти из настроек");
        quitButton.setCallbackData(CALLBACK_QUIT);
        quitRow.add(quitButton);
        rows.add(quitRow);

        markup.setKeyboard(rows);

        editMessage.setText("Выберите тему:");
        editMessage.setReplyMarkup(markup);
        try {
            TelegramBot.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
    }

    public void handleQuitCallback(Update update) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessage.setText("Вы успешно вышли из настроек!");
        editMessage.setReplyMarkup(null);
        try {
            TelegramBot.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
        TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getCallbackQuery().getMessage().getChatId());
    }

    public void handleMapTypeCallback(Update update) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (MapOption option : MAP_TYPES) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton(option.displayName() + (option.apiValue().equals(currentApiMapType) ? " ✓" : "")
            );
            button.setCallbackData(option.callback());
            row.add(button);
            rows.add(row);
        }

        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("← Назад");
        backButton.setCallbackData(CALLBACK_BACK);
        backRow.add(backButton);
        rows.add(backRow);

        List<InlineKeyboardButton> quitRow = new ArrayList<>();
        InlineKeyboardButton quitButton = new InlineKeyboardButton("Выйти из настроек");
        quitButton.setCallbackData(CALLBACK_QUIT);
        quitRow.add(quitButton);
        rows.add(quitRow);

        markup.setKeyboard(rows);

        editMessage.setText("Выберите тип карты:");
        editMessage.setReplyMarkup(markup);
        try {
            TelegramBot.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
    }

    public void handleLangCallback(Update update) {
        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (LangOption option : LANGUAGES) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton(option.displayName() + (option.apiValue().equals(currentApiLang) ? " ✓" : ""));
            button.setCallbackData(option.callback());
            row.add(button);
            rows.add(row);
        }


        List<InlineKeyboardButton> backRow = new ArrayList<>();
        InlineKeyboardButton backButton = new InlineKeyboardButton("← Назад");
        backButton.setCallbackData(CALLBACK_BACK);
        backRow.add(backButton);
        rows.add(backRow);

        List<InlineKeyboardButton> quitRow = new ArrayList<>();
        InlineKeyboardButton quitButton = new InlineKeyboardButton("Выйти из настроек");
        quitButton.setCallbackData(CALLBACK_QUIT);
        quitRow.add(quitButton);
        rows.add(quitRow);

        markup.setKeyboard(rows);

        editMessage.setText("Выберите язык:");
        editMessage.setReplyMarkup(markup);
        try {
            TelegramBot.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
    }

    public void handleBackCallback(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();

        saveCurrentSettings(userId);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessage.setText(getSettingsText(userId));
        editMessage.setReplyMarkup(getSettingsKeyboard(userId));
        try {
            TelegramBot.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
    }

    private void saveCurrentSettings(long userId) {
        try {
            YandexMapTheme theme = YandexMapTheme.valueOf(currentApiTheme);
            YandexMapTypes mapType = YandexMapTypes.valueOf(currentApiMapType);
            YandexMapLanguage lang = YandexMapLanguage.valueOf(currentApiLang);

            TelegramBot.getDatabaseManager().updateUserSettings(userId, theme, mapType, lang);
        } catch (IllegalArgumentException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(userId, "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(userId);
            throw new RuntimeException(e);
        }
    }


    public void handleSaveCallback(Update update) {
        long userId = update.getCallbackQuery().getFrom().getId();
        YandexMapTheme theme = YandexMapTheme.valueOf(currentDisplayTheme);
        YandexMapTypes mapType = YandexMapTypes.valueOf(currentDisplayMapType);
        YandexMapLanguage lang = YandexMapLanguage.valueOf(currentDisplayLang);

        TelegramBot.getDatabaseManager().updateUserSettings(userId, theme, mapType, lang);

        EditMessageText editMessage = new EditMessageText();
        editMessage.setChatId(update.getCallbackQuery().getMessage().getChatId().toString());
        editMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        editMessage.setText("Настройки успешно сохранены");
        try {
            TelegramBot.getInstance().execute(editMessage);
        } catch (TelegramApiException e) {
            LoggerUtil.logError(getClass(), "Произошла ошибка во время работы бота: " + e.getMessage());
            e.printStackTrace();
            TelegramBot.getInstance().sendErrorMessage(update.getMessage().getFrom().getId(), "⚠️ Ошибка при работе бота, обратитесь к администратору");
            TelegramBot.getInstance().getCommandManager().unsetActiveCommand(update.getMessage().getFrom().getId());
            throw new RuntimeException(e);
        }
    }

    public void handleOptionSelected(Update update, String optionType, String optionValue) {
        switch (optionType) {
            case "theme" -> {
                currentApiTheme = optionValue;
                currentDisplayTheme = THEMES.stream()
                        .filter(theme -> theme.apiValue().equals(currentApiTheme))
                        .findFirst()
                        .map(ThemeOption::displayName)
                        .orElse("Светлая");
                handleThemeCallback(update);
            }
            case "maptype" -> {
                currentApiMapType = optionValue;
                currentDisplayMapType = MAP_TYPES.stream()
                        .filter(map -> map.apiValue().equals(currentApiMapType))
                        .findFirst()
                        .map(MapOption::displayName)
                        .orElse("Обычная карта");
                handleMapTypeCallback(update);
            }
            case "lang" -> {
                currentApiLang = optionValue;
                currentDisplayLang = LANGUAGES.stream()
                        .filter(lang -> lang.apiValue().equals(currentApiLang))
                        .findFirst()
                        .map(LangOption::displayName)
                        .orElse("Русский язык");
                handleLangCallback(update);
            }
        }
    }
}