package io.kuenzler.android.lateagain.model;

import java.util.Date;

import io.kuenzler.android.lateagain.control.DateCalculator;

/**
 * @author Leonhard Künzler
 * @version 1.0
 * @date 09.11.15 22:00 cleanup
 */
public class Departure {

    private String locStart, locDestination, timeStart, timeDestination, type, delay, duration, platform;
    private long timestamp;

    /**
     *
     */
    public Departure() {
        // TODO something here?
        timestamp = System.currentTimeMillis();
    }

    /**
     * Set start and dest time
     *
     * @param start start time as HH.mm
     * @param dest  dest time as HH.mm
     */
    public void setTimes(String start, String dest) {
        timeStart = start;
        timeDestination = dest;
    }

    /**
     * Set start and dest location
     *
     * @param start startlocation
     * @param dest  destlocation
     */
    public void setLocations(String start, String dest) {
        locStart = start;
        locDestination = dest;
    }

    public void setLocStart(String start) {
        locStart = start;

    }

    public void setDest(String dest) {
        locDestination = dest;
    }

    /**
     * @return the locStart
     */
    public String getLocStart() {
        return locStart;
    }

    /**
     * @return the locDestination
     */
    public String getLocDestination() {
        return locDestination;
    }

    /**
     * @param locDestination the locDestination to set
     */
    public void setLocDestination(String locDestination) {
        this.locDestination = locDestination;
    }

    /**
     * @return the timeStart
     */
    public String getTimeStart() {
        return timeStart;
    }

    /**
     * @param timeStart the timeStart to set
     */
    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    /**
     * @return the timeDestination
     */
    public String getTimeDestination() {
        return timeDestination;
    }

    /**
     * @param timeDestination the timeDestination to set
     */
    public void setTimeDestination(String timeDestination) {
        this.timeDestination = timeDestination;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the delay
     */
    public String getDelay() {
        return delay;
    }

    /**
     * @param delay the delay to set
     */
    public void setDelay(String delay) {
        this.delay = delay;
    }

    /**
     * @return the duration
     */
    public String getDuration() {
        return duration;
    }

    /**
     * @param duration the duration to set
     */
    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatform() {
        return platform;
    }

    @Override
    public String toString() {
        return "From " + locStart + ", Platform " + platform + " to " + locDestination + " (" + type
                + "), at " + timeStart + " with " + delay + " delay. ("
                + DateCalculator.getDistancems(timestamp) + "ms ago)";
    }

    static boolean verifyDeparture(Departure dep) {
        boolean result = true;
        if (dep.getLocStart() == null) {
            result = false;
        }
        if (dep.getLocDestination() == null) {
            // not needed for now // result = false;
        }
        try {
            Integer.parseInt(dep.getDelay());
        } catch (NumberFormatException e) {
            result = false;
        }

        return result;
    }
}