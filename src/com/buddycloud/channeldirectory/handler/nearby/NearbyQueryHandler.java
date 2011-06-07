package com.buddycloud.channeldirectory.handler.nearby;

import java.net.MalformedURLException;
import java.util.ArrayList;
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
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.ChannelQueryHandler;
import com.buddycloud.channeldirectory.handler.response.ChannelData;
import com.buddycloud.channeldirectory.handler.response.Geolocation;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for nearby content.
 * A query should contain user's lat/lon pair, so
 * this handle can return channels close to user location.
 * Returns the closest channels (in ascending distance order)
 * in a 1000km radius.
 */
public class NearbyQueryHandler extends ChannelQueryHandler {

	private static final String RADIUS_IN_KM = "1000";
	private static final String SOLR_CHANNELCORE_PROP = "solr.channelcore";
	
	public NearbyQueryHandler(Properties properties) {
		super("http://buddycloud.com/channel_directory/nearby_query", properties);
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element pointElement = queryElement.element("point");
		
		if (pointElement == null) {
			return XMPPUtils.error(iq, "Query does not contain point element.", 
					getLogger());
		}
		
		Attribute latAtt = pointElement.attribute("lat");
		if (latAtt == null) {
			return XMPPUtils.error(iq, 
					"Location point does not contain point latitude element.", 
					getLogger());
		}
		
		Attribute lngAtt = pointElement.attribute("lon");
		if (lngAtt == null) {
			return XMPPUtils.error(iq, 
					"Location point does not contain point longitude element.", 
					getLogger());
		}
		
		double lat = Double.valueOf(latAtt.getValue());
		double lng = Double.valueOf(lngAtt.getValue());
		
		List<ChannelData> nearbyObjects;
		
		try {
			nearbyObjects = findNearbyObjects(lat, lng);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					getLogger());
		}
		
		return createIQResponse(iq, nearbyObjects);
	}

	private List<ChannelData> findNearbyObjects(double lat, double lng) throws MalformedURLException, SolrServerException {
		SolrServer solrServer = getSolrServer();
		SolrQuery solrQuery = new SolrQuery("*:*");
		solrQuery.set("fq", "{!geofilt}");
		solrQuery.set("sfield", "geoloc");
		solrQuery.set("pt", lat + "," + lng);
		solrQuery.set("d", RADIUS_IN_KM);
		
		solrQuery.addSortField("geodist()", ORDER.asc);
		
		QueryResponse queryResponse = solrServer.query(solrQuery);
		
		return convertResponse(queryResponse);
	}
	
	private static List<ChannelData> convertResponse(QueryResponse queryResponse) {
		List<ChannelData> channels = new ArrayList<ChannelData>();
		SolrDocumentList results = queryResponse.getResults();
		
		for (SolrDocument solrDocument : results) {
			channels.add(convertDocument(solrDocument));
		}
		
		return channels;
	}

	private static ChannelData convertDocument(SolrDocument solrDocument) {
		ChannelData channelData = new ChannelData();
		String latLonStr = (String) solrDocument.getFieldValue("geoloc");
		if (latLonStr != null) {
			String[] latLonSplit = latLonStr.split(",");
			channelData.setGeolocation(new Geolocation(
					Double.parseDouble(latLonSplit[0]), 
					Double.parseDouble(latLonSplit[1])));
		}
		
		channelData.setId((String) solrDocument.getFieldValue("jid"));
		channelData.setTitle((String) solrDocument.getFieldValue("title"));
		return channelData;
	}

	private SolrServer getSolrServer() throws MalformedURLException {
		String solrChannelUrl = (String) getProperties()
				.get(SOLR_CHANNELCORE_PROP);
		SolrServer server = new CommonsHttpSolrServer(solrChannelUrl);
		return server;
	}

}
