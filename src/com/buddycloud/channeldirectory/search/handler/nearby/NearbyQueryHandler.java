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
package com.buddycloud.channeldirectory.search.handler.nearby;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.commons.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.search.handler.common.ChannelQueryHandler;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.rsm.SolrRSMUtils;
import com.buddycloud.channeldirectory.search.utils.XMPPUtils;

/**
 * Handles queries for nearby content.
 * A query should contain user's lat/lon pair, so
 * this handle can return channels close to user location.
 * Returns the closest channels (in ascending distance order)
 * in a 1000km radius.
 */
public class NearbyQueryHandler extends ChannelQueryHandler {

	private static final String RADIUS_IN_KM = "1000";
	
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
		
		RSM rsm = RSMUtils.parseRSM(queryElement);
		List<ChannelData> nearbyObjects;
		
		try {
			nearbyObjects = findNearbyObjects(lat, lng, rsm);
		} catch (Exception e) {
			return XMPPUtils.error(iq, "Search could not be performed, service is unavailable.", 
					getLogger());
		}
		
		return createIQResponse(iq, nearbyObjects, rsm);
	}

	private List<ChannelData> findNearbyObjects(double lat, double lng, RSM rsm) throws MalformedURLException, SolrServerException {
		SolrServer solrServer = SolrServerFactory.createChannelCore(getProperties());
		SolrQuery solrQuery = new SolrQuery("*:*");
		solrQuery.set("fq", "{!geofilt}");
		solrQuery.set("sfield", "geoloc");
		solrQuery.set("pt", lat + "," + lng);
		solrQuery.set("d", RADIUS_IN_KM);
		
		solrQuery.addSortField("geodist()", ORDER.asc);
		
		SolrRSMUtils.preprocess(solrQuery, rsm);
		QueryResponse queryResponse = solrServer.query(solrQuery);
		SolrRSMUtils.postprocess(queryResponse, rsm);
		
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

}
