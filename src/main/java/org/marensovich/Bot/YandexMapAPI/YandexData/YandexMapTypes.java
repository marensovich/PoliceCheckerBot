package org.marensovich.Bot.YandexMapAPI.YandexData;

public enum YandexMapTypes {

    base("map"),
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
