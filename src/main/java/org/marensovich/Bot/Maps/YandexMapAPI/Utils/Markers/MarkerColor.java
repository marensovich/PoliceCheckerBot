package org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers;

public enum MarkerColor {
    // Основные цвета для pm и pm2
    WHITE("wt"),        // Белый
    DARK_ORANGE("do"),  // Темно-оранжевый
    DARK_BLUE("db"),    // Темно-синий
    BLUE("bl"),         // Синий
    GREEN("gn"),        // Зеленый
    DARK_GREEN("dg"),   // Темно-зеленый (только pm2)
    GRAY("gr"),         // Серый
    LIGHT_BLUE("lb"),   // Светло-синий
    NIGHT("nt"),        // Темная ночь
    ORANGE("or"),       // Оранжевый
    PINK("pn"),         // Розовый
    RED("rd"),          // Красный
    VIOLET("vv"),       // Фиолетовый
    YELLOW("yw"),       // Желтый

    // Специальные цвета для pm2
    ORG("org"),         // Голубой (без контента)
    DIR("dir"),         // Фиолетовый (без контента)
    BLUE_YELLOW("blyw"); // Голубой с желтой точкой

    private final String value;

    MarkerColor(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
