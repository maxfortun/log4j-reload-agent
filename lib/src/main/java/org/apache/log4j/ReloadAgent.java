package org.apache.log4j;

import java.lang.instrument.Instrumentation;

import java.net.URL;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class ReloadAgent {
	private static Logger logger = Logger.getLogger(ReloadAgent.class);

	private static URL getURL() {
		String prop = System.getProperty("log4j.configuration");
		if (null == prop) {
			prop = "log4j.properties";
		}

		return ReloadAgent.class.getClassLoader().getResource(prop);
	}

	public static void premain(String args, Instrumentation instrumentation){
		URL url = getURL();
		if (!url.getProtocol().equalsIgnoreCase("file")) {
			throw new IllegalArgumentException("Can't watch "+url.toString()+". Not a file.");
		}
		logger.info("Watching "+url.toString());

		PropertyConfigurator.configureAndWatch(url.getFile());

		// shutdown log4j (and its monitor thread) on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down watching "+url.toString());
				LogManager.shutdown();
			}
		});

	}
}

