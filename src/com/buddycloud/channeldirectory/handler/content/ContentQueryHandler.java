package com.buddycloud.channeldirectory.handler.content;

import java.util.List;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.AbstractQueryHandler;
import com.buddycloud.channeldirectory.handler.response.Content;
import com.buddycloud.channeldirectory.handler.response.FakeData;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for content posts.
 * A query should contain a content query string, so
 * this handle can return channels related to this search.
 *  
 */
public class ContentQueryHandler extends AbstractQueryHandler {

	public ContentQueryHandler() {
		super("urn:oslo:contentsearch");
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
		
		List<Content> nearbyObjects = findObjectsByContent(search);
		
		return createIQResponse(iq, nearbyObjects);
	}

	private List<Content> findObjectsByContent(String search) {
		return FakeData.createData();
	}

}
