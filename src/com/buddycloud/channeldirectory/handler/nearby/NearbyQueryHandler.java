package com.buddycloud.channeldirectory.handler.nearby;

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.QueryHandler;
import com.buddycloud.channeldirectory.rsm.RSM;
import com.buddycloud.channeldirectory.rsm.RSMUtils;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for nearby content.
 * A query should contain user's lat/lon pair, so
 * this handle can return channels close to user location.
 *  
 */
public class NearbyQueryHandler implements QueryHandler {

	private static final Logger LOGGER = Logger.getLogger(NearbyQueryHandler.class);
	
	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element pointElement = queryElement.element("point");
		
		if (pointElement == null) {
			return XMPPUtils.error(iq, "Query does not contain point element.", 
					LOGGER);
		}
		
		Attribute latAtt = pointElement.attribute("lat");
		if (latAtt == null) {
			return XMPPUtils.error(iq, 
					"Location point does not contain point latitude element.", 
					LOGGER);
		}
		
		Attribute lngAtt = pointElement.attribute("lon");
		if (lngAtt == null) {
			return XMPPUtils.error(iq, 
					"Location point does not contain point longitude element.", 
					LOGGER);
		}
		
		double lat = Double.valueOf(latAtt.getValue());
		double lng = Double.valueOf(lngAtt.getValue());
		
		List<NearbyObject> nearbyObjects = findNearbyObjects(lat, lng);
		
		return createIQResponse(iq, nearbyObjects);
	}

	private IQ createIQResponse(IQ iq, List<NearbyObject> nearbyObjects) {
		IQ result = IQ.createResultIQ(iq);
		
		RSM rsm = RSMUtils.parseRSM(iq.getElement().element("query"));
		
		List<NearbyObject> filteredNearbyObjects = null;
		
		try {
			filteredNearbyObjects = RSMUtils.filterRSMResponse(
					nearbyObjects, rsm);
		} catch (IllegalArgumentException e) {
			return XMPPUtils.errorRSM(iq, LOGGER);
		}
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (NearbyObject nearbyObject : filteredNearbyObjects) {
			Element itemElement = queryElement.addElement("item");
			itemElement.addAttribute("jid", nearbyObject.getJid());
			itemElement.addAttribute("type", nearbyObject.getType());
			
			Element titleElement = itemElement.addElement("title");
			titleElement.setText(nearbyObject.getTitle());
			
			Element geoElement = itemElement.addElement("geoloc", Geolocation.NAMESPACE);
			geoElement.addElement("lat").setText(Double.valueOf(
					nearbyObject.getGeolocation().getLat()).toString());
			geoElement.addElement("lon").setText(Double.valueOf(
					nearbyObject.getGeolocation().getLng()).toString());
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}

	private List<NearbyObject> findNearbyObjects(double lat, double lng) {
		return NearbyFakeData.createData();
	}

	@Override
	public String getNamespace() {
		return "urn:oslo:nearbyobjects";
	}

}
