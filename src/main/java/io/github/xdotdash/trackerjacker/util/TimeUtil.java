package io.github.xdotdash.trackerjacker.util;

public class TimeUtil {

    public static String displayMinutes(int minutes) {
        StringBuilder time = new StringBuilder();

        double days = minutes / 24 / 60;
        double hours = minutes / 60 % 24;
        double mins = minutes % 60;

        if (days >= 1)
            time.append(Math.round(days)).append(days == 1 ? " day " : " days ");
        if (hours >= 1)
            time.append(Math.round(hours)).append(hours == 1 ? " hour " : " hours ");
        if (mins >= 1)
            time.append(Math.round(mins)).append(mins == 1 ? " min" : " mins");

        String str = time.toString();

        return str.isEmpty() ? "0" : time.toString();
    }
}