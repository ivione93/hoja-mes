package com.ivione93.hojames;

import com.google.firebase.Timestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static Timestamp toTimestamp(String date, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        Date parsedDate = dateFormat.parse(date);
        Timestamp timestamp = new Timestamp(parsedDate);
        return timestamp;
    }

    public static String toString(Timestamp timestamp, String format) {
        Date date = timestamp.toDate();
        return new SimpleDateFormat(format).format(date);
    }

    public static String toStringCalendar(Timestamp timestamp) {
        Date date = timestamp.toDate();
        return new SimpleDateFormat("yyyy-MM-dd").format(date);
    }

    public static String toString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static String initCalendarToString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static String selectDateCalendarToString(String date, String format) {
        Date sdf = null;
        try {
            sdf = new SimpleDateFormat("dd/MM/yyyy").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return toString(sdf, format);
    }

    public static String calculatePartial(String time, String distance) {
        String sRitmo;
        BigDecimal bRitmo;
        String formattedTime = convertTime(time);
        float fTime = Float.parseFloat(formattedTime);
        float fDistance = Float.parseFloat(distance);
        float fRitmo = fTime / fDistance;
        bRitmo = new BigDecimal(fRitmo).setScale(2, RoundingMode.UP);
        int iRitmo = bRitmo.intValue();

        String str = String.valueOf(bRitmo);
        int decNumberInt = Integer.parseInt(str.substring(str.indexOf('.') + 1));
        int sec = (60 * decNumberInt) / 100;
        String seg;
        if (sec < 10) {
            seg = "0" + sec;
        } else {
            seg = "" + sec;
        }

        sRitmo = iRitmo + "." + seg;

        return sRitmo;
    }

    public static String calculatePartialCycling(String time, String distance) {
        String formattedTime = convertTime(time); // minutos
        Float t = Float.parseFloat(formattedTime) / 60;
        Float d = Float.parseFloat(distance);
        Float v = d / t;
        BigDecimal result = new BigDecimal(v).setScale(1, RoundingMode.UP);

        return String.valueOf(result);
    }

    public static String convertTime(String time) {
        String formattedTime;
        if (time.startsWith("00")) {
            formattedTime = time.substring(4).replace(":", ".");
        } else {
            Integer hh = Integer.parseInt(time.substring(0,2));
            Integer mm = Integer.parseInt(time.substring(4,6));
            Integer min = (hh * 60) + mm;
            formattedTime = min.toString() + "." + time.substring(7,9);
        }

        return formattedTime;
    }

    public static String getFormattedTime(String result) {
        String formattedResult;
        String splitHour, splitMinutes, splitSecond;

        if (result.equals("AB")) {
            formattedResult = result;
        } else {
            splitHour = result.substring(0, 2);
            splitMinutes = result.substring(4,6);
            splitSecond = result.substring(7,9);

            if (splitHour.equals("00")) {
                formattedResult = splitMinutes + ":" + splitSecond;
            } else {
                if (splitHour.startsWith("0")) {
                    splitHour = splitHour.substring(1,2);
                }
                if (splitMinutes.equals("00") && splitSecond.equals("00")) {
                    formattedResult = splitHour + "h";
                } else {
                    formattedResult = splitHour + "h " + splitMinutes + ":" + splitSecond;
                }
            }
        }
        return formattedResult;
    }

    public static String getFormattedResult(String result) {
        String formattedResult;
        String splitHour, splitMinutes, splitSecond, splitMiliseconds;

        if (result.equals("AB")) {
            formattedResult = result;
        } else {
            splitHour = result.substring(0, 2);
            splitMinutes = result.substring(4,6);
            splitSecond = result.substring(7,9);
            splitMiliseconds = result.substring(10,12);

            if (splitHour.equals("00")) {
                if (splitMinutes.equals("00")) {
                    formattedResult = splitSecond + "." + splitMiliseconds;
                } else {
                    formattedResult = splitMinutes + ":" + splitSecond + "." + splitMiliseconds;
                }
            } else {
                if (splitHour.startsWith("0")) {
                    splitHour = splitHour.substring(1,2);
                }
                if (splitMinutes.equals("00") && splitSecond.equals("00") && splitMiliseconds.equals("00")) {
                    formattedResult = splitHour + "h";
                } else {
                    if (splitMiliseconds.equals("00")) {
                        formattedResult = splitHour + "h " + splitMinutes + ":" + splitSecond;
                    } else {
                        formattedResult = splitHour + "h " + splitMinutes + ":" + splitSecond + "." + splitMiliseconds;
                    }
                }
            }
        }
        return formattedResult;
    }

}
