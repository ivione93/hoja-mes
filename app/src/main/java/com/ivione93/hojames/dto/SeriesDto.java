package com.ivione93.hojames.dto;

public class SeriesDto {

    public String distance;
    public String time;
    public boolean hurdles;
    public String shoes;

    public SeriesDto(String distance, String time, boolean hurdles, String shoes) {
        this.distance = distance;
        this.time = time;
        this.hurdles = hurdles;
        this.shoes = shoes;
    }
}
