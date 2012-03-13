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
package com.buddycloud.channeldirectory.crawler.node;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;

/**
 *
 */
public class CrawlerHelper {

	private static Logger LOGGER = Logger.getLogger(CrawlerHelper.class);
	
	/**
	 * @param user
	 * @throws SQLException 
	 */
	public static void enqueueNewServer(String user, 
			ChannelDirectoryDataSource dataSource) {
		
		String server = user.substring(user.indexOf('@') + 1);
		
		PreparedStatement statement = null;
		
		try {
			statement = dataSource.prepareStatement(
					"INSERT INTO subscribed_server(name) values (?)", 
					server);
			statement.execute();
		} catch (SQLException e) {
			LOGGER.warn("Server already inserted " + server);
		} finally {
			if (statement != null) {
				ChannelDirectoryDataSource.close(statement);
			}
		}
	}

	public static String getNodeId(String nodeFullJid) {
		String[] nodeFullJidSplitted = nodeFullJid.split("/");
		
		if (nodeFullJidSplitted.length < 4) {
			return null;
		}
		
		String nodeId = nodeFullJidSplitted[2];
		return nodeId;
	}

}
