package org.marensovich.Bot.Data;

import java.sql.Timestamp;


/**
 * Класс информации о пользователе
 */
public class UserInfo {

    public long userId;
    public String yandexLang;
    public String yandexTheme;
    public String yandexMaptype;
    public boolean isAdmin;
    public String subscribe; // "none", "vip", "premium"
    public int genMap;
    public java.sql.Timestamp registrationTime;

    public String subscribeType; // "none", "vip", "premium"
    public Timestamp subscriptionExpiration;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getYandexLang() {
        return yandexLang;
    }

    public void setYandexLang(String yandexLang) {
        this.yandexLang = yandexLang;
    }

    public String getYandexTheme() {
        return yandexTheme;
    }

    public void setYandexTheme(String yandexTheme) {
        this.yandexTheme = yandexTheme;
    }

    public String getYandexMaptype() {
        return yandexMaptype;
    }

    public void setYandexMaptype(String yandexMaptype) {
        this.yandexMaptype = yandexMaptype;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getSubscribe() {
        return subscribe;
    }

    public void setSubscribe(String subscribe) {
        this.subscribe = subscribe;
    }

    public int getGenMap() {
        return genMap;
    }

    public void setGenMap(int genMap) {
        this.genMap = genMap;
    }

    public java.sql.Timestamp getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(java.sql.Timestamp registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getSubscribeType() {
        return subscribeType;
    }

    public void setSubscribeType(String subscribeType) {
        this.subscribeType = subscribeType;
    }

    public java.sql.Timestamp getSubscriptionExpiration() {
        return subscriptionExpiration;
    }

    public void setSubscriptionExpiration(java.sql.Timestamp subscriptionExpiration) {
        this.subscriptionExpiration = subscriptionExpiration;
    }
}