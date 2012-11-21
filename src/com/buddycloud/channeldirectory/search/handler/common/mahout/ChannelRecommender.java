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
package com.buddycloud.channeldirectory.search.handler.common.mahout;

import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender.MostSimilarEstimator;
import org.apache.mahout.cf.taste.impl.recommender.PreferredItemsNeighborhoodCandidateItemsStrategy;
import org.apache.mahout.cf.taste.impl.recommender.TopItems;
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.MostSimilarItemsCandidateItemsStrategy;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import com.buddycloud.channeldirectory.search.handler.response.ChannelData;

/**
 * Mahout based user-recommender for channels.
 * Uses the {@link GenericBooleanPrefUserBasedRecommender} with
 * a {@link LogLikelihoodSimilarity} for the user similarity. 
 * 
 */
public class ChannelRecommender {

	/**
	 * 
	 */
	private static final int MAX_CACHE_SIZE = 100000;
	private static final String POSTGRESQL_MODEL = "postgresql";
	private static final String INMEMORY_MODEL = "memory";
	
	private Recommender userRecommender;
	private ChannelRecommenderDataModel recommenderDataModel;
	
	private NearestNUserNeighborhood userNeighborhood;
	private LogLikelihoodSimilarity itemSimilarity;
	
	public ChannelRecommender(Properties properties) throws TasteException {
		
		this.recommenderDataModel = createDataModel(properties);
		DataModel dataModel = recommenderDataModel.getDataModel();

		UserSimilarity userSimilarity = new CachingUserSimilarity(
				new LogLikelihoodSimilarity(dataModel), MAX_CACHE_SIZE);
		this.userNeighborhood = new NearestNUserNeighborhood(10,
				Double.NEGATIVE_INFINITY, userSimilarity, dataModel, 1.0);
		this.userRecommender = new GenericBooleanPrefUserBasedRecommender(dataModel,
				userNeighborhood, userSimilarity);

		this.itemSimilarity = new LogLikelihoodSimilarity(dataModel);
	}

	private ChannelRecommenderDataModel createDataModel(Properties properties) {
		String recommenderModel = properties.getProperty("mahout.recommender");
		if (recommenderModel.equals(INMEMORY_MODEL)) {
			return new MemoryRecommenderDataModel(properties);
		}
		
		if (recommenderModel.equals(POSTGRESQL_MODEL)) {
			return new PostgreSQLRecommenderDataModel(properties);
		}
		
		return null;
	}

	/**
	 * Recommends a list of jids of channels that are
	 * related to the user taste.
	 * 
	 * @param userJid The user jid
	 * @param howMany The number of recommendations
	 * @return A list of recommended channels' jids 
	 * @throws TasteException
	 * @throws SQLException 
	 */
	public RecommendationResponse recommend(String userJid, int howMany)
			throws TasteException, SQLException {
		Long userId = recommenderDataModel.toUserId(userJid);
		
		if (userId == null) {
			return new RecommendationResponse(new LinkedList<ChannelData>(), 0);
		}
		
		List<RecommendedItem> recommended = userRecommender.recommend(
				userId, howMany);
		
		List<ChannelData> recommendedChannels = new LinkedList<ChannelData>();
		
		for (RecommendedItem recommendedItem : recommended) {
			recommendedChannels.add(recommenderDataModel.toChannelData(
					recommendedItem.getItemID()));
		}
		
		return new RecommendationResponse(recommendedChannels, 
				getPreferenceCount(userId));
	}

	/**
	 * Recommends a list of jids of channels that are
	 * similar to a given channel.
	 * 
	 * @param channelJid The channel jid
	 * @param howMany The number of recommendations
	 * @return A list of similar channels' jids 
	 * @throws TasteException
	 * @throws SQLException 
	 */
	public RecommendationResponse getSimilarChannels(String channelJid, int howMany)
			throws TasteException, SQLException {
		
		Long itemId = recommenderDataModel.toChannelId(channelJid);
		
		if (itemId == null) {
			return new RecommendationResponse(new LinkedList<ChannelData>(), 0);
		}
		
		TopItems.Estimator<Long> estimator = new MostSimilarEstimator(
				itemId, itemSimilarity, null);
		MostSimilarItemsCandidateItemsStrategy candidateStrategy = new PreferredItemsNeighborhoodCandidateItemsStrategy();
		
		FastIDSet possibleItemIDs = candidateStrategy.getCandidateItems(
				new long[] {itemId}, recommenderDataModel.getDataModel());
		List<RecommendedItem> recommended = TopItems.getTopItems(
				howMany, possibleItemIDs.iterator(), null, estimator);
		
		List<ChannelData> recommendedChannels = new LinkedList<ChannelData>();
		
		for (RecommendedItem recommendedItem : recommended) {
			recommendedChannels.add(recommenderDataModel.toChannelData(
					recommendedItem.getItemID()));
		}
		
		return new RecommendationResponse(recommendedChannels, 
				possibleItemIDs.size());
	}

	private int getPreferenceCount(long theUserId) throws TasteException {
		FastIDSet possibleItemIDs = new FastIDSet();
		long[] theNeighborhood = userNeighborhood.getUserNeighborhood(theUserId);
		DataModel dataModel = recommenderDataModel.getDataModel();
		
		for (long userID : theNeighborhood) {
			possibleItemIDs.addAll(dataModel.getItemIDsFromUser(userID));
		}
		possibleItemIDs.removeAll(dataModel.getItemIDsFromUser(theUserId));

		return possibleItemIDs.size();
	}
}