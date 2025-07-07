package org.marensovich.Bot.YandexMapAPI;

public enum YandexMapScale {

    SCALE_1(1.0f),
    SCALE_2(2.0f),
    SCALE_3(3.0f),
    SCALE_4(4.0f);

    private final float scale;

    YandexMapScale(float scale) {
        this.scale = scale;
    }

    public float getScale() {
        return scale;
    }
}
