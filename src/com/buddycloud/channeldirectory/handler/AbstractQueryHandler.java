package com.buddycloud.channeldirectory.handler;

import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.response.Content;
import com.buddycloud.channeldirectory.handler.response.Geolocation;
import com.buddycloud.channeldirectory.rsm.RSM;
import com.buddycloud.channeldirectory.rsm.RSMUtils;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Generic QueryHandler. 
 * QueryHandler implementations should extend this class.
 * 
 * @see QueryHandler
 *  
 */
public abstract class AbstractQueryHandler implements QueryHandler {

	private final String namespace;
	private final Logger logger;

	/**
	 * Creates a QueryHandler for a given namespace 
	 * @param namespace
	 */
	public AbstractQueryHandler(String namespace) {
		this.namespace = namespace;
		this.logger = Logger.getLogger(getClass());
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
	protected IQ createIQResponse(IQ iq, List<Content> allContent) {
		IQ result = IQ.createResultIQ(iq);
		
		RSM rsm = RSMUtils.parseRSM(iq.getElement().element("query"));
		
		List<Content> filteredNearbyObjects = null;
		
		try {
			filteredNearbyObjects = RSMUtils.filterRSMResponse(
					allContent, rsm);
		} catch (IllegalArgumentException e) {
			return XMPPUtils.errorRSM(iq, logger);
		}
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (Content nearbyObject : filteredNearbyObjects) {
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
}
