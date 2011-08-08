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
