package com.buddycloud.channeldirectory.crawler.node;

import org.jivesoftware.smackx.pubsub.Node;

public interface NodeCrawler {

	void crawl(Node node) throws Exception;
	
}
