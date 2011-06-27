package com.buddycloud.channeldirectory.handler.recommendation;

import java.util.LinkedList;
import java.util.List;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.buddycloud.channeldirectory.handler.response.ChannelData;

/**
 * Mahout based user-recommender for channels.
 * Uses the {@link GenericBooleanPrefUserBasedRecommender} with
 * a {@link LogLikelihoodSimilarity} for the user similarity. 
 * 
 */
public class ChannelRecommender {

	private Recommender userRecommender;
	private GenericItemBasedRecommender itemRecommender;
	private ChannelRecommenderDumpDataModel channelDataModel = new ChannelRecommenderDumpDataModel();
	
	public ChannelRecommender() throws TasteException {
		DataModel dataModel = channelDataModel.getDataModel();
		UserSimilarity userSimilarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(dataModel), dataModel);
		UserNeighborhood neighborhood =
			new NearestNUserNeighborhood(10, Double.NEGATIVE_INFINITY, userSimilarity, dataModel, 1.0);
		userRecommender = new GenericBooleanPrefUserBasedRecommender(dataModel, neighborhood, userSimilarity);
		
		ItemSimilarity itemSimilarity = new LogLikelihoodSimilarity(dataModel);
		itemRecommender = new GenericBooleanPrefItemBasedRecommender(
				dataModel, itemSimilarity);
	}

	/**
	 * Recommends a list of jids of channels that are
	 * related to the user taste.
	 * 
	 * @param userID The user jid
	 * @param howMany The number of recommendations
	 * @return A list of recommended channels' jids 
	 * @throws TasteException
	 */
	public List<ChannelData> recommend(String userID, int howMany) throws TasteException {
		List<RecommendedItem> recommended = userRecommender.recommend(
				channelDataModel.toUserId(userID), howMany);
		
		List<ChannelData> recommendedChannels = new LinkedList<ChannelData>();
		
		for (RecommendedItem recommendedItem : recommended) {
			recommendedChannels.add(channelDataModel.toChannelData(
					recommendedItem.getItemID()));
		}
		
		return recommendedChannels;
	}
	
	/**
	 * Recommends a list of jids of channels that are
	 * similar to a given channel.
	 * 
	 * @param channelJid The channel jid
	 * @param howMany The number of recommendations
	 * @return A list of similar channels' jids 
	 * @throws TasteException
	 */
	public List<ChannelData> getSimilarChannels(String channelJid, int howMany) throws TasteException {
		List<RecommendedItem> recommended = itemRecommender.mostSimilarItems(
				channelDataModel.toChannelId(channelJid), howMany);
		
		List<ChannelData> recommendedChannels = new LinkedList<ChannelData>();
		
		for (RecommendedItem recommendedItem : recommended) {
			recommendedChannels.add(channelDataModel.toChannelData(
					recommendedItem.getItemID()));
		}
		
		return recommendedChannels;
	}
	
}
