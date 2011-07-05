package com.buddycloud.channeldirectory.handler.common.mahout;

import org.apache.mahout.cf.taste.model.DataModel;

import com.buddycloud.channeldirectory.handler.response.ChannelData;

/**
 * Implementations should embed a Mahout data model,
 * and also should be responsible for translating 
 * Mahout-specific ids to jids. 
 * 
 */
public interface ChannelRecommenderDataModel {

	/**
	 * Returns a Mahout {@link DataModel}, 
	 * which is required by the recommender.
	 * 
	 * @return
	 */
	public DataModel getDataModel();

	/**
	 * Converts a user jid into a long id, 
	 * which is required by Mahout.
	 * 
	 * @param userJid
	 * @return
	 */
	public long toUserId(String userJid);

	/**
	 * Given the Mahout long id for an item,
	 * returns the respective channel data.
	 * 
	 * @param itemID
	 * @return
	 */
	public ChannelData toChannelData(long itemID);

	/**
	 * Converts a channel jid into a long id, 
	 * which is required by Mahout.
	 * 
	 * @param channelJid
	 * @return
	 */
	public long toChannelId(String channelJid);

	
}
