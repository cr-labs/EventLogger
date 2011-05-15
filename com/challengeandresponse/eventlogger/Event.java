package com.challengeandresponse.eventlogger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Holds a single server event -- an IP address, timestamp, and text message
 * @author jim
 */

public class Event {

	public	long	time;
	public	String	address;
	public	String	message;

	/**
	 * Construct an Event that sets no default values (useful for QBE queries only, really, so only accessible via getQBEInstance() method
	 */
	private Event() {
	}

	/**
	 * Return an empty Event object for use with QBE. Otherwise has no purpose
	 * @return a new Event object instance
	 */
	public static Event getQBEInstance() {
		return new Event();
	}
	
	/**
	 * Construct an Event, setting the time to the current time, and including the message and address
	 * @param address
	 * @param message
	 */	
	public Event(String address, String message) {
		this.time = System.currentTimeMillis();
		this.address = address;
		this.message = message;
	}
	
	/**
	 * Construct a ServerEvent, setting the time to the current time, and including the message
	 * @param message
	 */
	public Event(String message) {
		this("",message);
	}
	
	
	/**
	 * Returns a nicely formatted version of one server event, using the SimpleDateFormat provided in dateFormat
	 * @param dateFormat a SimpleDateFormat compatible String showing how the date should be formatted for output
	 * @return This ServerEvent record, formatted, with one blank space between each field (time, address, message)
	 */
	public String toString(String dateFormat) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		return (sdf.format(new Date(time))+" "+address+" "+message);
	}
	
	/**
	 * Returns a nicely formatted version of one server event, leaving the date unformatted as a long Unix timestamp
	 * @return This ServerEvent record, formatted, with one blank space between each field (time, address, message)
	 */
	public String toString() {
		return (time+" "+address+" "+message);
	}
	
}

