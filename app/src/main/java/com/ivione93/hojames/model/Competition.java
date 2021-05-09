package com.ivione93.hojames.model;

import java.util.Date;

public class Competition {

    public String date;
    public String license;
    public String name;
    public String place;
    public String track;
    public String result;

    public Competition() {}

    public Competition(String date, String license, String name, String place, String track, String result) {
        this.date = date;
        this.license = license;
        this.name = name;
        this.place = place;
        this.track = track;
        this.result = result;
    }
}
