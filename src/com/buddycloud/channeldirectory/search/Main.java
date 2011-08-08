package com.buddycloud.channeldirectory.search;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.jivesoftware.whack.ExternalComponentManager;
import org.xmpp.component.ComponentException;

/**
 * Creates and starts the Channel Directory XMPP component.
 * It is also responsible for loading its properties and starting
 * the ComponentManager, that abstracts the XMPP connection.
 */
public class Main {

	private static final String CONFIGURATION_FILE = System.getenv("CHANNEL_DIRECTORY_HOME") + "/configuration.properties";
	private static Logger LOGGER = Logger.getLogger(Main.class);
	
	/**
	 * Starts the server
	 * @param args
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) {
		
		Properties configuration = new Properties();
		try {
			configuration.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (IOException e) {
			LOGGER.fatal("Configuration could not be loaded.", e);
		}
		
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
