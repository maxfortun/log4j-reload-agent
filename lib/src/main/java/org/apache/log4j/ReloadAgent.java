package org.apache.log4j;

import java.io.File;

import java.lang.instrument.Instrumentation;

import java.net.URL;

import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class ReloadAgent {
	private static Logger logger = Logger.getLogger(ReloadAgent.class);
	private static CountDownLatch countDownLatch = new CountDownLatch(1);

	static {
		logger.trace("Init latch "+countDownLatch.getCount());
	}

	public static URL getURL() {
		String prop = System.getProperty("log4j.configuration");
		if (null == prop) {
			prop = "log4j.properties";
		}

		return ReloadAgent.class.getClassLoader().getResource(prop);
	}

	private static void premainImpl(String args, Instrumentation instrumentation) {
		URL url = getURL();
		if (!url.getProtocol().equalsIgnoreCase("file")) {
			throw new IllegalArgumentException("Can't watch "+url.toString()+". Not a file.");
		}

		File file = new File(url.getFile());
		logger.info("Watching "+file);

		PropertyConfigurator.configureAndWatch(file.getAbsolutePath(), 5000L);

		// shutdown log4j (and its monitor thread) on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down watching "+url.toString());
				LogManager.shutdown();
			}
		});

		logger.trace("Ready. "+countDownLatch.getCount());
		countDownLatch.countDown();
	}

	public static void await() throws Exception {
		logger.trace("Waiting for init to complete. " + countDownLatch.getCount());
		countDownLatch.await();
		logger.trace("Init complete. "+countDownLatch.getCount());
	}

	public static void premain(String args, Instrumentation instrumentation) {
		synchronized(ReloadAgent.class) {
			premainImpl(args, instrumentation);
		}
	}

}

