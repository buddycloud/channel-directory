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
package com.buddycloud.channeldirectory.search.handler.content;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.search.handler.common.PostQueryHandler;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;
import com.buddycloud.channeldirectory.search.handler.response.PostData;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.rsm.SolrRSMUtils;
import com.buddycloud.channeldirectory.search.utils.XMPPUtils;

/**
 * Handles queries for content posts.
 * A query should contain a content query string, so
 * this handle can return channel posts related to this search.
 *  
 */
public class ContentQueryHandler extends PostQueryHandler {

	public ContentQueryHandler(Properties properties) {
		super("http://buddycloud.com/channel_directory/content_query", properties);
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element searchElement = queryElement.element("search");
		
		if (searchElement == null) {
			return XMPPUtils.error(iq, "Query does not contain search element.", 
					getLogger());
		}
		
		String search = searchElement.getText();
		
		if (search == null || search.isEmpty()) {
			return XMPPUtils.error(iq, "Search content cannot be empty.", 
					getLogger());
		}
		
		RSM rsm = RSMUtils.parseRSM(queryElement);
		List<PostData> relatedPosts;
		
		try {
			relatedPosts = findObjectsByContent(search, rsm);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		return createIQResponse(iq, relatedPosts, rsm);
	}

	private List<PostData> findObjectsByContent(String search, RSM rsm)
			throws MalformedURLException, SolrServerException {
		SolrServer solrServer = new SolrServerFactory().createPostCore(
				getProperties());
		SolrQuery solrQuery = new SolrQuery(search);
		solrQuery.setSortField("updated", ORDER.desc);
		
		SolrRSMUtils.preprocess(solrQuery, rsm);
		QueryResponse queryResponse = solrServer.query(solrQuery);
		SolrRSMUtils.postprocess(queryResponse, rsm);
		
		return convertResponse(queryResponse);
	}
	
	private static List<PostData> convertResponse(QueryResponse queryResponse) {
		List<PostData> channels = new ArrayList<PostData>();
		SolrDocumentList results = queryResponse.getResults();
		
		for (SolrDocument solrDocument : results) {
			channels.add(convertDocument(solrDocument));
		}
		
		return channels;
	}

	private static PostData convertDocument(SolrDocument solrDocument) {
		PostData postData = new PostData();
		
		String latLonStr = (String) solrDocument.getFieldValue("geoloc");
		String locStr = (String) solrDocument.getFieldValue("geoloc_text");
		
		if (latLonStr != null || locStr != null) {
			Geolocation geoLocation = new Geolocation();
			geoLocation.setText(locStr);
			
			if (latLonStr != null) {
				String[] latLonSplit = latLonStr.split(",");
				geoLocation.setLat(Double.parseDouble(latLonSplit[0]));
				geoLocation.setLng(Double.parseDouble(latLonSplit[1]));
			}
			
			postData.setGeolocation(geoLocation);
		}
		
		postData.setId((String) solrDocument.getFieldValue("id"));
		postData.setAuthor((String) solrDocument.getFieldValue("author"));
		postData.setContent((String) solrDocument.getFieldValue("content"));
		postData.setServerId((String) solrDocument.getFieldValue("server_id"));
		postData.setParentSimpleId((String) solrDocument.getFieldValue("parent_simpleid"));
		postData.setParentFullId((String) solrDocument.getFieldValue("parent_fullid"));
		postData.setInReplyTo((String) solrDocument.getFieldValue("inreplyto"));
		postData.setUpdated((Date) solrDocument.getFieldValue("updated"));
		postData.setPublished((Date) solrDocument.getFieldValue("published"));
		
		return postData;
	}

}
