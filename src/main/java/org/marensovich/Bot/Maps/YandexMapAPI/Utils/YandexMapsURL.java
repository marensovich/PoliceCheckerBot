package org.marensovich.Bot.Maps.YandexMapAPI.Utils;

import io.github.cdimascio.dotenv.Dotenv;
import org.marensovich.Bot.Maps.YandexMapAPI.YandexData.*;

public class YandexMapsURL {

    private static final String YandexMapsAPIToken = Dotenv.load().get("YANDEX_MAPS_API_KEY");

    private static String BASE_URL = "https://static-maps.yandex.ru/v1";

    private static final String LL = "&ll=";
    private static final String SPN = "&spn=";
    private static final String BBOX = "&bbox=";
    private static final String Z = "&z=";
    private static final String SIZE = "&size=";
    private static final String SCALE = "&scale=";
    private static final String PT = "&pt=";
    private static final String PL = "&pl=";
    private static final String LANG = "?lang=";
    private static final String STYLE = "&style=";
    private static final String THEME = "&theme=";
    private static final String MAPTYPE = "&maptype=";

    public static String generateURL(
            float latitude, float longitude,
            Float spn, String bbox,
            Integer z, YandexMapSize mapSize,
            YandexMapScale scale, String pt,
            String pl, YandexMapLanguage lang,
            String style, YandexMapTheme theme, YandexMapTypes maptype) {

        StringBuilder urlBuilder = new StringBuilder(BASE_URL);

        urlBuilder.append(LANG).append(lang != null ? lang.getLang() : YandexMapLanguage.ru_RU.getLang());

        urlBuilder.append(LL).append(latitude).append(",").append(longitude);

        if (spn != null && spn != 0.0f) {
            urlBuilder.append(SPN).append(spn).append(",").append(spn);
        }
        if (bbox != null && !bbox.isEmpty()) {
            urlBuilder.append(BBOX).append(bbox);
        }
        if (z != null) {
            if (z <= 0){
                z = 0;
                urlBuilder.append(Z).append(z);
            } else if (z >= 21){
                z = 21;
                urlBuilder.append(Z).append(z);
            } else {
                urlBuilder.append(Z).append(z);
            }
        }
        if (mapSize != null) {
            int[] dimensions = mapSize.getDimensions();
            urlBuilder.append(SIZE).append(dimensions[0]).append(",").append(dimensions[1]);
        }
        if (scale != null) {
            urlBuilder.append(SCALE).append(scale.getScale());
        }
        if (pt != null && !pt.isEmpty()) {
            urlBuilder.append(PT).append(pt);
        }
        if (pl != null && !pl.isEmpty()) {
            urlBuilder.append(PL).append(pl);
        }
        if (style != null && !style.isEmpty()) {
            urlBuilder.append(STYLE).append(style);
        }
        if (theme != null) {
            urlBuilder.append(THEME).append(theme.getTheme());
        }
        if (maptype != null) {
            urlBuilder.append(MAPTYPE).append(maptype.getType());
        }
        urlBuilder.append("&apikey=").append(YandexMapsAPIToken);

        return urlBuilder.toString();
    }

    public static String getBBOXparam(double x1, double x2, double y1, double y2){
        return x1 + x2 + "~" + y1 + y2;
    }
}

