package org.marensovich.Bot.Data;

/**
 * Типы подписок
 */
public enum SubscribeTypes {

    None("none"),
    VIP("vip"),
    Premium("premium");

    private final String type;

    SubscribeTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    /**
     * Получения enum типа подписки по тексту
     * @param text
     * @return
     */
    public static SubscribeTypes fromString(String text) {
        for (SubscribeTypes b : SubscribeTypes.values()) {
            if (b.type.equalsIgnoreCase(text)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Нет такого типа подписки: " + text);
    }


}
