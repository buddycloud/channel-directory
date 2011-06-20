package com.buddycloud.channeldirectory.handler.recommendation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.IDRescorer;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.buddycloud.channeldirectory.handler.response.ChannelData;

/**
 * Mahout based user-recommender for channels.
 * Uses the {@link GenericBooleanPrefUserBasedRecommender} with
 * a {@link LogLikelihoodSimilarity} for the user similarity. 
 * 
 */
public class ChannelRecommender implements Recommender {

	private Recommender recommender;
	private ChannelRecommenderDumpDataModel channelDataModel = new ChannelRecommenderDumpDataModel();
	
	public ChannelRecommender() throws TasteException {
		DataModel dataModel = channelDataModel.getDataModel();
		UserSimilarity similarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(dataModel), dataModel);
		UserNeighborhood neighborhood =
			new NearestNUserNeighborhood(10, Double.NEGATIVE_INFINITY, similarity, dataModel, 1.0);
		recommender = new GenericBooleanPrefUserBasedRecommender(dataModel, neighborhood, similarity);
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
		List<RecommendedItem> recommended = recommender.recommend(
				channelDataModel.toUserId(userID), howMany);
		
		List<ChannelData> recommendedChannels = new LinkedList<ChannelData>();
		
		for (RecommendedItem recommendedItem : recommended) {
			recommendedChannels.add(channelDataModel.toChannelData(
					recommendedItem.getItemID()));
		}
		
		return recommendedChannels;
	}
	
	@Override
	public List<RecommendedItem> recommend(long userID, int howMany) throws TasteException {
		return recommender.recommend(userID, howMany);
	}

	@Override
	public List<RecommendedItem> recommend(long userID, int howMany, IDRescorer rescorer) throws TasteException {
		return recommender.recommend(userID, howMany, rescorer);
	}

	@Override
	public float estimatePreference(long userID, long itemID) throws TasteException {
		return recommender.estimatePreference(userID, itemID);
	}

	@Override
	public void setPreference(long userID, long itemID, float value) throws TasteException {
		recommender.setPreference(userID, itemID, value);
	}

	@Override
	public void removePreference(long userID, long itemID) throws TasteException {
		recommender.removePreference(userID, itemID);
	}

	@Override
	public DataModel getDataModel() {
		return recommender.getDataModel();
	}

	@Override
	public void refresh(Collection<Refreshable> alreadyRefreshed) {
		recommender.refresh(alreadyRefreshed);
	}

}
