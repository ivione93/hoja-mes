package com.ivione93.hojames;

import com.google.firebase.Timestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static boolean validateDateFormat(String sDate) {
        SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy");
        formatDate.setLenient(false);
        try {
            formatDate.parse(sDate);
        } catch (ParseException e) {
            return false;
        }
        return true;
    }

    public static Timestamp toTimestamp(String date) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date parsedDate = dateFormat.parse(date);
        Timestamp timestamp = new Timestamp(parsedDate);
        return timestamp;
    }

    public static String toString(Timestamp timestamp) {
        Date date = timestamp.toDate();
        String formatted = new SimpleDateFormat("dd/MM/yyyy").format(date);
        return formatted;
    }

    public static String toString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(date);
    }

    public static String calculatePartial(String time, String distance) {
        String sRitmo;
        BigDecimal bRitmo;
        float fTime = Float.parseFloat(time);
        float fDistance = Float.parseFloat(distance);
        float fRitmo = fTime / fDistance;
        bRitmo = new BigDecimal(fRitmo).setScale(2, RoundingMode.UP);
        int iRitmo = (int) fRitmo;

        String str = String.valueOf(bRitmo);
        int decNumberInt = Integer.parseInt(str.substring(str.indexOf('.') + 1));
        int sec = (60 * decNumberInt) / 100;
        String seg = "";
        if (sec < 10) {
            seg = "0" + sec;
        } else {
            seg = "" + sec;
        }

        sRitmo = iRitmo + "." + seg;

        return sRitmo;
    }

}
