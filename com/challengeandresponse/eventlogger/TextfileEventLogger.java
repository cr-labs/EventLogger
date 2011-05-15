package com.challengeandresponse.eventlogger;

import java.io.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;


/**
 * logs server events.
 * 
 * A goal of this logger is to make it easy to write a very simple appended
 * log file. The constructor will throw an exception if the log file cannot be opened
 * as it needs to be. However, the other log-writing methods do not throw exceptions
 * on errors, to keep the calling code clean. Optionally, exceptionsToStderr can be
 * set true, and mid-course exceptions in writing/closing the file, will be written
 * there if possible. If false, they are swallowed because the last thing you want is
 * a logger that throws exceptions in the middle of other exception handling, or application
 * source that's full of try/catch to accommodate a logger.
 */

public class TextfileEventLogger implements EventLoggerI {

	private File eventFile;
	private boolean exceptionsToStderr;
	private boolean active;

	private BufferedWriter writer;	// for appending the file
	private RandomAccessFile raf; 	// for tailing the file via the getLastN() method
	
	// when tailing a file, buffer is this size
	private static final int 	BUF_LEN = 1000;
	private static final char	NEWLINE = '\n';
	

	/**
	 * Opens the event file for appending, at 'eventFilePath'
	 * @param eventFilePath the path to the event file
	 * @param exceptionsToStderr if true, exceptions encountered when trying to write the log file will be written to STDERR. If false, exceptions are swallowed
	 * @throws EventLoggerException
	 */
	public TextfileEventLogger(String eventFilePath, boolean exceptionsToStderr)
	throws EventLoggerException {
		// set the configuration specs for this db
		eventFile = new File(eventFilePath);
		this.exceptionsToStderr = exceptionsToStderr;
		this.active = true;

		try {
			// create the file, if it does not exist
			eventFile.createNewFile();
			if (! eventFile.canWrite())
				throw new EventLoggerException("Event file cannot be written to: "+eventFilePath);
			writer = new BufferedWriter(new FileWriter(eventFile,true));
			raf = new RandomAccessFile(eventFile,"r");
		}
		catch (IOException ioe) {
			this.active = false;
			if (exceptionsToStderr) 
				System.err.println("Error creating or opening event file for writing: "+eventFilePath);
			throw new EventLoggerException("Logger DEACTIVATED due to exception creating or opening event file for writing: "+eventFilePath);
		}
		addEvent("Logger started");
	}

	
	public void shutdown() {
		addEvent("Logger shutting down");
		try {
			writer.close();
		}
		catch (IOException ioe) {
			if (exceptionsToStderr)
				System.err.println("Exception closing writer: "+ioe.getMessage());
		}
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
	 * Convenience method to tdd an event to the event database, with a text message only, and no address
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
		if (! this.active)
			return "";
		
		String line = "";
		String parenAddress = "("+address.replace("(","").replace(")","")+")";
		try {
			line = ((new Date())+" "+parenAddress+" "+message);
			writer.write(line.trim()+NEWLINE);
			writer.flush();
			return line;
		} 
		catch (IOException ioe) {
			this.active = false;
			if (exceptionsToStderr)
				System.err.println("Logger DEACTIVATED due to exception adding event to file: "+ioe.getMessage());
			return line;
		}
	}



	/**
	 * Return the last N non-empty lines of server events from the log file. I said non-empty, not
	 * non-blank. Lines of length >= 1 are included in the returned kit.
	 * @param n how many to return
	 * @return the last N non-empty lines of the file, as a vector of Strings, in the same order as found in the file
	 */
	public Vector <String> getLastN(int n) {
		Vector <String> v = new Vector <String> ();

		try {
			byte[] buffer = new byte[BUF_LEN];
			long fileLen = raf.length();
			long seekPos = fileLen - Math.min(fileLen,BUF_LEN);
			StringBuilder sb = new StringBuilder();

			int bytesRead;
			raf.seek(seekPos);
			while ( (v.size() < n) && ((bytesRead = raf.read(buffer)) != -1)) {
				for (int i = bytesRead-1; i >= 0; i--) {
					if (buffer[i] == NEWLINE) {
						// empty lines are not included in the returned set
						if (sb.length() > 0)
							v.insertElementAt(sb.toString(),0);
						sb = new StringBuilder();
					}
					else {
						sb.insert(0,(char) buffer[i]);
					}
					// could run over the size limit within the first block, so check
					if (v.size() >= n)
						break;
				}
				// if we just processed the very beginning of the file, then 
				// there's no more work to be done. End the inner loop
				// after saving off the first line of the file
				if (seekPos == 0) {
					if (sb.length() > 0)
						v.insertElementAt(sb.toString(),0);
					break;
				}
				long oldSeekPos = seekPos;
				seekPos = Math.max(0,seekPos - BUF_LEN);
				raf.seek(seekPos);
				long seekLen = oldSeekPos - seekPos;
				buffer = new byte[(int) seekLen];
			}
		}
		catch (IOException ioe) {
			if (exceptionsToStderr)
				System.err.println("Exception reading file for getLastN: "+ioe.getMessage());
		}
		return v;
	}



	/** for testing */
	public static void main(String[] args)
	throws EventLoggerException {
		
		TextfileEventLogger tel = new TextfileEventLogger("/tmp/test.txt",true);
		System.out.println("adding event");
		tel.addEvent("Test event 1");
		
		System.out.println("\n\nDoing getLastN(10)\n\n");
		Vector <String> vv = tel.getLastN(3);
		
		Iterator <String> it = vv.iterator();
		while (it.hasNext())
			System.out.println(it.next());
		
		System.out.println("shutting down");
		tel.shutdown();		
	}

	
	

}