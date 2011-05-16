package com.buddycloud.channeldirectory.handler.response;


/**
 * Represents a XMPP content (identified by its jid) 
 * with a type, title and Lat/Lng pair. 
 * 
 * @see Geolocation
 * 
 */
public class Content {

	/**
	 * Type constant for channels.
	 */
	public static final String TYPE_CHANNEL = "channel";
	
	private String jid;
	private String type;
	private String title;
	
	private Geolocation geolocation;
	
	public void setGeolocation(Geolocation geolocation) {
		this.geolocation = geolocation;
	}

	public Geolocation getGeolocation() {
		return geolocation;
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
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}
	
}
