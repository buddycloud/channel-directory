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
package com.buddycloud.channeldirectory.cli;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * @author Abmar
 *
 */
public class QueryToDBMS implements Query {

	private String sql;

	public QueryToDBMS(String sql) {
		this.sql = sql;
	}

	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.cli.Query#exec(java.lang.String, java.util.Properties)
	 */
	@Override
	public String exec(String args, Properties configuration) throws Exception {
		
		ChannelDirectoryDataSource dataSource = new ChannelDirectoryDataSource(configuration);
		PreparedStatement statement = null;
		try {
			statement = dataSource.prepareStatement(sql);
			ResultSet resultSet = statement.executeQuery();
			if (!resultSet.next()) {
				return "";
			}
			return resultSet.getString(1);
			
		} catch (SQLException e1) {
			throw e1;
		} finally {
			ChannelDirectoryDataSource.close(statement);
			DataSources.destroy(dataSource.getDataSource());
		}
	}

}
