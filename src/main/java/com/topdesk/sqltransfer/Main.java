package com.topdesk.sqltransfer;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public final class Main {
	private static final String VERSION_ID = "0.9";
	
	static Logger logger = Logger.getLogger(Main.class);

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.println("Usage : see wrapper.conf or use: java Main <settings.xml> <optional runonce> <optional debug>");
			System.exit(1);
		}
		String fileName = args[0];
		Arrays.sort(args);
		boolean runOnce = Arrays.binarySearch(args, "runonce") > -1;
		boolean debug = Arrays.binarySearch(args, "debug") > -1;
        
		try {
			run(fileName, runOnce, debug);
		}
		catch (Exception e) {
			if (logger.getAppender(Main.class.toString()) != null) {
				logger.info("");
				logger.info(String.format("Error (%s): ", e.getClass()));
				logger.info("  " + e.getMessage());
				if (debug) {
					logger.error("", e);
				}
			}
			else {
				System.err.println(e);
			}
			System.exit(1);
		}
	}

	public static void run(String fileName, boolean runOnce, boolean debug) throws IOException, ClassNotFoundException, ParseException {
		File logProperties = getFile("etc/logging.properties");
		PropertyConfigurator.configure(logProperties.toURI().toURL());
		logConfiguration(fileName);
		
		SQLtransferXMLParser parser = new SQLtransferXMLParser(new File(fileName), null);
		SQLtransferDefinition definition = parser.createPrepareImportDefinition();
		
		SQLtransfer sqlTransfer = new SQLtransfer(definition, debug, false);
		if (!runOnce) {
			for (Date date : parser.gatherTimes()) {
				sqlTransfer.runLater(date);
			}
		}
		else {
			sqlTransfer.runNow();
		}
	}

	public static File getFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) {
			String baseDir = System.getProperty("basedir");
			if (baseDir != null && !baseDir.isEmpty()) { 
				file = new File(baseDir, fileName);
			}
		}
		if (!file.exists()) {
			throw new RuntimeException(String.format("Cannot find file %s.", file));
		}
		return file;
	}
    
	public static void logConfiguration(String filename) {
		logger.info("");
		logger.info(String.format("--- SQLtransfer (%s) started", VERSION_ID));
		logger.info("Using settings from: " + filename);
	}
	
	private Main() {
		// Prevent instantiation
	}
}