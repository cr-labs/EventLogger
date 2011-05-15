package com.challengeandresponse.eventlogger;

import java.util.*;

import javax.servlet.http.HttpServletRequest;


/**
 * logs server events.
 * 
 * A goal of this logger is to make it easy to write to stdout, but in the logger-y way 
 * so a different logger could be substituted just by instantiating that one instead.
 */

public class StdoutEventLogger implements EventLoggerI {

	private boolean enabled;

	private static final List <String> LASTN_RESPONSE = new Vector <String> ();
	static {
		LASTN_RESPONSE.add("StdoutEventLogger cannot return LastN events");
	}

	
	/**
	 * Opens the logger
	 */
	public StdoutEventLogger() {
		this.enabled = true;
		addEvent("Logger started");
	}

	public void shutdown() {
		addEvent("Logger shutting down");
	}
	
	/**
	 * Disable logging... calls to addEvent() will be silently ignored
	 */
	public void disable() {
		this.enabled = false;
		
	}

	/**
	 * Enable logging after it has been disabled. Calls to addEvent() will write to the log.
	 */
	public void enable() {
		this.enabled = true;
	}


	/**
	 * Convenience method to add an event to the event database, with a text message only, and no address
	 * @param message
	 */
	public String addEvent(String message) {
		return addEvent("",message);
	}
	
	/**
	 * Convenience method to add an event to the event database, with a text message and additional info culled from a Servlet request
	 * @param message The message to include
	 * @param request The servlet request that triggered the event. We'll pick out the requester's address and maybe more info about the request for the log
	 */
	public void addEvent(HttpServletRequest request, String message) {
		addEvent(request.getRemoteAddr(),message);
	}

	/**
	 * Add an event to the event file, with both an IP address and a message.
	 * This is the method that actually appends the error log. The other addEvent methods 
	 * are just convenience methods.
	 * If the logger has been made INACTIVE due to any previous exceptions,
	 * this method will return without logging anything.
	 */
	public synchronized String addEvent(String address, String message) {
		if (! this.enabled)
			return "";
		String parenAddress = "("+address.replace("(","").replace(")","")+")";
		String line = ((new Date())+" "+parenAddress+" "+message);
		System.out.println(line.trim());
		return line.trim();
	}



	/**
	 * Return the last N non-empty lines of server events from the log file. I said non-empty, not
	 * non-blank. Lines of length >= 1 are included in the returned kit.
	 * @return the last N non-empty lines of the file, as a vector of Strings, in the same order as found in the file
	 */
	public List <String> getLastN(int n) {
		return LASTN_RESPONSE;
	}

	
	/** for testing */
	public static void main(String[] args)
	throws EventLoggerException {
		
		EventLoggerI el = new StdoutEventLogger ();
		System.out.println("adding event");
		el.addEvent("Test event 1");
		
		System.out.println("Doing getLastN(10)");
		
		Iterator <String> it = el.getLastN(3).iterator();
		while (it.hasNext())
			System.out.println(it.next());
		
		System.out.println("Shutting down");
		el.shutdown();		
	}
	

	

}