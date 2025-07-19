package org.marensovich.Bot.Utils;

import org.marensovich.Bot.TelegramBot;

import java.util.Timer;
import java.util.TimerTask;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DailyGenMapReset {

    private Timer timer;

    public void startDailyReset() {
        timer = new Timer("DailyGenMapReset", true);

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime nextMidnight = now.truncatedTo(ChronoUnit.DAYS).plusDays(1);
        long initialDelay = ChronoUnit.MILLIS.between(now, nextMidnight);

        timer.scheduleAtFixedRate(new ResetTask(), initialDelay, 24 * 60 * 60 * 1000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private class ResetTask extends TimerTask {
        @Override
        public void run() {
            try {
                TelegramBot.getDatabaseManager().resetAllGenMaps();
            } catch (Exception e) {
                System.err.println("Ошибка в задании сброса gen_map: " + e.getMessage());
            }
        }
    }


}