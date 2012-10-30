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
package com.buddycloud.channeldirectory.search.handler.recommendation;

import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.search.handler.common.ChannelQueryHandler;
import com.buddycloud.channeldirectory.search.handler.common.mahout.ChannelRecommender;
import com.buddycloud.channeldirectory.search.handler.common.mahout.RecommendationResponse;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;
import com.buddycloud.channeldirectory.search.rsm.MahoutRSMUtils;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.utils.XMPPUtils;

/**
 * Handles queries for user recommendation.
 * A query should contain an user jid, so
 * this handler can return recommended channels 
 * to this given user.
 *  
 */
public class RecommendationQueryHandler extends ChannelQueryHandler {

	private final ChannelRecommender recommender;
	private static final int DEFAULT_PAGE = 10;
	
	public RecommendationQueryHandler(Properties properties, ChannelRecommender recommender) {
		super("http://buddycloud.com/channel_directory/recommendation_query", properties);
		this.recommender = recommender;
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element userJidElement = queryElement.element("user-jid");
		
		if (userJidElement == null) {
			return XMPPUtils.error(iq, "Query does not contain user-jid element.", 
					getLogger());
		}
		
		String userJid = userJidElement.getText();
		
		if (userJid == null || userJid.isEmpty()) {
			return XMPPUtils.error(iq, "User-jid cannot be empty.", 
					getLogger());
		}
		
		RSM rsm = RSMUtils.parseRSM(queryElement);
		rsm.setMax(rsm.getMax() != null ? rsm.getMax() : DEFAULT_PAGE);
		
		List<ChannelData> recommendedChannels;
		try {
			recommendedChannels = findRecommendedChannels(userJid, rsm);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		List<ChannelData> recommendedChannelsFullData;
		try {
			recommendedChannelsFullData = retrieveFromSolr(recommendedChannels);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		return createIQResponse(iq, recommendedChannelsFullData, rsm);
	}

	private List<ChannelData> retrieveFromSolr(List<ChannelData> recommendedChannelsData) throws Exception {
		SolrServer solrServer = SolrServerFactory.createChannelCore(getProperties());
		List<ChannelData> channelsData = new LinkedList<ChannelData>();
		for (ChannelData recommendedChannelData : recommendedChannelsData) {
			SolrQuery solrQuery = new SolrQuery("jid:" + recommendedChannelData.getId());
			QueryResponse queryResponse = solrServer.query(solrQuery);
			ChannelData channelData = convertResponse(queryResponse);
			if (channelData == null) {
				throw new Exception("Could not retrieve channels metadata.");
			}
			channelsData.add(channelData);
		}
		return channelsData;
	}
	
	private static ChannelData convertResponse(QueryResponse queryResponse) {
		SolrDocumentList results = queryResponse.getResults();
		if (results.isEmpty()) {
			return null;
		}
		return convertDocument(results.iterator().next());
	}
	
	private static ChannelData convertDocument(SolrDocument solrDocument) {
		ChannelData channelData = new ChannelData();
		String latLonStr = (String) solrDocument.getFieldValue("geoloc");
		if (latLonStr != null) {
			String[] latLonSplit = latLonStr.split(",");
			channelData.setGeolocation(new Geolocation(
					Double.parseDouble(latLonSplit[0]), 
					Double.parseDouble(latLonSplit[1])));
		}
		
		channelData.setCreationDate((Date) solrDocument.getFieldValue("creation-date"));
		channelData.setChannelType((String) solrDocument.getFieldValue("channel-type"));
		channelData.setId((String) solrDocument.getFieldValue("jid"));
		channelData.setTitle((String) solrDocument.getFieldValue("title"));
		channelData.setDescription((String) solrDocument.getFieldValue("description"));
		
		return channelData;
	}
	
	private List<ChannelData> findRecommendedChannels(String search, RSM rsm)
			throws TasteException, SQLException {
		int howMany = MahoutRSMUtils.preprocess(rsm);
		RecommendationResponse response = recommender.recommend(search, howMany);
		return MahoutRSMUtils.postprocess(response, rsm);
	}
}
