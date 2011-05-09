package com.buddycloud.channeldirectory.handler.nearby;

/**
 * Represents a XMPP content (identified by its jid) 
 * with a Lat/Lng pair. 
 * 
 * @see NearbyQueryHandler
 * 
 */
public class NearbyObject {

	/**
	 * Type constant for channels.
	 */
	public static final String TYPE_CHANNEL = "channel";
	
	private String jid;
	private String type;
	private String title;
	private Geolocation geolocation;
	
	public void setJid(String jid) {
		this.jid = jid;
	}
	
	public String getJid() {
		return jid;
	}

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
