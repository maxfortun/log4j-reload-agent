package org.apache.log4j.reload;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

class ReloadAgentTest {
	private static Logger logger = Logger.getLogger(ReloadAgentTest.class);

	private File file;
	private Properties properties = new Properties();
	private String propertyName;

	public ReloadAgentTest() throws Exception {
		file = ReloadAgent.getFile();
		logger.debug("Waiting for "+file);
		while (!file.exists()) {
			Thread.sleep(100);
		}

		FileInputStream fileInputStream = new FileInputStream(file);
		properties.load(fileInputStream);
		fileInputStream.close();

		propertyName = "log4j.logger."+this.getClass().getName();
		logger.debug("Property: "+propertyName);
	}

	@Test void logManagerReloads() throws Exception {
		ReloadAgent.await();
		logManagerReloadsImpl();
	}

	private void setProperty(String value) throws Exception {
		logger.debug("Changing "+propertyName+": "+value);

		properties.setProperty(propertyName, value);

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		properties.store(fileOutputStream, "Switched to "+value);
		fileOutputStream.close();
	}

	private void waitForLevel(String level) throws Exception {
		long sleep = 1000;
		while(logger.getLevel() == null || !level.equals(logger.getLevel().toString())) {
			logger.debug("Waiting for level "+logger.getLevel()+" to change to "+level+" for "+sleep+"ms");
			Thread.sleep(sleep);
		}

		logger.debug("Level changed to: "+logger.getLevel());
	}

	void logManagerReloadsImpl() throws Exception {
		logger.debug("Initial level: "+logger.getLevel());

		setProperty("TRACE");
		waitForLevel("TRACE");

		setProperty("DEBUG");
		waitForLevel("DEBUG");

		//assertTrue(classUnderTest.someLibraryMethod(), "someLibraryMethod should return 'true'");
	}
}
