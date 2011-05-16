package com.buddycloud.channeldirectory.handler.response;

import java.util.ArrayList;
import java.util.List;


public class FakeData {

	public static List<Content> createData() {
		
		List<Content> fakeList = new ArrayList<Content>();
		
		Content object1 = new Content();
		object1.setGeolocation(new Geolocation(45.44, 12.33));
		object1.setJid("topicchanne01@example.org");
		object1.setTitle("A channel about topic 01");
		object1.setType(Content.TYPE_CHANNEL);
		fakeList.add(object1);
		
		Content object2 = new Content();
		object2.setGeolocation(new Geolocation(45.44, 12.33));
		object2.setJid("topicchanne02@example.org");
		object2.setTitle("A channel about topic 02");
		object2.setType(Content.TYPE_CHANNEL);
		fakeList.add(object2);
		
		return fakeList;
	}
	
}
