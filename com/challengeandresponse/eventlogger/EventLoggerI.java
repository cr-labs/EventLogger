package com.challengeandresponse.eventlogger;

import java.util.List;

/**
 * 
 * @author jim
 *
 */
public interface EventLoggerI {
	
	public void shutdown();

	/**
	 * @param message the message to write to the event log
	 * @return A text version of the newly-added event, with the timestamp, exactly as it appears in the event log. If no event is written (e.g. if logger is disabled), return an empty string ""
	 */
	public String addEvent(String message);
	
	/**
	 * @param message the message to write to the event log
	 * @return A text version of the newly-added event, with the timestamp, exactly as it appears in the event log. If no event is written (e.g. if logger is disabled), return an empty string ""
	 */
	public String addEvent(String address, String message);
	
	/**
	 * Defined as returning the last "n" events in a meaningful way. But it's implementation-dependent.
	 * For database-logged events, this could be a value in seconds or the last N records;
	 * for textfile-logged events, this is the last N lines of the log file, where presumably
	 * every line is one log record.
	 * @param n the number of events to return
	 * @return a list of the last n Event objects
	 */
	public List getLastN(int n);
	
	/**
	 * Call disable() to squelch logging - addEvent() calls will not cause the log to be written
	 */
	public void disable();
	
	/**
	 * Call enable() to resume logging if it's been disabled
	 */
	public void enable();
	
}