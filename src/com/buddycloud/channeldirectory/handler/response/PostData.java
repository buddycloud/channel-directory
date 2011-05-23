package com.buddycloud.channeldirectory.handler.response;

import com.buddycloud.channeldirectory.handler.PostQueryHandler;

/**
 * Represents a response data for searches
 * to posts.
 * 
 * @see PostQueryHandler
 * 
 */
public class PostData extends ContentData {

	private Geolocation geolocation;
	
	public void setGeolocation(Geolocation geolocation) {
		this.geolocation = geolocation;
	}

	public Geolocation getGeolocation() {
		return geolocation;
	}
	
}
