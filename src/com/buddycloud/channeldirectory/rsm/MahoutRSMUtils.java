package com.buddycloud.channeldirectory.rsm;

import java.util.List;

import com.buddycloud.channeldirectory.handler.common.mahout.RecommendationResponse;
import com.buddycloud.channeldirectory.handler.response.ChannelData;

/**
 * It is responsible for providing utility methods related to RSM format 
 * (http://xmpp.org/extensions/xep-0059.html),
 * which are used on the Solr query processing and response.
 * 
 * @see RSM
 *  
 */
public class MahoutRSMUtils {

	public static int preprocess(RSM rsm)
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
		
		rsm.setIndex(initialIndex);
		rsm.setFirst(String.valueOf(initialIndex + 1));
		rsm.setLast(String.valueOf(lastIndex + 1));
		
		return lastIndex + 1;
	}
	
	public static List<ChannelData> postprocess(
			RecommendationResponse response, RSM rsm)
			throws IllegalArgumentException {
		
		rsm.setCount(response.getNumFound());
		
		List<ChannelData> responseItems = response.getResponse().subList(
				Integer.valueOf(rsm.getFirst()) - 1, 
				Integer.valueOf(rsm.getLast()));
		
		if (responseItems.isEmpty()) {
			rsm.setFirst(null);
			rsm.setLast(null);
		}
		
		return responseItems;
	}
}
