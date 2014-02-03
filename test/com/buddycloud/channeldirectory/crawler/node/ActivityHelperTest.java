package com.buddycloud.channeldirectory.crawler.node;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Properties;

import junit.framework.Assert;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.SolrParams;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.buddycloud.HSQLDBTest;
import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;
import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.search.handler.response.PostData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class ActivityHelperTest extends HSQLDBTest {

	private static final Long ONE_HOUR = 1000L * 60L * 60L;
	
	private SolrServerFactory solrFactory;
	private SolrServer solrServer;
	
	@Before
	public void setUp() throws Exception {
		solrFactory = Mockito.mock(SolrServerFactory.class);
		solrServer = Mockito.mock(SolrServer.class);
		Mockito.when(solrFactory.createChannelCore(
				Mockito.any(Properties.class))).thenReturn(solrServer);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testNullPostData() {
		ActivityHelper.updateActivity(null, null, null);
	}
	
	@Test
	public void testNoChannelInPostData() throws SQLException {
		ActivityHelper.updateActivity(new PostData(), getDataSource(), null);
		assertDBEmpty();
	}
	
	@Test
	public void testNotRegisteredChannelInPostData() throws Exception {
		PostData postData = new PostData();
		postData.setParentSimpleId("whatever@whatever.com");
		
		recordChannelSolrQuery();
		ActivityHelper.updateActivity(postData, getDataSource(), null, solrFactory);
		
		assertDBEmpty();
	}

	@Test
	public void testChannelWithNoPreviousActivity() throws Exception {
		long postTimestampInHours = 100;
		long postTimestamp = ONE_HOUR * postTimestampInHours;
		
		PostData postData = new PostData();
		postData.setParentSimpleId("whatever@whatever.com");
		postData.setPublished(new Date(postTimestamp));
		
		SolrDocumentList sdl = recordChannelSolrQuery();
		sdl.add(new SolrDocument());
		ActivityHelper.updateActivity(postData, getDataSource(), null, solrFactory);
		
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		
		Assert.assertTrue(resultSet.next());
		Assert.assertEquals("whatever@whatever.com", 
				resultSet.getString("channel_jid"));
		
		JsonArray allActivity = new JsonArray();
		
		JsonObject activityInThisHour = new JsonObject();
		activityInThisHour.addProperty("p", postTimestampInHours);
		activityInThisHour.addProperty("a", 1);
		allActivity.add(activityInThisHour);
		
		for (int i = 1; i < ActivityHelper.MAX_WINDOW_SIZE; i++) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours - i);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		Assert.assertEquals(allActivity.toString(), 
				resultSet.getString("detailed_activity"));
		
		Assert.assertEquals(1, 
				resultSet.getLong("summarized_activity"));
		Assert.assertEquals(new Timestamp(postTimestamp), 
				resultSet.getTimestamp("updated"));
		Assert.assertEquals(new Timestamp(postTimestamp), 
				resultSet.getTimestamp("earliest"));
		
		ChannelDirectoryDataSource.close(st);
	}

	@Test
	public void testChannelPreviousActivityInSameHour() throws Exception {
		long postTimestampInHours = 100;
		long oldPostTimestamp = ONE_HOUR * postTimestampInHours;
		
		SolrDocumentList sdl = recordChannelSolrQuery();
		sdl.add(new SolrDocument());
		
		PostData newPostData = new PostData();
		newPostData.setParentSimpleId("whatever@whatever.com");
		newPostData.setPublished(new Date(oldPostTimestamp + 1));
		ActivityHelper.updateActivity(newPostData, getDataSource(), null, solrFactory);
		
		PostData oldPostData = new PostData();
		oldPostData.setParentSimpleId("whatever@whatever.com");
		oldPostData.setPublished(new Date(oldPostTimestamp));
		ActivityHelper.updateActivity(oldPostData, getDataSource(), null, solrFactory);
		
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		
		Assert.assertTrue(resultSet.next());
		Assert.assertEquals("whatever@whatever.com", 
				resultSet.getString("channel_jid"));
		
		JsonArray allActivity = new JsonArray();
		
		JsonObject activityInThisHour = new JsonObject();
		activityInThisHour.addProperty("p", postTimestampInHours);
		activityInThisHour.addProperty("a", 2);
		allActivity.add(activityInThisHour);
		
		for (int i = 1; i < ActivityHelper.MAX_WINDOW_SIZE; i++) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours - i);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		Assert.assertEquals(allActivity.toString(), 
				resultSet.getString("detailed_activity"));
		Assert.assertEquals(2, 
				resultSet.getLong("summarized_activity"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp + 1), 
				resultSet.getTimestamp("updated"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp), 
				resultSet.getTimestamp("earliest"));
		
		ChannelDirectoryDataSource.close(st);
	}
	
	@Test
	public void testChannelPreviousActivityInOtherHour() throws Exception {
		long postTimestampInHours = 100;
		long oldPostTimestamp = ONE_HOUR * postTimestampInHours;
		
		SolrDocumentList sdl = recordChannelSolrQuery();
		sdl.add(new SolrDocument());
		
		PostData newPostData = new PostData();
		newPostData.setParentSimpleId("whatever@whatever.com");
		newPostData.setPublished(new Date(oldPostTimestamp + ONE_HOUR + 1));
		ActivityHelper.updateActivity(newPostData, getDataSource(), null, solrFactory);
		
		PostData oldPostData = new PostData();
		oldPostData.setParentSimpleId("whatever@whatever.com");
		oldPostData.setPublished(new Date(oldPostTimestamp));
		ActivityHelper.updateActivity(oldPostData, getDataSource(), null, solrFactory);
		
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		
		Assert.assertTrue(resultSet.next());
		Assert.assertEquals("whatever@whatever.com", 
				resultSet.getString("channel_jid"));
		
		JsonArray allActivity = new JsonArray();
		
		JsonObject activityInThisHour = new JsonObject();
		activityInThisHour.addProperty("p", postTimestampInHours + 1);
		activityInThisHour.addProperty("a", 1);
		allActivity.add(activityInThisHour);
		
		JsonObject activityInLastHour = new JsonObject();
		activityInLastHour.addProperty("p", postTimestampInHours);
		activityInLastHour.addProperty("a", 1);
		allActivity.add(activityInLastHour);
		
		for (int i = 2; i < ActivityHelper.MAX_WINDOW_SIZE; i++) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours - i + 1);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		Assert.assertEquals(allActivity.toString(), 
				resultSet.getString("detailed_activity"));
		Assert.assertEquals(2, 
				resultSet.getLong("summarized_activity"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp + ONE_HOUR + 1), 
				resultSet.getTimestamp("updated"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp), 
				resultSet.getTimestamp("earliest"));
		
		ChannelDirectoryDataSource.close(st);
	}
	
	@Test
	public void testChannelPreviousActivitySkippingHours() throws Exception {
		long postTimestampInHours = 100;
		long oldPostTimestamp = ONE_HOUR * postTimestampInHours;
		
		SolrDocumentList sdl = recordChannelSolrQuery();
		sdl.add(new SolrDocument());
		
		final int skippingHours = 5;
		
		PostData newPostData = new PostData();
		newPostData.setParentSimpleId("whatever@whatever.com");
		newPostData.setPublished(new Date(oldPostTimestamp + skippingHours * ONE_HOUR + 1));
		ActivityHelper.updateActivity(newPostData, getDataSource(), null, solrFactory);
		
		PostData oldPostData = new PostData();
		oldPostData.setParentSimpleId("whatever@whatever.com");
		oldPostData.setPublished(new Date(oldPostTimestamp));
		ActivityHelper.updateActivity(oldPostData, getDataSource(), null, solrFactory);
		
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		
		Assert.assertTrue(resultSet.next());
		Assert.assertEquals("whatever@whatever.com", 
				resultSet.getString("channel_jid"));
		
		JsonArray allActivity = new JsonArray();
		JsonObject activityInThisHour = new JsonObject();
		activityInThisHour.addProperty("p", postTimestampInHours + skippingHours);
		activityInThisHour.addProperty("a", 1);
		allActivity.add(activityInThisHour);
		
		for (int i = skippingHours - 1; i >= 1; i--) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours + i);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		JsonObject activityInLastHour = new JsonObject();
		activityInLastHour.addProperty("p", postTimestampInHours);
		activityInLastHour.addProperty("a", 1);
		allActivity.add(activityInLastHour);
		
		for (int i = 1; i < ActivityHelper.MAX_WINDOW_SIZE - skippingHours; i++) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours - i);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		Assert.assertEquals(allActivity.toString(), 
				resultSet.getString("detailed_activity"));
		
		Assert.assertEquals(2, 
				resultSet.getLong("summarized_activity"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp + skippingHours * ONE_HOUR + 1), 
				resultSet.getTimestamp("updated"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp), 
				resultSet.getTimestamp("earliest"));
		
		ChannelDirectoryDataSource.close(st);
	}
	
	@Test
	public void testChannelOutOfWindowBounds() throws Exception {
		long postTimestampInHours = 100;
		long oldPostTimestamp = ONE_HOUR * postTimestampInHours;
		
		SolrDocumentList sdl = recordChannelSolrQuery();
		sdl.add(new SolrDocument());
		
		PostData newPostData = new PostData();
		newPostData.setParentSimpleId("whatever@whatever.com");
		newPostData.setPublished(new Date(oldPostTimestamp + 
				ActivityHelper.MAX_WINDOW_SIZE * ONE_HOUR));
		ActivityHelper.updateActivity(newPostData, getDataSource(), null, solrFactory);
		
		PostData oldPostData = new PostData();
		oldPostData.setParentSimpleId("whatever@whatever.com");
		oldPostData.setPublished(new Date(oldPostTimestamp));
		ActivityHelper.updateActivity(oldPostData, getDataSource(), null, solrFactory);
		
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		
		Assert.assertTrue(resultSet.next());
		Assert.assertEquals("whatever@whatever.com", 
				resultSet.getString("channel_jid"));
		
		JsonArray allActivity = new JsonArray();
		
		JsonObject activityInLastHour = new JsonObject();
		activityInLastHour.addProperty("p", postTimestampInHours + ActivityHelper.MAX_WINDOW_SIZE);
		activityInLastHour.addProperty("a", 1);
		allActivity.add(activityInLastHour);
		
		for (int i = 1; i < ActivityHelper.MAX_WINDOW_SIZE; i++) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours + ActivityHelper.MAX_WINDOW_SIZE - i);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		Assert.assertEquals(allActivity.toString(), 
				resultSet.getString("detailed_activity"));
		Assert.assertEquals(1, 
				resultSet.getLong("summarized_activity"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp + 
				ActivityHelper.MAX_WINDOW_SIZE * ONE_HOUR), 
				resultSet.getTimestamp("updated"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp + 
				ActivityHelper.MAX_WINDOW_SIZE * ONE_HOUR), 
				resultSet.getTimestamp("earliest"));
		
		ChannelDirectoryDataSource.close(st);
	}
	
	@Test
	public void testChannelActivityWindowTruncation() throws Exception {
		long postTimestampInHours = 100;
		long oldPostTimestamp = ONE_HOUR * postTimestampInHours;
		
		PostData oldPostData = new PostData();
		oldPostData.setParentSimpleId("whatever@whatever.com");
		oldPostData.setPublished(new Date(oldPostTimestamp));
		
		SolrDocumentList sdl = recordChannelSolrQuery();
		sdl.add(new SolrDocument());
		
		ActivityHelper.updateActivity(oldPostData, getDataSource(), null, solrFactory);
		
		final int skippingHours = 50;
		final int windowMaxSize = 30;
		
		PostData newPostData = new PostData();
		newPostData.setParentSimpleId("whatever@whatever.com");
		newPostData.setPublished(new Date(oldPostTimestamp + skippingHours * ONE_HOUR + 1));
		
		ActivityHelper.updateActivity(newPostData, getDataSource(), null, solrFactory);
		
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		
		Assert.assertTrue(resultSet.next());
		Assert.assertEquals("whatever@whatever.com", 
				resultSet.getString("channel_jid"));
		
		JsonArray allActivity = new JsonArray();
		JsonObject activityInThisHour = new JsonObject();
		activityInThisHour.addProperty("p", postTimestampInHours + skippingHours);
		activityInThisHour.addProperty("a", 1);
		allActivity.add(activityInThisHour);
		
		for (int i = skippingHours - 1; i > (skippingHours - windowMaxSize); i--) {
			JsonObject activity = new JsonObject();
			activity.addProperty("p", postTimestampInHours + i);
			activity.addProperty("a", 0);
			allActivity.add(activity);
		}
		
		Assert.assertEquals(allActivity.toString(), 
				resultSet.getString("detailed_activity"));
		
		Assert.assertEquals(1, 
				resultSet.getLong("summarized_activity"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp + skippingHours * ONE_HOUR + 1), 
				resultSet.getTimestamp("updated"));
		Assert.assertEquals(new Timestamp(oldPostTimestamp), 
				resultSet.getTimestamp("earliest"));
		
		ChannelDirectoryDataSource.close(st);
	}
	
	private SolrDocumentList recordChannelSolrQuery() throws SolrServerException {
		QueryResponse res = Mockito.mock(QueryResponse.class);
		SolrDocumentList sdl = new SolrDocumentList();
		Mockito.when(res.getResults()).thenReturn(sdl);
		Mockito.when(solrServer.query(Mockito.any(SolrParams.class))).thenReturn(res);
		return sdl;
	}
	
	private void assertDBEmpty() throws SQLException {
		Statement st = getDataSource().createStatement();
		ResultSet resultSet = st.executeQuery("SELECT * FROM channel_activity");
		Assert.assertFalse(resultSet.next());
		ChannelDirectoryDataSource.close(st);
	}
	
}
