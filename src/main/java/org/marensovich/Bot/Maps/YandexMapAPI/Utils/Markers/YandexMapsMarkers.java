package org.marensovich.Bot.Maps.YandexMapAPI.Utils.Markers;

import java.util.ArrayList;
import java.util.List;

public class YandexMapsMarkers {
    private int markerCounter = 1;
    private final List<String> markers = new ArrayList<>();


    // Метод для добавления простой метки (только координаты)
    public void addMarker(double longitude, double latitude) {
        markers.add(String.format("%f,%f", longitude, latitude));
    }

    // Метод для добавления метки с контентом (числом)
    public void addMarker(double longitude, double latitude, int content) {
        markers.add(String.format("%f,%f,%d", longitude, latitude, content));
    }

    // Метод для добавления метки с полным описанием (стиль, цвет, размер, контент)
    public void addMarker(double longitude, double latitude,
                          MarkerStyle style, MarkerColor color,
                          MarkerSize size) {
        StringBuilder marker = new StringBuilder();
        marker.append(String.format("%f,%f,", longitude, latitude));
        marker.append(style.getValue()).append(color.getValue()).append(size.getValue());

        markers.add(marker.toString());
    }

    // Метод для добавления специальных меток (flag, org, comma и т.д.)
    public void addSpecialMarker(double longitude, double latitude, MarkerStyle style) {
        markers.add(String.format("%f,%f,%s", longitude, latitude, style.getValue()));
    }

    // Метод для добавления метки с буквой (A или B)
    public void addLetterMarker(double longitude, double latitude, MarkerStyle style, char letter) {
        if (letter == 'A' || letter == 'a' || letter == 'Б' || letter == 'б') {
            markers.add(String.format("%f,%f,%s%c", longitude, latitude, style.getValue(), letter));
        } else {
            throw new IllegalArgumentException("Only 'A' or 'B' (rus) letters are supported");
        }
    }

    public String generatePtParameter() {
        if (markers.isEmpty()) {
            return "";
        }

        // Сбрасываем счетчик после генерации
        markerCounter = 1;
        String result = String.join("~", markers);
        markers.clear();
        return result;
    }

    public int getCurrentMarkerNumber() {
        return markerCounter++;
    }

    // Сброс счетчика меток
    public void resetMarkerCounter() {
        markerCounter = 1;
    }
}