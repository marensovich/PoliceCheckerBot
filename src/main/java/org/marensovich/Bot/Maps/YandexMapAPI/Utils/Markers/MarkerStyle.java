package org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers;

public enum MarkerStyle {
    PM("pm"),      // Круглые метки с цифрами/буквами
    PM2("pm2"),    // Круглые метки версии 2
    FLAG("flag"),   // Флажок
    VK("vk"),       // Кнопки VK
    ORG("org"),     // Голубая метка с хвостиком
    COMMA("comma"), // Голубая метка с кругом
    ROUND("round"), // Круглая голубая метка
    HOME("home"),   // Значок дома
    WORK("work"),   // Значок работы
    YA_RU("ya_ru"), // Логотип Яндекса (RU)
    YA_EN("ya_en"); // Логотип Яндекса (EN)

    private final String value;

    MarkerStyle(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
