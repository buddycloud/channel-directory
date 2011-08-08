package com.buddycloud.channeldirectory.search.utils;

import org.dom4j.Element;

import com.buddycloud.channeldirectory.search.handler.response.Geolocation;

public class GeolocationUtils {

	public static void appendGeoLocation(Element geoElement, Geolocation geoLocation) {
		
		if (geoElement == null || geoLocation == null) {
			return;
		}
		
		if (geoLocation.getLat() != null) {
			geoElement.addElement("lat").setText(
					geoLocation.getLat().toString());
		}
		
		if (geoLocation.getLng() != null) {
			geoElement.addElement("lon").setText(
					geoLocation.getLng().toString());
		}
		
		if (geoLocation.getText() != null) {
			geoElement.addElement("text").setText(
					geoLocation.getText());
		}
	}

}
