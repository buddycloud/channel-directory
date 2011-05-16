package com.buddycloud.channeldirectory.handler.response;

/**
 * Represents a Latitude/Longitude location pair.
 * 
 */
public class Geolocation {

	public static final String NAMESPACE = "http://jabber.org/protocol/geoloc";
	
	private double lat;
	private double lng;
	
	public Geolocation(double lat, double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}
	
	public double getLat() {
		return lat;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public double getLng() {
		return lng;
	}
	
}
