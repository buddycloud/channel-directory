package com.buddycloud.channeldirectory.handler.similarity;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.common.ChannelQueryHandler;
import com.buddycloud.channeldirectory.handler.common.mahout.ChannelRecommender;
import com.buddycloud.channeldirectory.handler.common.mahout.RecommendationResponse;
import com.buddycloud.channeldirectory.handler.response.ChannelData;
import com.buddycloud.channeldirectory.rsm.MahoutRSMUtils;
import com.buddycloud.channeldirectory.rsm.RSM;
import com.buddycloud.channeldirectory.rsm.RSMUtils;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for user recommendation.
 * A query should contain an user jid, so
 * this handler can return recommended channels 
 * to this given user.
 *  
 */
public class SimilarityQueryHandler extends ChannelQueryHandler {

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
		List<ChannelData> recommendedChannels;
		try {
			recommendedChannels = findSimilarChannels(channelJid, rsm);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					getLogger());
		}
		
		return createIQResponse(iq, recommendedChannels, rsm);
	}

	private List<ChannelData> findSimilarChannels(String search, RSM rsm)
			throws TasteException, SQLException {
		int howMany = MahoutRSMUtils.preprocess(rsm);
		RecommendationResponse recommendationResponse = recommender.getSimilarChannels(search, howMany);
		return MahoutRSMUtils.postprocess(recommendationResponse, rsm);
	}
}
