package com.buddycloud.channeldirectory.crawler;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

/**
 * Creates and starts the Crawler component.
 * 
 */
public class Main {

	private static final String CONFIGURATION_FILE = "configuration.properties";
	private static Logger LOGGER = Logger.getLogger(Main.class);
	
	/**
	 * Starts the crawler
	 * @param args
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException {
		Properties configuration = new Properties();
		try {
			configuration.load(new FileInputStream(CONFIGURATION_FILE));
		} catch (IOException e) {
			LOGGER.fatal("Configuration could not be loaded.", e);
		}
		
		String solrChannelUrl = (String) configuration.get("solr.channelcore");
		if (solrChannelUrl == null) {
			LOGGER.fatal("Solr channels' core URL must be set.");
		}
		
		SolrServer server = new CommonsHttpSolrServer(solrChannelUrl);
		List<SolrInputDocument> createFakeSolrDocuments = createFakeSolrChannels();
		
		try {
			server.deleteByQuery("*:*");
			server.commit();
			
			server.add(createFakeSolrDocuments);
			server.commit();
		} catch (Exception e) {
			LOGGER.warn("Channels' documents could not be added to Solr server.", e);
		}
	}
	
	private static List<SolrInputDocument> createFakeSolrChannels() {
		List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();
		
		SolrInputDocument object1 = new SolrInputDocument();
		object1.addField("geoloc", "45.44,12.33");
		object1.addField("jid", "topicchanne01@example.org");
		object1.setField("title", "A channel about topic 01");
		documents.add(object1);
		
		SolrInputDocument object2 = new SolrInputDocument();
		object2.addField("geoloc", "45.44,-12.33");
		object2.addField("jid", "topicchanne02@example.org");
		object2.setField("title", "A channel about topic 02");
		documents.add(object2);
		
		return documents;
	}
}
