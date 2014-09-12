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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.dom4j.Element;
import org.xmpp.packet.IQ;

import com.buddycloud.channeldirectory.search.handler.AbstractQueryHandler;
import com.buddycloud.channeldirectory.search.handler.QueryHandler;
import com.buddycloud.channeldirectory.search.handler.response.Geolocation;
import com.buddycloud.channeldirectory.search.handler.response.PostData;
import com.buddycloud.channeldirectory.search.rsm.RSM;
import com.buddycloud.channeldirectory.search.rsm.RSMUtils;
import com.buddycloud.channeldirectory.search.utils.FeatureUtils;
import com.buddycloud.channeldirectory.search.utils.GeolocationUtils;

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

	protected IQ createIQResponse(IQ iq, List<PostData> allContent, RSM rsm) {
		IQ result = IQ.createResultIQ(iq);
		
		Element queryEl = iq.getElement().element("query");
		
		Set<String> options = FeatureUtils.parseOptions(queryEl);
		
		Element queryElement = result.getElement().addElement("query", getNamespace());
		
		for (PostData postObject : allContent) {
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
			
			if (postObject.getPublished() != null) {
				FeatureUtils.addElement(options, entryElement, "published", 
						DATE_FORMAT.format(postObject.getPublished()));
			}
			
			FeatureUtils.addElement(options, entryElement, "parent_fullid", 
					postObject.getParentFullId());
			FeatureUtils.addElement(options, entryElement, "parent_simpleid", 
					postObject.getParentSimpleId());
			
			Element geoElement = FeatureUtils.addNamespaceElement(
					options, entryElement, "geoloc", Geolocation.NAMESPACE);
			GeolocationUtils.appendGeoLocation(geoElement, 
					postObject.getGeolocation());
			
			Element inReplyEl = FeatureUtils.addNamespaceElement(
					options, entryElement, "in-reply-to", THREAD_NAMESPACE);
			if (inReplyEl != null) {
				inReplyEl.addAttribute("ref", postObject.getInReplyTo());
			}
		}
		
		RSMUtils.appendRSMElement(queryElement, rsm);
		
		return result;
	}
}
