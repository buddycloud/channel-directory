package com.buddycloud.channeldirectory.handler.content;

import java.util.List;
import java.util.Properties;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.handler.PostQueryHandler;
import com.buddycloud.channeldirectory.handler.response.FakeDataGenerator;
import com.buddycloud.channeldirectory.handler.response.PostData;
import com.buddycloud.channeldirectory.utils.XMPPUtils;

/**
 * Handles queries for content posts.
 * A query should contain a content query string, so
 * this handle can return channel posts related to this search.
 *  
 */
public class ContentQueryHandler extends PostQueryHandler {

	public ContentQueryHandler(Properties properties) {
		super("urn:oslo:contentsearch", properties);
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
		
		List<PostData> relatedPosts = findObjectsByContent(search);
		
		return createIQResponse(iq, relatedPosts);
	}

	private List<PostData> findObjectsByContent(String search) {
		return FakeDataGenerator.createFakePosts();
	}

}
