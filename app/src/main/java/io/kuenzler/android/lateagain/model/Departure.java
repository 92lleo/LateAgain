package io.kuenzler.android.lateagain.model;

import java.util.Date;

/**
 * @author Leonhard K�nzler
 * @version 0.1
 * @date 15.10.31 23:00
 */
public class Departure {

	String locStart, locDestination, timeStart, timeDestination, type, delay,
			duration;

	/**
	 * 
	 */
	public Departure() {
		// TODO something here?
	}

	/**
	 * 
	 * @param start
	 * @param dest
	 */
	public void setTimes(String start, String dest) {
		timeStart = start;
		timeDestination = dest;
	}

	/**
	 * 
	 * @param start
	 * @param dest
	 */
	public void setLocations(String start, String dest) {
		locStart = start;
		locDestination = dest;
	}

	/**
	 * @return the locStart
	 */
	public String getLocStart() {
		return locStart;
	}

	/**
	 * @param locStart
	 *            the locStart to set
	 */
	public void setLocStart(String locStart) {
		this.locStart = locStart;
	}

	/**
	 * @return the locDestination
	 */
	public String getLocDestination() {
		return locDestination;
	}

	/**
	 * @param locDestination
	 *            the locDestination to set
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
	 * @param timeStart
	 *            the timeStart to set
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
	 * @param timeDestination
	 *            the timeDestination to set
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
	 * @param type
	 *            the type to set
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
	 * @param delay
	 *            the delay to set
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
	 * @param duration
	 *            the duration to set
	 */
	public void setDuration(String duration) {
		this.duration = duration;
	}

	@Override
	public String toString() {
		return "From " + locStart + " to " + locDestination + " (" + type
				+ "), " + timeStart + "-" + timeDestination + " (" + duration
				+ "). Delay: " + delay;
	}
}