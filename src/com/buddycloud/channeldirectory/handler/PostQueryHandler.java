package com.buddycloud.channeldirectory.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
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

	private static final String ATOM_NAMESPACE = "http://www.w3.org/2005/Atom";
	private static final String THREAD_NAMESPACE = "http://purl.org/syndication/thread/1.0";
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd'T'HH:mm:ssZ");
	
	public PostQueryHandler(String namespace, Properties properties) {
		super(namespace, properties);
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
		
		for (PostData postObject : filteredObjects) {
			Element itemElement = queryElement.addElement("item");
			
			FeatureUtils.addAttribute(options, itemElement, "id",
					postObject.getId());
			FeatureUtils.addAttribute(options, itemElement, "type",
					postObject.getType());
			
			Element entryElement = itemElement.addElement("entry", ATOM_NAMESPACE);
			
			FeatureUtils.addElement(options, entryElement, "author", 
					postObject.getAuthor());
			
			Element contentElement = FeatureUtils.addElement(
					options, entryElement, "content", 
					postObject.getContent());
			if (contentElement != null) {
				contentElement.addAttribute("type", "text");
			}
			
			if (postObject.getUpdated() != null) {
				FeatureUtils.addElement(options, entryElement, "updated", 
						DATE_FORMAT.format(postObject.getUpdated()));
			}
			
			String fullId = postObject.getLeafNodeName() + ":"
					+ postObject.getMessageId();
			FeatureUtils.addElement(options, entryElement, "id", fullId);
			
			Element geoElement = FeatureUtils.addNamespaceElement(
					options, entryElement, "geoloc", Geolocation.NAMESPACE);
			appendGeoLocation(geoElement, 
					postObject.getGeolocation());
			
			Element inReplyEl = FeatureUtils.addNamespaceElement(
					options, entryElement, "thr:in-reply-to", THREAD_NAMESPACE);
			if (inReplyEl != null) {
				inReplyEl.addAttribute("ref", postObject.getInReplyTo());
			}
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}

	private void appendGeoLocation(Element geoElement, Geolocation geoLocation) {
		
		if (geoElement == null || geoLocation == null) {
			return;
		}
		
		if (geoLocation.getLat() != null) {
			geoElement.addElement("lat").setText(
					geoLocation.getLat().toString());
		}
		
		if (geoLocation.getLng() != null) {
			geoElement.addElement("lon").setText(
					geoLocation.getLng().toString());
		}
		
		if (geoLocation.getText() != null) {
			geoElement.addElement("text").setText(
					geoLocation.getText());
		}
	}
}
