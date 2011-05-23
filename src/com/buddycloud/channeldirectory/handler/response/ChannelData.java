package com.buddycloud.channeldirectory.handler.response;


/**
 * Represents a XMPP channel (identified by its jid) 
 * with a title and a geolocation attribute. 
 * 
 * @see Geolocation
 * 
 */
public class ChannelData extends ContentData {

	/**
	 * Type constant for channels.
	 */
	private static final String TYPE_CHANNEL = "channel";
	
	public ChannelData() {
		setType(TYPE_CHANNEL);
	}
	
	private String title;
	
	private Geolocation geolocation;
	
	public void setGeolocation(Geolocation geolocation) {
		this.geolocation = geolocation;
	}

	public Geolocation getGeolocation() {
		return geolocation;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

}
