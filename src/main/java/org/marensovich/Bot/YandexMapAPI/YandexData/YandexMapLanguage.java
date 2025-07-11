package org.marensovich.Bot.YandexMapAPI.YandexData;

public enum YandexMapLanguage {

    ru_RU("ru_RU"),
    en_US("en_US"),
    en_RU("en_RU"),
    ru_UA("ru_UA"),
    uk_UA("uk_UA"),
    tr_TR("tr_TR");

    private final String lang;

    YandexMapLanguage(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

}
