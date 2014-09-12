/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buddycloud.channeldirectory.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * General utils for accessing the engine configuration
 * 
 */
public class ConfigurationUtils {
	
	private static final String CONFIGURATION_FILE = getChannelDirHome() 
			+ "/configuration.properties";
	
	private static Logger LOGGER = Logger.getLogger(ConfigurationUtils.class);
	
	public static Properties loadConfiguration() throws IOException {
		Properties configuration = new Properties();
		try {
			configuration.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (IOException e) {
			LOGGER.fatal("Configuration could not be loaded.", e);
			throw e;
		}
		return configuration;
	}

	
	public static String getChannelDirHome() {
		String channelDirHome = System.getenv("CHANNEL_DIRECTORY_HOME");
		return channelDirHome == null ? "." : channelDirHome;
	}
}
