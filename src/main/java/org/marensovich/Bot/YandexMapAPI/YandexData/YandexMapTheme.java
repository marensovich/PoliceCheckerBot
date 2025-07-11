package org.marensovich.Bot.YandexMapAPI.YandexData;

public enum YandexMapTheme {

    Light("light"),
    Dark("dark");

    private final String theme;

    YandexMapTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme(){
        return theme;
    }
}
