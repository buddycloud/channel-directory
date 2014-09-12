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

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import au.com.bytecode.opencsv.CSVReader;

import com.buddycloud.channeldirectory.search.handler.response.ChannelData;

/**
 * Reads a dump from a CSV file and stores all taste 
 * data in memory. The CSV file path is addressed by the 
 * "mahout.dumpfile" property. This is a model for testing purposes
 * and it is static.
 * 
 */
public class MemoryRecommenderDataModel implements ChannelRecommenderDataModel {

	private Map<String, Long> userToId = new HashMap<String, Long>();
	private Map<String, Long> itemToId = new HashMap<String, Long>();

	private Map<Long, String> idToUser = new HashMap<Long, String>();
	private Map<Long, ChannelData> idToItem = new HashMap<Long, ChannelData>();
	
	private DataModel dataModel;
	private Properties properties;

	public MemoryRecommenderDataModel(Properties properties) {
		this.properties = properties;
		try {
			createDataModel();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void createDataModel() throws IOException {
		CSVReader reader = new CSVReader(new FileReader(
				properties.getProperty("mahout.dumpfile")));
		reader.readNext(); // Read header

		Map<Long, List<Preference>> preferences = new HashMap<Long, List<Preference>>();

		Long userId = -1L;
		Long itemId = -1L;

		while (true) {

			String[] nextLine = reader.readNext();
			if (nextLine == null) {
				break;
			}

			String item = nextLine[0];
			String title = nextLine[1];
			String user = nextLine[2];

			if (!userToId.containsKey(user)) {
				userId++;
				userToId.put(user, userId);
				idToUser.put(userId, user);
			}

			if (!itemToId.containsKey(item)) {
				itemId++;
				ChannelData chData = new ChannelData();
				chData.setId(item);
				chData.setTitle(title);

				itemToId.put(item, itemId);
				idToItem.put(itemId, chData);
			}

			Long currentUserId = userToId.get(user);
			BooleanPreference booleanPreference = new BooleanPreference(
					currentUserId, itemToId.get(item));

			List<Preference> prefList = preferences.get(currentUserId);

			if (prefList == null) {
				prefList = new LinkedList<Preference>();
				preferences.put(currentUserId, prefList);
			}

			prefList.add(booleanPreference);
		}

		FastByIDMap<PreferenceArray> userData = new FastByIDMap<PreferenceArray>();
		for (Entry<Long, List<Preference>> entry : preferences.entrySet()) {
			userData.put(entry.getKey(), new BooleanUserPreferenceArray(entry.getValue()));
		}

		this.dataModel = new GenericDataModel(userData);
	}
	

	@Override
	public DataModel getDataModel() {
		return dataModel;
	}

	@Override
	public Long toUserId(String userJid) {
		return userToId.get(userJid);
	}

	@Override
	public ChannelData toChannelData(long itemID) {
		return idToItem.get(itemID);
	}

	@Override
	public Long toChannelId(String channelJid) {
		return itemToId.get(channelJid);
	}

}
