package com.bentahsin.antiafk.utils;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeUtil {

    private static final Pattern TIME_PATTERN = Pattern.compile("(\\d+)([smhd])");

    public static long parseTime(String timeString) {
        if (timeString == null || timeString.isEmpty()) {
            return 0;
        }
        Matcher matcher = TIME_PATTERN.matcher(timeString.toLowerCase());
        long totalSeconds = 0;
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            switch (unit) {
                case "s":
                    totalSeconds += value;
                    break;
                case "m":
                    totalSeconds += value * 60L;
                    break;
                case "h":
                    totalSeconds += value * 3600L;
                    break;
                case "d":
                    totalSeconds += value * 86400L;
                    break;
            }
        }
        return totalSeconds;
    }

    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + " saniye";
        }
        long minutes = TimeUnit.SECONDS.toMinutes(seconds);
        long remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes);
        if (remainingSeconds > 0) {
            return minutes + " dakika " + remainingSeconds + " saniye";
        }
        return minutes + " dakika";
    }
}