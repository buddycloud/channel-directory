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
