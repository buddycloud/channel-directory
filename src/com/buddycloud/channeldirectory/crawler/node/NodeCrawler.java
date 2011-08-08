package com.buddycloud.channeldirectory.crawler.node;

import org.jivesoftware.smackx.pubsub.Node;

/**
 * Implementations of this interface are responsible for
 * crawling a {@link Node} in a specific interest context.
 * 
 */
public interface NodeCrawler {

	/**
	 * Retrieves information from a {@link Node} and 
	 * commits it into the channel directory database.
	 * 
	 * @param node
	 * @throws Exception
	 */
	void crawl(Node node) throws Exception;
	
}
