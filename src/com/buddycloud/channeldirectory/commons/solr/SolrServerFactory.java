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
package com.buddycloud.channeldirectory.commons.solr;

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
