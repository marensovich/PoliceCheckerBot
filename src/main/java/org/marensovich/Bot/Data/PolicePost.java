package org.marensovich.Bot.Data;

import java.sql.Timestamp;

public class PolicePost {
    public long id;
    public long userId;
    public double latitude;
    public double longitude;
    public String postType;
    public Timestamp registrationTime;
    public String comment;
    public boolean expired;
    public String distance;

    public PolicePost(long id, long userId, double latitude, double longitude,
                      String postType, Timestamp registrationTime,
                      String comment, boolean expired, String distance) {
        this.id = id;
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.postType = postType;
        this.registrationTime = registrationTime;
        this.comment = comment;
        this.expired = expired;
        this.distance = distance;
    }
}
