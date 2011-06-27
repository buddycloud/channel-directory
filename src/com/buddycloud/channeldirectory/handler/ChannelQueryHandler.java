package com.buddycloud.channeldirectory.handler;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.response.ChannelData;
import com.buddycloud.channeldirectory.handler.response.Geolocation;
import com.buddycloud.channeldirectory.rsm.RSM;
import com.buddycloud.channeldirectory.utils.FeatureUtils;
import com.buddycloud.channeldirectory.utils.GeolocationUtils;
import com.buddycloud.channeldirectory.utils.RSMUtils;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Abstract class for {@link QueryHandler} that returns
 * ChannelData.
 * 
 */
public abstract class ChannelQueryHandler extends AbstractQueryHandler {

	public ChannelQueryHandler(String namespace, Properties properties) {
		super(namespace, properties);
	}

	protected IQ createIQResponse(IQ iq, List<ChannelData> allContent) {
		IQ result = IQ.createResultIQ(iq);
		
		Element queryEl = iq.getElement().element("query");
		
		RSM rsm = RSMUtils.parseRSM(queryEl);
		Set<String> options = FeatureUtils.parseOptions(queryEl);
		
		List<ChannelData> filteredNearbyObjects = null;
		
		try {
			filteredNearbyObjects = RSMUtils.filterRSMResponse(
					allContent, rsm);
		} catch (IllegalArgumentException e) {
			return XMPPUtils.errorRSM(iq, getLogger());
		}
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (ChannelData nearbyObject : filteredNearbyObjects) {
			Element itemElement = queryElement.addElement("item");
			
			FeatureUtils.addAttribute(options, itemElement, "jid",
					nearbyObject.getId());
			FeatureUtils.addAttribute(options, itemElement, "type",
					nearbyObject.getType());
			FeatureUtils.addElement(options, itemElement, "title",
					nearbyObject.getTitle());
			FeatureUtils.addElement(options, itemElement, "channel-type", 
					nearbyObject.getChannelType());

			Element geoElement = FeatureUtils.addNamespaceElement(
					options, itemElement, "geoloc", Geolocation.NAMESPACE);
			
			GeolocationUtils.appendGeoLocation(geoElement, nearbyObject.getGeolocation());
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}
}
