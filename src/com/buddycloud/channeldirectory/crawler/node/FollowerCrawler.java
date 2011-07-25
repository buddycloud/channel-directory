package com.buddycloud.channeldirectory.crawler.node;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;

import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.Subscription;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class FollowerCrawler implements NodeCrawler {

	private Properties configuration;
	private ComboPooledDataSource dataSource;
	
	public FollowerCrawler(Properties configuration) {
		this.configuration = configuration;
		setupDataSource();
	}

	private void setupDataSource() {
		this.dataSource = new ComboPooledDataSource();
		dataSource.setJdbcUrl(configuration.getProperty("jdbc.url"));
		try {
			dataSource.setDriverClass("org.postgresql.Driver");
		} catch (PropertyVetoException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void crawl(Node node) throws Exception {
		List<Subscription> subscriptions = node.getSubscriptions();
		
		String item = node.getId();
		
		for (Subscription subscription : subscriptions) {
		
			String user = subscription.getJid();
			
			Connection connection = dataSource.getConnection();
			
			Long userId = fetchRowId(user, "t_user", connection);
			Long itemId = fetchRowId(item, "item", connection);
			
			Statement selectTasteSt = connection.createStatement();
			ResultSet selectTasteResult = selectTasteSt.executeQuery(
					"SELECT * FROM taste_preferences WHERE user_id = '" + userId + "' " +
					"AND item_id = '" + itemId + "'");
			
			if (!selectTasteResult.next()) {
				Statement insertTasteSt = connection.createStatement();
				insertTasteSt.execute("INSERT INTO taste_preferences(user_id, item_id) " +
						"VALUES ('" + userId + "', '" + itemId + "')");
				insertTasteSt.close();
			}
			
			selectTasteResult.close();
			selectTasteSt.close();
			connection.close();
		}
		
	}

	private static Long fetchRowId(String user, String tableName, Connection connection)
			throws SQLException {
		
		Statement selectRowSt = connection.createStatement();
		ResultSet selectRowResult = selectRowSt.executeQuery(
				"SELECT id FROM " + tableName + " WHERE jid = '" + user + "'");
		Long userId = null;
		
		if (selectRowResult.next()) {
			userId = selectRowResult.getLong("id");
		} else {
			Statement insertRowSt = connection.createStatement();
			insertRowSt.execute("INSERT INTO " + tableName + "(jid) VALUES ('" + user + "') RETURNING id");
			
			ResultSet insertUserResult = insertRowSt.getResultSet();
			insertUserResult.next();
			userId = insertUserResult.getLong("id");
			
			insertUserResult.close();
			insertRowSt.close();
		}
		
		selectRowResult.close();
		selectRowSt.close();
		return userId;
	}

}
