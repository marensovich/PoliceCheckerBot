package org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers;

public enum MarkerSize {
    SMALL("s"),     // Маленький (только для pm)
    MEDIUM("m"),    // Средний
    LARGE("l");     // Большой

    private final String value;

    MarkerSize(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
