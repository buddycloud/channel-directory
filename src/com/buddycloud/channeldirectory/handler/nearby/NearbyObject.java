package com.buddycloud.channeldirectory.handler.nearby;

import com.buddycloud.channeldirectory.handler.QueryResponseObject;

/**
 * Represents a XMPP content (identified by its jid) 
 * with a Lat/Lng pair. 
 * 
 * @see NearbyQueryHandler
 * 
 */
public class NearbyObject extends QueryResponseObject {

	/**
	 * Type constant for channels.
	 */
	public static final String TYPE_CHANNEL = "channel";
	
	private String type;
	private String title;
	private Geolocation geolocation;
	
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setGeolocation(Geolocation geolocation) {
		this.geolocation = geolocation;
	}

	public Geolocation getGeolocation() {
		return geolocation;
	}
	
}
