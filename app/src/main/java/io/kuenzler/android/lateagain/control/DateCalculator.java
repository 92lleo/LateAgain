package io.kuenzler.android.lateagain.control;

import android.util.Log;

import io.kuenzler.android.lateagain.model.Departure;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Leonhard Künzler
 * @version 0.6
 * @date 09.11.05 22:00 cleanup
 */
public class DateCalculator {

    private Departure departure;

    /**
     *
     */
    public DateCalculator() {
        //TODO needed?
    }

    /**
     * Calculates difference between date 1 and date 2
     *
     * @param target target date
     * @param start  start date
     * @return time in ms to target
     */
    public long getDateDifference(Date target, Date start) {
        return target.getTime() - start.getTime();
    }

    /**
     * Returns date difference from now
     *
     * @param target different date
     * @return time in ms to target
     */
    public long getDateDifferenceFromNow(Date target) {
        return getDateDifference(target, new Date());
    }

    /**
     * Splits a date (long ms) in nice String
     *
     * @param date date as long (ms)
     * @return single values as real numbers
     */
    public long[] splitDate(long date) {
        long[] splitted = new long[4];
        splitted[3] = date / 1000 % 60; // seconds
        splitted[2] = date / (60 * 1000) % 60; // mins
        splitted[1] = date / (60 * 60 * 1000) % 24; // hours
        splitted[0] = date / (24 * 60 * 60 * 1000); // days
        return splitted;
    }

    /**
     * Simple version of print date (l, b ,b , b)
     *
     * @param date
     */
    public String[] printDate(long date) {
        return printDate(date, false, true, true, true);
    }

    /**
     * Gives date back in hh.mm.ss
     *
     * @param date date as long (ms)
     * @param day  show date
     * @param hour show hours
     * @param min  show minutes
     * @param sec  show seconds
     */
    public String[] printDate(long date, boolean day, boolean hour, boolean min, boolean sec) {
        long[] splitted = splitDate(date);

        String time = "";
        if (day) {
            time += splitted[0] + ".";
        }
        if (hour) {
            if (String.valueOf(splitted[1]).length() == 1) {
                time += 0;
            }
            time += splitted[1] + ":";
        }
        if (min) {
            if (String.valueOf(splitted[2]).length() == 1) {
                time += 0;
            }
            time += splitted[2] + ":";
        }
        if (String.valueOf(splitted[3]).length() == 1) {
            time += 0;
        }
        time += splitted[3];
        String[] result = new String[2];
        result[0] = time;
        result[1] = departure.getTimeStart() + " (" + departure.getDelay() + ")";
        return result;
    }

    /**
     * Extracts start time and delay from departure and starts contdown
     *
     * @param departure the dep. to count down to
     */
    public long countToDeparture(Departure departure) {
        this.departure = departure;
        String time = departure.getTimeStart();
        String[] splittedTime = time.split(":");

        System.out.println("\nCalculating time departure at " + time + " ("
                + departure.getDelay() + ")");

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splittedTime[0]));
        cal.set(Calendar.MINUTE, Integer.parseInt(splittedTime[1]));
        cal.set(Calendar.SECOND, 0); //to direct minute
        cal.set(Calendar.MILLISECOND, 0); //to direct minute
        int delay = Integer.parseInt(departure.getDelay().substring(1)); //TODO if not just delay
        long departureTime = cal.getTimeInMillis() + (delay * 60000);
        Date date = new Date(departureTime);
        return getDateDifferenceFromNow(date);
    }

    /**
     * @return
     */
    public static String getCurrentDate() {
        Date today = new Date();
        DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMANY);
        return formatter.format(today);
    }

    public static String getCurrentTime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm");//dd/MM/yyyy
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }

    public static long getDistancems(long timestamp) {
        return System.currentTimeMillis() - timestamp;
    }
}
