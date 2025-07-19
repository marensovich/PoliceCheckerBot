package org.marensovich.Bot.Maps.YandexMapAPI.YandexData;

public enum YandexMapTheme {

    light("light"),
    dark("dark");

    private final String theme;

    YandexMapTheme(String theme) {
        this.theme = theme;
    }

    public String getTheme(){
        return theme;
    }
}
