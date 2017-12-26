package com.strandls.naksha;

import java.io.File;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Various configurations needed by the web app
 * 
 * @author mukund
 */
public class NakshaConfig {

	private static Configuration config;

	private static final Logger logger = LoggerFactory.getLogger(NakshaConfig.class);

	static {
		Configurations configs = new Configurations();
		try {
			config = configs.properties(new File("config.properties"));
		} catch (ConfigurationException cex) {
			logger.error("Error while reading configuration. Message " + cex.getMessage());
		}
	}

	public static String getString(String key) {
		return config.getString(key);
	}

	public static int getInt(String key) {
		return config.getInt(key);
	}
}
