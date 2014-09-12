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
package com.buddycloud.channeldirectory.search.rsm;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.response.QueryResponse;


/**
 * It is responsible for providing utility methods related to RSM format 
 * (http://xmpp.org/extensions/xep-0059.html),
 * which are used on the Solr query processing and response.
 * 
 * @see RSM
 *  
 */
public class SolrRSMUtils {

	public static void preprocess(
			SolrQuery query, RSM rsm)
			throws IllegalArgumentException {
		
		String after = rsm.getAfter();
		String before = rsm.getBefore();
		
		int initialIndex = rsm.getIndex();
		int lastIndex = -1;
		
		if (after != null) {
			initialIndex = Integer.valueOf(after);
		}
		if (before != null && !before.isEmpty()) {
			lastIndex = Integer.valueOf(before) - 2;
		}
		
		if (rsm.getMax() != null) {
			if (before != null) {
				initialIndex = lastIndex - rsm.getMax() + 1;
			} else {
				lastIndex = initialIndex + rsm.getMax() - 1;
			}
		}
		
		if (lastIndex >= 0) {
			query.setStart(initialIndex);
			query.setRows(lastIndex - initialIndex + 1);
		}
		
		rsm.setIndex(initialIndex);
		rsm.setFirst(String.valueOf(initialIndex + 1));
		rsm.setLast(String.valueOf(lastIndex + 1));
	}
	
	public static void postprocess(
			QueryResponse queryResponse, RSM rsm)
			throws IllegalArgumentException {
		
		rsm.setCount((int)queryResponse.getResults().getNumFound());
		
		if (queryResponse.getResponse().size() == 0) {
			rsm.setFirst(null);
			rsm.setLast(null);
		}
	}
}
