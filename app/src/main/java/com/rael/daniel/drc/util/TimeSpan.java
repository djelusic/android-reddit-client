package com.rael.daniel.drc.util;

import java.util.Date;

/**
 * Calculates the difference between two dates.
 */
public class TimeSpan {

    public static String calculateTimeSpan(long t1, long t2) {
        Date start = new Date(t1);
        Date end = new Date(t2);

        long diffInSeconds = (end.getTime() - start.getTime());

        if (diffInSeconds < 60) return diffInSeconds + "s ago";
        else if ((diffInSeconds = (diffInSeconds / 60)) < 60) return diffInSeconds + "m ago";
        else if ((diffInSeconds = (diffInSeconds / 60)) < 24) return diffInSeconds + "h ago";
        else return diffInSeconds / 24 + "d ago";
    }
}
