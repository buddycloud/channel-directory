package com.buddycloud.channeldirectory.handler.common.solr;

import java.net.MalformedURLException;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

public class SolrServerFactory {

	private static final String SOLR_CHANNELCORE_PROP = "solr.channelcore";
	private static final String SOLR_POSTCORE_PROP = "solr.postcore";

	private static SolrServer createSolrCore(Properties properties,
			String coreProperty) throws MalformedURLException {
		String solrChannelUrl = (String) properties.get(coreProperty);
		return new CommonsHttpSolrServer(solrChannelUrl);
	}

	public static SolrServer createChannelCore(Properties properties)
			throws MalformedURLException {
		return createSolrCore(properties, SOLR_CHANNELCORE_PROP);
	}

	public static SolrServer createPostCore(Properties properties)
			throws MalformedURLException {
		return createSolrCore(properties, SOLR_POSTCORE_PROP);
	}

}
