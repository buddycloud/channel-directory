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
package com.buddycloud.channeldirectory.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.xmpp.component.AbstractComponent;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;

import com.buddycloud.channeldirectory.search.handler.QueryHandler;
import com.buddycloud.channeldirectory.search.handler.active.MostActiveQueryHandler;
import com.buddycloud.channeldirectory.search.handler.common.mahout.ChannelRecommender;
import com.buddycloud.channeldirectory.search.handler.content.ContentQueryHandler;
import com.buddycloud.channeldirectory.search.handler.metadata.MetadataQueryHandler;
import com.buddycloud.channeldirectory.search.handler.nearby.NearbyQueryHandler;
import com.buddycloud.channeldirectory.search.handler.recommendation.RecommendationQueryHandler;
import com.buddycloud.channeldirectory.search.handler.similarity.SimilarityQueryHandler;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.utils.XMPPUtils;

/**
 * Channel Directory XMPP Component
 * Follows the XEP-0114 (http://xmpp.org/extensions/xep-0114.html)
 * 
 */
public class ChannelDirectoryComponent extends AbstractComponent {

	private static final String DESCRIPTION = "A pub sub search engine, " +
			"metadata crawler and recommendation service";
	private static final String NAME = "Channel Directory";
	private static final Logger LOGGER = Logger.getLogger(ChannelDirectoryComponent.class);
	
	private final Map<String, QueryHandler> queryHandlers = new HashMap<String, QueryHandler>();
	private final Properties properties;

	public ChannelDirectoryComponent(Properties properties) {
		this.properties = properties;
	}
	
	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public void postComponentStart() {
		createHandlers();
	}

	/* (non-Javadoc)
	 * @see org.xmpp.component.AbstractComponent#send(org.xmpp.packet.Packet)
	 */
	@Override
	protected void send(Packet arg0) {
		LOGGER.debug("S: " + arg0.toXML());
		super.send(arg0);
	}
	
	@Override
	protected IQ handleIQGet(IQ iq) throws Exception {
		
		LOGGER.debug("R: " + iq.toXML());
		
		Element queryElement = iq.getElement().element("query");
		if (queryElement == null) {
			return XMPPUtils.error(iq, "IQ does not contain query element.", 
					LOGGER);
		}
		
		Namespace namespace = queryElement.getNamespace();
		
		QueryHandler queryHandler = queryHandlers.get(namespace.getURI());
		if (queryHandler == null) {
			return XMPPUtils.error(iq, "QueryHandler not found for namespace: " + namespace, 
					LOGGER);
		}
		
		return queryHandler.handle(iq);
	}
	
	@Override 
	protected String[] discoInfoFeatureNamespaces() {
		ArrayList<String> namespaces = new ArrayList<String>(queryHandlers.keySet());
		namespaces.add(RSM.NAMESPACE);
		return (namespaces.toArray(new String[]{}));
	}

	@Override 
	protected String discoInfoIdentityCategory() {
		return ("Search");
	}

	@Override 
	protected String discoInfoIdentityCategoryType() {
		return ("Directory");
	}

	private void createHandlers() {
		addHandler(new NearbyQueryHandler(properties));
		addHandler(new MetadataQueryHandler(properties));
		addHandler(new ContentQueryHandler(properties));
		addHandler(new MostActiveQueryHandler(properties));
		
		ChannelRecommender recommender = createRecommender(properties);
		addHandler(new RecommendationQueryHandler(properties, recommender));
		addHandler(new SimilarityQueryHandler(properties, recommender));
	}
	
	private ChannelRecommender createRecommender(Properties properties) {
		try {
			return new ChannelRecommender(properties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void addHandler(QueryHandler queryHandler) {
		queryHandlers.put(queryHandler.getNamespace(), queryHandler);
	}
}
