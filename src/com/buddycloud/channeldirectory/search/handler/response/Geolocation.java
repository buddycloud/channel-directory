package com.buddycloud.channeldirectory.search.handler.response;

/**
 * Represents a Latitude/Longitude location pair, 
 * and a text representation of a location
 * 
 */
public class Geolocation {

	public static final String NAMESPACE = "http://jabber.org/protocol/geoloc";
	
	private Double lat;
	private Double lng;
	private String text;
	
	public Geolocation() {}
	
	public Geolocation(Double lat, Double lng) {
		this.lat = lat;
		this.lng = lng;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}
	
	public Double getLat() {
		return lat;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}

	public Double getLng() {
		return lng;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}
	
}
