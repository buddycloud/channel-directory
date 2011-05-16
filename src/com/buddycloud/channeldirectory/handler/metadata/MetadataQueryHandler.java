package com.buddycloud.channeldirectory.handler.metadata;

import java.util.List;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.AbstractQueryHandler;
import com.buddycloud.channeldirectory.handler.response.Content;
import com.buddycloud.channeldirectory.handler.response.FakeData;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for content metadata.
 * A query should contain a metadata query string, so
 * this handle can return channels related to this search.
 *  
 */
public class MetadataQueryHandler extends AbstractQueryHandler {

	public MetadataQueryHandler() {
		super("urn:oslo:metadatasearch");
	}

	@Override
	public IQ handle(IQ iq) {
		
		Element queryElement = iq.getElement().element("query");
		Element searchElement = queryElement.element("search");
		
		if (searchElement == null) {
			return XMPPUtils.error(iq, "Query does not contain search element.", 
					getLogger());
		}
		
		String search = searchElement.getText();
		
		if (search == null || search.isEmpty()) {
			return XMPPUtils.error(iq, "Search content cannot be empty.", 
					getLogger());
		}
		
		List<Content> nearbyObjects = findObjectsByMetadata(search);
		
		return createIQResponse(iq, nearbyObjects);
	}

	private List<Content> findObjectsByMetadata(String search) {
		return FakeData.createData();
	}

}
