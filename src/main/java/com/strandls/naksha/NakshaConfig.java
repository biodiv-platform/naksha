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

	private static final Logger logger = LoggerFactory.getLogger(NakshaConfig.class);

	public static String getString(String key) {
		Configuration config;
		Configurations configs = new Configurations();
		String configFile = System.getenv("NAKSHA_CONFIG_PATH");
		try {
			config = configs.properties(new File(configFile != null ? configFile : "config.properties"));
			return config.getString(key);
		} catch (ConfigurationException cex) {
			logger.error("Error while reading configuration. Message {}", cex.getMessage());
		}
		return null;
	}

	public static int getInt(String key) {
		return Integer.parseInt(getString(key));
	}
}
