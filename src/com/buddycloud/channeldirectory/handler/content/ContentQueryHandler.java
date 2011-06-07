package com.buddycloud.channeldirectory.handler.content;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.PostQueryHandler;
import com.buddycloud.channeldirectory.handler.response.Geolocation;
import com.buddycloud.channeldirectory.handler.response.PostData;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for content posts.
 * A query should contain a content query string, so
 * this handle can return channel posts related to this search.
 *  
 */
public class ContentQueryHandler extends PostQueryHandler {

	private static final String SOLR_POSTCORE_PROP = "solr.postcore";
	
	public ContentQueryHandler(Properties properties) {
		super("http://buddycloud.com/channel_directory/content_query", properties);
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element searchElement = queryElement.element("search");
		
		if (searchElement == null) {
			return XMPPUtils.error(iq, "Query does not contain search element.", 
					getLogger());
		}
		
		String search = searchElement.getText();
		
		if (search == null || search.isEmpty()) {
			return XMPPUtils.error(iq, "Search content cannot be empty.", 
					getLogger());
		}
		
		List<PostData> relatedPosts;
		
		try {
			relatedPosts = findObjectsByContent(search);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					getLogger());
		}
		
		return createIQResponse(iq, relatedPosts);
	}

	private List<PostData> findObjectsByContent(String search)
			throws MalformedURLException, SolrServerException {
		SolrServer solrServer = getSolrServer();
		SolrQuery solrQuery = new SolrQuery(search);
		solrQuery.setSortField("updated", ORDER.desc);
		QueryResponse queryResponse = solrServer.query(solrQuery);

		return convertResponse(queryResponse);
	}
	
	private static List<PostData> convertResponse(QueryResponse queryResponse) {
		List<PostData> channels = new ArrayList<PostData>();
		SolrDocumentList results = queryResponse.getResults();
		
		for (SolrDocument solrDocument : results) {
			channels.add(convertDocument(solrDocument));
		}
		
		return channels;
	}

	private static PostData convertDocument(SolrDocument solrDocument) {
		PostData postData = new PostData();
		
		String latLonStr = (String) solrDocument.getFieldValue("geoloc");
		String locStr = (String) solrDocument.getFieldValue("geoloc_text");
		
		if (latLonStr != null || locStr != null) {
			Geolocation geoLocation = new Geolocation();
			geoLocation.setText(locStr);
			
			if (latLonStr != null) {
				String[] latLonSplit = latLonStr.split(",");
				geoLocation.setLat(Double.parseDouble(latLonSplit[0]));
				geoLocation.setLng(Double.parseDouble(latLonSplit[1]));
			}
			
			postData.setGeolocation(geoLocation);
		}
		
		postData.setId((String) solrDocument.getFieldValue("id"));
		postData.setAuthor((String) solrDocument.getFieldValue("author"));
		postData.setAffiliation((String) solrDocument.getFieldValue("affiliation"));
		postData.setContent((String) solrDocument.getFieldValue("content"));
		postData.setServerId((String) solrDocument.getFieldValue("server_id"));
		postData.setLeafNodeName((String) solrDocument.getFieldValue("leafnode_name"));
		postData.setLeafNodeId((String) solrDocument.getFieldValue("leafnode_id"));
		postData.setMessageId((String) solrDocument.getFieldValue("message_id"));
		postData.setInReplyTo((String) solrDocument.getFieldValue("inreplyto"));
		postData.setUpdated((Date) solrDocument.getFieldValue("updated"));
		
		return postData;
	}
	
	private SolrServer getSolrServer() throws MalformedURLException {
		String solrChannelUrl = (String) getProperties()
				.get(SOLR_POSTCORE_PROP);
		SolrServer server = new CommonsHttpSolrServer(solrChannelUrl);
		return server;
	}

}
