package com.buddycloud.channeldirectory.handler.response;

import com.buddycloud.channeldirectory.rsm.RSM;

/**
 * Content query response in RSMable format.
 * Must have an identifier for indexing and paging purposes. 
 * 
 * @see RSM
 */
public class ContentData {

	private String id;
	private String type;
	
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
