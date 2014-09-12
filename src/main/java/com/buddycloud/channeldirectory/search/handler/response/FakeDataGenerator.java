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
