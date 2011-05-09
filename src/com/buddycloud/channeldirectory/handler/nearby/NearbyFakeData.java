package com.buddycloud.channeldirectory.handler.nearby;

import java.util.LinkedList;
import java.util.List;

public class NearbyFakeData {

	public static List<NearbyObject> createData() {
		
		List<NearbyObject> fakeList = new LinkedList<NearbyObject>();
		
		NearbyObject object1 = new NearbyObject();
		object1.setGeolocation(new Geolocation(45.44, 12.33));
		object1.setJid("topicchanne01@example.org");
		object1.setTitle("A channel about topic 01");
		object1.setType(NearbyObject.TYPE_CHANNEL);
		fakeList.add(object1);
		
		NearbyObject object2 = new NearbyObject();
		object2.setGeolocation(new Geolocation(45.44, 12.33));
		object2.setJid("topicchanne02@example.org");
		object2.setTitle("A channel about topic 02");
		object2.setType(NearbyObject.TYPE_CHANNEL);
		fakeList.add(object2);
		
		return fakeList;
	}
	
}
