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
package com.buddycloud.channeldirectory.search.handler.similarity;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.search.handler.common.ChannelQueryHandler;
import com.buddycloud.channeldirectory.search.handler.common.mahout.ChannelRecommender;
import com.buddycloud.channeldirectory.search.handler.common.mahout.RecommendationResponse;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
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
public class SimilarityQueryHandler extends ChannelQueryHandler {

	private static final int DEFAULT_PAGE = 10;
	private final ChannelRecommender recommender;

	public SimilarityQueryHandler(Properties properties, ChannelRecommender recommender) {
		super("http://buddycloud.com/channel_directory/similar_channels", properties);
		this.recommender = recommender;
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element channelJidElement = queryElement.element("channel-jid");
		
		if (channelJidElement == null) {
			return XMPPUtils.error(iq, "Query does not contain channel-jid element.", 
					getLogger());
		}
		
		String channelJid = channelJidElement.getText();
		
		if (channelJid == null || channelJid.isEmpty()) {
			return XMPPUtils.error(iq, "Channel-jid cannot be empty.", 
					getLogger());
		}
		
		RSM rsm = RSMUtils.parseRSM(queryElement);
		rsm.setMax(rsm.getMax() != null ? rsm.getMax() : DEFAULT_PAGE);
		
		List<ChannelData> similarChannels;
		try {
			similarChannels = findSimilarChannels(channelJid, rsm);
		} catch (Exception e) {
			return XMPPUtils.error(iq, 
					"Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		List<ChannelData> similarChannelsFullData;
		try {
			similarChannelsFullData = retrieveFromSolr(similarChannels);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					e, getLogger());
		}
		
		return createIQResponse(iq, similarChannelsFullData, rsm);
	}

	private List<ChannelData> findSimilarChannels(String search, RSM rsm)
			throws TasteException, SQLException {
		int howMany = MahoutRSMUtils.preprocess(rsm);
		RecommendationResponse recommendationResponse = recommender.getSimilarChannels(search, howMany);
		return MahoutRSMUtils.postprocess(recommendationResponse, rsm);
	}
}
