package com.buddycloud.channeldirectory.search.handler.response;

import java.util.ArrayList;
import java.util.List;


public class FakeDataGenerator {

	public static List<ChannelData> createFakeChannels() {
		
		List<ChannelData> fakeList = new ArrayList<ChannelData>();
		
		ChannelData object1 = new ChannelData();
		object1.setGeolocation(new Geolocation(45.44, 12.33));
		object1.setId("topicchanne01@example.org");
		object1.setTitle("A channel about topic 01");
		fakeList.add(object1);
		
		ChannelData object2 = new ChannelData();
		object2.setGeolocation(new Geolocation(45.44, 12.33));
		object2.setId("topicchanne02@example.org");
		object2.setTitle("A channel about topic 02");
		fakeList.add(object2);
		
		return fakeList;
	}
	
	public static List<PostData> createFakePosts() {
		
		List<PostData> fakeList = new ArrayList<PostData>();
		return fakeList;
	}
	
}
