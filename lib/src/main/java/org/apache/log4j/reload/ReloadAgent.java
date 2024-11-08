package org.apache.log4j.reload;

import java.io.File;

import java.lang.instrument.Instrumentation;

import java.net.URI;
import java.net.URL;

import java.util.concurrent.CountDownLatch;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class ReloadAgent {
	private static Logger logger = Logger.getLogger(ReloadAgent.class);
	private static CountDownLatch countDownLatch = new CountDownLatch(1);

	private static Map<String, String> args = new HashMap<>();
	private static File file = null;

	static {
		logger.trace("Init latch "+countDownLatch.getCount());
	}

	public static File getFile() {
		return file;
	}

	private static void initArgs(final String stringArgs) {
		if(null == stringArgs) {
			return;
		}

		logger.trace("Init args: "+stringArgs);

		args.putAll(
			Stream.of(stringArgs.split(","))
				.map(String::trim)
				.filter(stringArg -> !stringArg.isEmpty())
				.map(stringArg -> stringArg.split("=", 2))
				.collect(Collectors.toMap(
					tokens -> tokens[0],
					tokens -> {
						if(tokens.length > 1) {
							return tokens[1];
						}
						return "true";
					}
				))
		);
	}

	private static String getFileName() {
		String fileName = args.get("file");
		if(null != fileName) {
			fileName = fileName.replaceAll("^[^:]+:","");
			logger.trace("File name from args: "+fileName);
			return fileName;
		}

		fileName = System.getProperty("log4j.configuration");
		if (null != fileName) {
			fileName = fileName.replaceAll("^[^:]+:","");
			logger.trace("File name from system property log4j.configuration: "+fileName);
			return fileName;
		}

		fileName = "log4j.properties";
		logger.trace("File name from defaults: "+fileName);
		return fileName;
	}

	public static void initFile(String stringArgs) throws Exception {
		URL url = ReloadAgent.class.getClassLoader().getResource(getFileName());
		file = new File(url.getFile());

		if (!file.exists()) {
			throw new IllegalArgumentException(file+" does not exist.");
		}

		logger.info("Watching: "+file);
	}

	private static void premainImpl(String stringArgs, Instrumentation instrumentation) throws Exception {
		initArgs(stringArgs);
		initFile(stringArgs);

		PropertyConfigurator.configureAndWatch(file.getAbsolutePath(), 5000L);

		// shutdown log4j (and its monitor thread) on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				logger.info("Shutting down watching "+file);
				LogManager.shutdown();
			}
		});

		countDownLatch.countDown();
		logger.trace("Ready. "+countDownLatch.getCount());
	}

	public static void await() throws Exception {
		logger.trace("Waiting for init to complete. " + countDownLatch.getCount());
		countDownLatch.await();
		logger.trace("Init complete. "+countDownLatch.getCount());
	}

	public static void premain(String stringArgs, Instrumentation instrumentation) throws Exception {
		try {
			premainImpl(stringArgs, instrumentation);
		} catch(Exception e) {
			if(Boolean.parseBoolean(args.get("fail"))) {
				throw e;
			}
			logger.error("Failed to initialize.", e);
		}
	}

}

