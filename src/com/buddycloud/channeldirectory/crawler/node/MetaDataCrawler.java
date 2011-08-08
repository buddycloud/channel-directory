package com.buddycloud.channeldirectory.crawler.node;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.packet.DataForm;
import org.jivesoftware.smackx.packet.DiscoverInfo;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;

import com.buddycloud.channeldirectory.handler.common.solr.SolrServerFactory;
import com.buddycloud.channeldirectory.handler.response.ChannelData;
import com.buddycloud.channeldirectory.handler.response.Geolocation;

/**
 * Responsible for crawling {@link Node} metadata, 
 * for instance: its title, its description and 
 * its geolocation.
 *  
 */
public class MetaDataCrawler implements NodeCrawler {

	private static final DecimalFormat LATLNG_FORMAT = new DecimalFormat("#0.00", 
			new DecimalFormatSymbols(Locale.US));
	
	private Properties configuration;
	
	public MetaDataCrawler(Properties configuration) {
		this.configuration = configuration;
	}
	
	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#crawl(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public void crawl(Node node) throws XMPPException {
		LeafNode leaf = (LeafNode) node;
		DiscoverInfo discoverInfo = leaf.discoverInfo();
		DataForm form = (DataForm) discoverInfo.getExtension("jabber:x:data");
		
		ChannelData channelData = new ChannelData();
		
		Geolocation geolocation = new Geolocation();
		channelData.setGeolocation(geolocation);
		channelData.setId(node.getId());

		Iterator<FormField> fields = form.getFields();
		
		while (fields.hasNext()) {
			FormField formField = fields.next();
			if (formField.getVariable().equals("pubsub#title")) {
				channelData.setTitle(formField.getValues().next());
			} else if (formField.getVariable().equals("pubsub#description")) {
				channelData.setDescription(formField.getValues().next());
			} else if (formField.getVariable().equals("x-buddycloud#geoloc-lat")) {
				geolocation.setLat(Double.valueOf(formField.getValues().next()));
			} else if (formField.getVariable().equals("x-buddycloud#geoloc-lon")) {
				geolocation.setLng(Double.valueOf(formField.getValues().next()));
			} else if (formField.getVariable().equals("x-buddycloud#geoloc-text")) {
				geolocation.setText(formField.getValues().next());
			}
		}
		
		if (geolocation.getLat() == null && geolocation.getLng() == null
				&& geolocation.getText() == null) {
			channelData.setGeolocation(null);
		}
		
		channelData.setType(getChannelType(node.getId()));
		
		try {
			insertOrUpate(channelData);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	private void insertOrUpate(ChannelData channelData) throws Exception {

		SolrInputDocument object = new SolrInputDocument();
		
		if (channelData.getGeolocation() != null) {
			Double lat = channelData.getGeolocation().getLat();
			Double lng = channelData.getGeolocation().getLng();
			
			if (lat != null && lng != null) {
				object.setField("geoloc", LATLNG_FORMAT.format(lat) + "," + LATLNG_FORMAT.format(lng));
			}
			String geoText = channelData.getGeolocation().getText();
			if (geoText != null) {
				object.setField("geoloc-text", geoText);
			}
		}
		
		object.setField("jid", channelData.getId());
		object.setField("title", channelData.getTitle());
		object.setField("description", channelData.getDescription());
		object.setField("channel-type", channelData.getType()); //topic or personal
		
		SolrServer solrServer = SolrServerFactory.createChannelCore(configuration);
		solrServer.add(object);
		solrServer.commit();
	}

	private static String getChannelType(String jid) {
		String type = jid.substring(0, jid.indexOf('/', 1));
		return type.equals("/channel") ? "topic" : "personal";
	}
	
}
