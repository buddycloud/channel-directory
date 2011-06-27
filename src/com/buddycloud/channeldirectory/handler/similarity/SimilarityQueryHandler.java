package com.buddycloud.channeldirectory.handler.similarity;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.solr.client.solrj.SolrServerException;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.ChannelQueryHandler;
import com.buddycloud.channeldirectory.handler.recommendation.ChannelRecommender;
import com.buddycloud.channeldirectory.handler.response.ChannelData;
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
		
		List<ChannelData> recommendedChannels;
		try {
			recommendedChannels = findSimilarChannels(channelJid);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					getLogger());
		}
		
		return createIQResponse(iq, recommendedChannels);
	}

	private List<ChannelData> findSimilarChannels(String search)
			throws MalformedURLException, SolrServerException, TasteException {
		return recommender.getSimilarChannels(search, 10);
	}
}
