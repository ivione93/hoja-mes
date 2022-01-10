package com.ivione93.hojames.dto;

public class SeriesDto {

    public String distance;
    public String time;
    public boolean hurdles;
    public boolean drags;
    public String shoes;

    public SeriesDto(String distance, String time, boolean hurdles, boolean drags, String shoes) {
        this.distance = distance;
        this.time = time;
        this.hurdles = hurdles;
        this.drags = drags;
        this.shoes = shoes;
    }
}
