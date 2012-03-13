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
package com.buddycloud.channeldirectory.search;

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.whack.ExternalComponentManager;
import org.xmpp.component.ComponentException;

import com.buddycloud.channeldirectory.commons.ConfigurationUtils;

/**
 * Creates and starts the Channel Directory XMPP component.
 * It is also responsible for loading its properties and starting
 * the ComponentManager, that abstracts the XMPP connection.
 */
public class Main {

	private static Logger LOGGER = Logger.getLogger(Main.class);
	
	/**
	 * Starts the server
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws IOException {
		
		Properties configuration = ConfigurationUtils.loadConfiguration();
		
		ExternalComponentManager componentManager = new ExternalComponentManager(
				configuration.getProperty("xmpp.host"),
				Integer.valueOf(configuration.getProperty("xmpp.port")));
		
		String subdomain = configuration.getProperty("xmpp.subdomain");
		componentManager.setSecretKey(subdomain, 
				configuration.getProperty("xmpp.secretkey"));
		
		try {
			componentManager.addComponent(subdomain, 
					new ChannelDirectoryComponent(configuration));
		} catch (ComponentException e) {
			LOGGER.fatal("Component could not be started.", e);
		}
		
		
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				LOGGER.fatal("Main loop.", e);
			}
		}
	}
	
}
