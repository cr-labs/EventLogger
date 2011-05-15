package com.challengeandresponse.eventlogger;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;


/**
 * logs events to nowhere.
 * 
 * This logger discards all things written to it. Its purpose is just to be a stand-in
 * in code that expects to log events but you don't want to (and it would be very
 * undesirable to do if (eventLogger != null) before every attempted log write.
 * Just give the code with log calls the null logger... then it can be easily
 * swapped out for a real logger for debugging or as desired.
 */

public class NullEventLogger implements EventLoggerI {
	
	private static final Vector <String> LASTN_RESPONSE = new Vector <String> ();
	static {
		LASTN_RESPONSE.add("NullEventLogger cannot return LastN events");
	}


	/**
	 * Opens the logger
	 */
	public NullEventLogger() {
	}

	public void shutdown() {
	}
	
	/**
	 * Disable logging... calls to addEvent() will be silently ignored
	 */
	public void disable() {
	}

	/**
	 * Enable logging after it has been disabled. Calls to addEvent() will resume writing the log.
	 */
	public void enable() {
	}


	/**
	 * Convenience method to add an event to the event database, with a text message only, and no address
	 * @param message
	 */
	public String addEvent(String message) {
		return "";
	}
	
	/**
	 * Convenience method to add an event to the event database, with a text message and additional info culled from a Servlet request
	 * @param message The message to include
	 * @param request The servlet request that triggered the event. We'll pick out the requester's address and maybe more info about the request for the log
	 */
	public void addEvent(HttpServletRequest request, String message) {
	}

	/**
	 * Add an event to the event file, with both an IP address and a message.
	 */
	public String addEvent(String address, String message) {
		return "";
	}



	/**
	 * NullEventLogger cannot return the last N lines because it has discarded them.
	 * Instead it returns a one-line vector indicating that this logger cannot do "lastN"
	 * @param n how many lines to return
	 * @return  a one-line vector indicating that this logger cannot do "lastN"
	 */
	public List <String> getLastN(int n) {
		return LASTN_RESPONSE;
	}



	/** for testing */
	public static void main(String[] args) {
		
		EventLoggerI el = new NullEventLogger();
		System.out.println("adding event");
		el.addEvent("Test event 1");
		
		System.out.println("Doing getLastN(10)");

		//		Vector <String> vv = (Vector<String>) el.getLastN(3).iterator();
		Iterator <String> it = el.getLastN(3).iterator();
		while (it.hasNext())
			System.out.println(it.next());
		
		System.out.println("Shutting down");
		el.shutdown();		
	}
	
	

}