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
