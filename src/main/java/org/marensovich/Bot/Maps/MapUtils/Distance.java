package org.marensovich.Bot.Maps.MapUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Distance {

    private Distance() {
        throw new AssertionError("Нельзя создать экземпляр утилитарного класса");
    }

    private static final double EARTH_RADIUS_KM = 6371.0;

    private static double calcDist(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    public static String getDistance(double lat1, double lon1, double lat2, double lon2){
        double distanceInKm = calcDist(lat1, lon1, lat2, lon2);
        if (distanceInKm < 1.0) {
            return new BigDecimal(distanceInKm * 1000).setScale(0, RoundingMode.HALF_UP) + " м";
        } else if (distanceInKm < 10.0) {
            return new BigDecimal(distanceInKm).setScale(3, RoundingMode.HALF_UP) + " км";
        } else {
            return new BigDecimal(distanceInKm).setScale(2, RoundingMode.HALF_UP) + " км";
        }
    }
}
