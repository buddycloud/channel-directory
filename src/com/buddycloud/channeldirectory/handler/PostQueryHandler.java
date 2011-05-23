package com.buddycloud.channeldirectory.handler;

import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.response.Geolocation;
import com.buddycloud.channeldirectory.handler.response.PostData;
import com.buddycloud.channeldirectory.rsm.RSM;
import com.buddycloud.channeldirectory.utils.FeatureUtils;
import com.buddycloud.channeldirectory.utils.RSMUtils;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Abstract class for {@link QueryHandler} that returns
 * PostData.
 * 
 */
public abstract class PostQueryHandler extends AbstractQueryHandler {

	public PostQueryHandler(String namespace) {
		super(namespace);
	}

	protected IQ createIQResponse(IQ iq, List<PostData> allContent) {
		IQ result = IQ.createResultIQ(iq);
		
		Element queryEl = iq.getElement().element("query");
		
		RSM rsm = RSMUtils.parseRSM(queryEl);
		Set<String> options = FeatureUtils.parseOptions(queryEl);
		
		List<PostData> filteredObjects = null;
		
		try {
			filteredObjects = RSMUtils.filterRSMResponse(
					allContent, rsm);
		} catch (IllegalArgumentException e) {
			return XMPPUtils.errorRSM(iq, getLogger());
		}
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (PostData nearbyObject : filteredObjects) {
			Element itemElement = queryElement.addElement("item");
			
			FeatureUtils.addAttribute(options, itemElement, "id",
					nearbyObject.getId());
			FeatureUtils.addAttribute(options, itemElement, "type",
					nearbyObject.getType());

			Element geoElement = FeatureUtils.addNamespaceElement(
					options, itemElement, "geoloc", Geolocation.NAMESPACE);
			
			if (geoElement != null) {
				geoElement.addElement("lat").setText(Double.valueOf(
						nearbyObject.getGeolocation().getLat()).toString());
				geoElement.addElement("lon").setText(Double.valueOf(
						nearbyObject.getGeolocation().getLng()).toString());
			}
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}
}
