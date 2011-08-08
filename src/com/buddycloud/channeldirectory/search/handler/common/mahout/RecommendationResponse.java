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
package com.buddycloud.channeldirectory.search.handler.common.mahout;

import java.util.List;

import com.buddycloud.channeldirectory.search.handler.response.ChannelData;

/**
 *
 */
public class RecommendationResponse {

	private List<ChannelData> response; 
	private int numFound;
	
	/**
	 * @param response
	 * @param numFound
	 */
	public RecommendationResponse(List<ChannelData> response, int numFound) {
		this.response = response;
		this.numFound = numFound;
	}
	
	/**
	 * @return the response
	 */
	public List<ChannelData> getResponse() {
		return response;
	}
	
	/**
	 * @param response the response to set
	 */
	public void setResponse(List<ChannelData> response) {
		this.response = response;
	}
	
	/**
	 * @return the numFound
	 */
	public int getNumFound() {
		return numFound;
	}
	
	/**
	 * @param numFound the numFound to set
	 */
	public void setNumFound(int numFound) {
		this.numFound = numFound;
	}
	
}
