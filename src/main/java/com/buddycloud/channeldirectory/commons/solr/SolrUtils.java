package com.buddycloud.channeldirectory.commons.solr;

import java.util.Date;

import org.apache.solr.common.SolrDocument;

import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;

public class SolrUtils {

	public static ChannelData convertToChannelData(SolrDocument solrDocument) {
		ChannelData channelData = new ChannelData();
		String latLonStr = (String) solrDocument.getFieldValue("geoloc");
		if (latLonStr != null) {
			String[] latLonSplit = latLonStr.split(",");
			channelData.setGeolocation(new Geolocation(
					Double.parseDouble(latLonSplit[0]), 
					Double.parseDouble(latLonSplit[1])));
		}
		
		channelData.setCreationDate((Date) solrDocument.getFieldValue("creation-date"));
		channelData.setChannelType((String) solrDocument.getFieldValue("channel-type"));
		channelData.setId((String) solrDocument.getFieldValue("jid"));
		channelData.setTitle((String) solrDocument.getFieldValue("title"));
		channelData.setDescription((String) solrDocument.getFieldValue("description"));
		channelData.setDefaultAffiliation((String) solrDocument.getFieldValue("default-affiliation"));
		
		return channelData;
	}

}
