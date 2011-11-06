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
package com.buddycloud.channeldirectory.crawler;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.pubsub.PubSubManager;

/**
 * Simply maintain a collection of {@link PubSubManager}, 
 * so different crawling strategies can use the same
 * node cache.
 * 
 */
public class PubSubManagers {

	private final Map<String, PubSubManager> pubSubManagers = new HashMap<String, PubSubManager>();
	private final Connection connection;
	
	public PubSubManagers(Connection connection) {
		this.connection = connection;
	}

	public PubSubManager getPubSubManager(String pubSubServer) {
		PubSubManager pubSubManager = pubSubManagers.get(pubSubServer);
		if (pubSubManager == null) {
			pubSubManager = new PubSubManager(connection, pubSubServer);
			pubSubManagers.put(pubSubServer, pubSubManager);
		}
		return pubSubManager;
	}

	/**
	 * @return
	 */
	public Connection getConnection() {
		return connection;
	}
	
}
