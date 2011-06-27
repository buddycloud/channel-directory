package com.buddycloud.channeldirectory.handler.recommendation;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.BooleanPreference;
import org.apache.mahout.cf.taste.impl.model.BooleanUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.Preference;
import org.apache.mahout.cf.taste.model.PreferenceArray;

import com.buddycloud.channeldirectory.handler.response.ChannelData;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Encapsulates a Mahout {@link DataModel}, required
 * by the {@link ChannelRecommender}. It loads the taste data 
 * from a CSV dump.
 * 
 * The header of the CVS dump must comply the following:
 * "nodename","title","jid","subscription","affiliation" 
 *  
 */
public class ChannelRecommenderDumpDataModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Map<String, Long> userToId = new HashMap<String, Long>();
	private Map<String, Long> itemToId = new HashMap<String, Long>();
	
	private Map<Long, String> idToUser = new HashMap<Long, String>();
	private Map<Long, ChannelData> idToItem = new HashMap<Long, ChannelData>();

	private GenericDataModel dataModel;

	public ChannelRecommenderDumpDataModel() {
		try {
			initModel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void initModel() throws IOException {
		CSVReader reader = new CSVReader(new FileReader("resources/channel-taste/dump.csv"));
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

	public GenericDataModel getDataModel() {
		return dataModel;
	}

	/**
	 * Converts a user jid into a long id, 
	 * which is required by Mahout.
	 * 
	 * @param userJid
	 * @return
	 */
	public long toUserId(String userJid) {
		return userToId.get(userJid);
	}

	/**
	 * Given the Mahout long id for an item,
	 * returns the respective channel data.
	 * 
	 * @param itemID
	 * @return
	 */
	public ChannelData toChannelData(long itemID) {
		return idToItem.get(itemID);
	}

	/**
	 * Converts a channel jid into a long id, 
	 * which is required by Mahout.
	 * 
	 * @param channelJid
	 * @return
	 */
	public long toChannelId(String channelJid) {
		return itemToId.get(channelJid);
	}

}
