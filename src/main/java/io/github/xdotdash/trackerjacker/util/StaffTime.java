package io.github.xdotdash.trackerjacker.util;

import java.util.UUID;

public class StaffTime {
    
    private final UUID uuid;
    private final int day0;
    private final int day1;
    private final int day2;
    private final int day3;
    private final int day4;
    private final int day5;
    private final int day6;

    public StaffTime(UUID uuid, int day0, int day1, int day2, int day3, int day4, int day5, int day6) {
        this.uuid = uuid;
        this.day0 = day0;
        this.day1 = day1;
        this.day2 = day2;
        this.day3 = day3;
        this.day4 = day4;
        this.day5 = day5;
        this.day6 = day6;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getDay0() {
        return day0;
    }

    public int getDay1() {
        return day1;
    }

    public int getDay2() {
        return day2;
    }

    public int getDay3() {
        return day3;
    }

    public int getDay4() {
        return day4;
    }

    public int getDay5() {
        return day5;
    }

    public int getDay6() {
        return day6;
    }
}