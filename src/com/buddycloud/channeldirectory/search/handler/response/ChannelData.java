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
	
	public static final String CH_TYPE_TOPIC = "topic";
	public static final String CH_TYPE_PERSONAL = "personal";
	
	public ChannelData() {
		setType(TYPE_CHANNEL);
	}
	
	private String title;
	private Geolocation geolocation;
	private String channelType;
	private String description;
	
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

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

}
