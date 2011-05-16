package com.buddycloud.channeldirectory.handler;

/**
 * A generic response item for a given query
 * to this service. Contains a jabber identifier. 
 * 
 */
public class QueryResponseObject {

	private String jid;

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}
	
}
