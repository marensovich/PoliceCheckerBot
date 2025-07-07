package org.marensovich.Bot.YandexMapAPI;

import java.util.HashMap;
import java.util.Map;

public enum YandexMapSize {

    Large("650x450"),
    Medium("450х150"),
    Small("200x200");

    private final String size;

    YandexMapSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }

    public int[] getDimensions() {
        String[] parts = size.split("x");
        return new int[] { Integer.parseInt(parts[0].trim()), Integer.parseInt(parts[1].trim()) };
    }


}
