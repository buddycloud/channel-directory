package com.buddycloud.channeldirectory.handler.nearby;

import java.util.List;
import java.util.Properties;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.ChannelQueryHandler;
import com.buddycloud.channeldirectory.handler.response.ChannelData;
import com.buddycloud.channeldirectory.handler.response.FakeDataGenerator;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for nearby content.
 * A query should contain user's lat/lon pair, so
 * this handle can return channels close to user location.
 *  
 */
public class NearbyQueryHandler extends ChannelQueryHandler {

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
		
		List<ChannelData> nearbyObjects = findNearbyObjects(lat, lng);
		
		return createIQResponse(iq, nearbyObjects);
	}

	private List<ChannelData> findNearbyObjects(double lat, double lng) {
		return FakeDataGenerator.createFakeChannels();
	}

}
