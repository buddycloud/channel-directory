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
package com.buddycloud.channeldirectory.search.handler.common;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.search.handler.AbstractQueryHandler;
import com.buddycloud.channeldirectory.search.handler.QueryHandler;
import com.buddycloud.channeldirectory.search.handler.response.ChannelData;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.utils.FeatureUtils;
import com.buddycloud.channeldirectory.search.utils.GeolocationUtils;

/**
 * Abstract class for {@link QueryHandler} that returns
 * ChannelData.
 * 
 */
public abstract class ChannelQueryHandler extends AbstractQueryHandler {

	public ChannelQueryHandler(String namespace, Properties properties) {
		super(namespace, properties);
	}

	protected IQ createIQResponse(IQ iq, List<ChannelData> allContent, RSM rsm) {
		IQ result = IQ.createResultIQ(iq);
		
		Element queryEl = iq.getElement().element("query");
		Set<String> options = FeatureUtils.parseOptions(queryEl);
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (ChannelData nearbyObject : allContent) {
			Element itemElement = queryElement.addElement("item");
			
			FeatureUtils.addAttribute(options, itemElement, "jid",
					nearbyObject.getId());
			FeatureUtils.addAttribute(options, itemElement, "type",
					nearbyObject.getType());
			FeatureUtils.addAttribute(options, itemElement, "description",
					nearbyObject.getDescription());
			FeatureUtils.addElement(options, itemElement, "title",
					nearbyObject.getTitle());
			FeatureUtils.addElement(options, itemElement, "channel-type", 
					nearbyObject.getChannelType());

			Element geoElement = FeatureUtils.addNamespaceElement(
					options, itemElement, "geoloc", Geolocation.NAMESPACE);
			
			GeolocationUtils.appendGeoLocation(geoElement, nearbyObject.getGeolocation());
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}
}
