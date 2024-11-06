package org.apache.log4j;

import java.lang.instrument.Instrumentation;

import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

public class ReloadAgent {
	public static void premain(String args, Instrumentation instrumentation){
		String file = "";
		PropertyConfigurator.configureAndWatch(file);

		// shutdown log4j (and its monitor thread) on shutdown
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LogManager.shutdown();
			}
		});

	}
}

