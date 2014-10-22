/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buddycloud.channeldirectory.crawler.node;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.pubsub.BuddycloudNode;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.rsm.packet.RSMSet;

import com.buddycloud.channeldirectory.commons.db.ChannelDirectoryDataSource;

/**
 * Responsible for crawling {@link Node} data
 * regarding its posts.
 *  
 */
public class FirehoseCrawler implements NodeCrawler {

	/**
	 * 
	 */
	private static Logger LOGGER = Logger.getLogger(FirehoseCrawler.class);
	
	private final ChannelDirectoryDataSource dataSource;
	private PostCrawler postCrawler;
	
	public FirehoseCrawler(Properties configuration, ChannelDirectoryDataSource dataSource) {
		this.dataSource = dataSource;
		this.postCrawler = new PostCrawler(configuration, dataSource);
	}
	
	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#crawl(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public void crawl(BuddycloudNode node, String server) throws Exception {
		
		String newerThan = CrawlerHelper.getLastItemCrawled(server, dataSource);
		String olderItemId = null;
		
		Set<String> nodesAlreadyVisited = new HashSet<String>();
		
		while (true) {
			List<PacketExtension> additionalExtensions = new LinkedList<PacketExtension>();
			List<PacketExtension> returnedExtensions = new LinkedList<PacketExtension>();
			List<Item> items = null;
			if (olderItemId != null) {
				RSMSet nextRsmSet = RSMSet.newAfter(olderItemId);
				additionalExtensions.add(nextRsmSet);
			}
			items = node.getItems(additionalExtensions, returnedExtensions);
			if (items.isEmpty()) {
				break;
			}
			boolean done = false;
			for (Item item : items) {
				Element itemEntry = CrawlerHelper.getAtomEntry(item);
				String itemId = itemEntry.elementText("id");
				if (newerThan != null && itemId.equals(newerThan)) {
					done = true;
					break;
				}
				olderItemId = itemId;
				try {
					String nodeId = CrawlerHelper.getNodeFromItemId(itemId);
					postCrawler.processPost(nodeId, 
							CrawlerHelper.getChannelFromNode(nodeId), item);
					if (nodesAlreadyVisited.add(nodeId)) {
						CrawlerHelper.updateLastItemCrawled(node, 
								itemId, server, dataSource);
					}
				} catch (Exception e) {
					LOGGER.warn(e);
				}
			}
			if (done) {
				break;
			}
		}
	}


	/* (non-Javadoc)
	 * @see com.buddycloud.channeldirectory.crawler.node.NodeCrawler#accept(org.jivesoftware.smackx.pubsub.Node)
	 */
	@Override
	public boolean accept(BuddycloudNode node) {
		return node.getId().equals("/firehose");
	}
}
