package org.marensovich.Bot.Maps.YandexMapAPI.YandexData;

public enum YandexMapTypes {

    map("map"),
    driving("driving"),
    transit("transit"),
    admin("admin");

    private final String type;

    YandexMapTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

}
