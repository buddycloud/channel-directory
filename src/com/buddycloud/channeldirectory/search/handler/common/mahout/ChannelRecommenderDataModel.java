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

import org.apache.mahout.cf.taste.model.DataModel;

import com.buddycloud.channeldirectory.search.handler.response.ChannelData;

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
	public Long toUserId(String userJid);

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
	public Long toChannelId(String channelJid);

	
}
