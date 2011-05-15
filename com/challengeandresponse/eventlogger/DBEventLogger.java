package com.challengeandresponse.eventlogger;

import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.db4o.Db4o;
import com.db4o.ObjectContainer;
import com.db4o.config.Configuration;
import com.db4o.query.Predicate;

/**
 * logs server events
 */

public class DBEventLogger implements EventLoggerI {

	private ObjectContainer eventDB;
	
	private boolean active;

	/**
	 * Opens the event database in the file named eventDBFile, and configures its indices and other goodies
	 * @param eventDBFile
	 * @throws EventLoggerException
	 */
	public DBEventLogger(String eventDBFile, int dbMessageLevel)
	throws EventLoggerException {
		// set the configuration specs for this db
		Configuration conf = Db4o.newConfiguration();
		conf.allowVersionUpdates(true);
		conf.objectClass(Event.class).objectField("time").indexed(true);
		conf.messageLevel(dbMessageLevel);
		try {
			eventDB = Db4o.openFile(conf,eventDBFile);
		}
		catch (com.db4o.ext.Db4oException db4oe) {
			throw new EventLoggerException("Exception opening eventDB:"+db4oe.getMessage());
		}
		active = true;
	}

	public void shutdown() {
		eventDB.close();
	}
	
	/**
	 * Disable logging... calls to addEvent() will be silently ignored
	 */
	public void disable() {
		this.active = false;
		
	}

	/**
	 * Enable logging after it has been disabled. Calls to addEvent() will resume writing the log.
	 */
	public void enable() {
		this.active = true;
	}


	/**
	 * Add an event to the event database, with a text message
	 * @param message
	 */
	public String addEvent(String message) {
		return addEvent("",message);
	}
	
	public String addEvent(String address, String message) {
		if (active) {
			Event ev = new Event(message,address);
			eventDB.set(ev);
			return ev.toString();
		}
		else
			return "";
	}

	/**
	 * Add a server event to the event database, with a text message and additional info culled from the request
	 * @param 	message	 The message to include
	 * @param	request	The request that triggered the event. We'll pick out the requester's address and maybe more info about the request for the log
	 */
	public void addEvent(HttpServletRequest request, String message) {
		addEvent(request.getRemoteAddr(),message);
	}


	/**
	 * Retrieve the last 'n' seconds of events and return them.
	 * @param n how many seconds of events to return
	 * @return the last 'n' seconds of events as a List of Event objects
	 */
	public List <Event> getLastN(int n) {
		// run the query: last (sec) seconds of records, in reverse order newest to oldest
		List <Event> result = eventDB.query(new SecondsPredicate(n),new LogComparator(true));
		return result;
	}

	
	
	/// INNER CLASSES - the QUERY PREDICATES AND COMPARATORS
	
	final class SecondsPredicate extends Predicate <Event> {
		private static final long serialVersionUID = 1L;
		long cutoff;

		SecondsPredicate(int sec) {
			cutoff = System.currentTimeMillis() - (long) (sec * 1000);
		}
		public boolean match(Event ev) {
			return ev.time > cutoff;
		}
	}

	final class LogComparator implements Comparator <Event> {
		final int less, greater;
		static final int EQUAL = 0;
		LogComparator(boolean invert) {
			// normal return for 'less' is a negative value; 0 for 'equal'; a positive value for 'more' 
			less 	= (! invert) ? -1 :  1;
			greater	= (! invert) ?  1 : -1;
		}
		public int compare(Event a, Event b) {
			if (a.time < b.time)
				return less;
			else if (a.time == b.time)
				return EQUAL;
			else return greater;
		}
	}

	

}