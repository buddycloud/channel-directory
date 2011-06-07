package com.buddycloud.channeldirectory.handler.response;

import java.util.Date;

import com.buddycloud.channeldirectory.handler.PostQueryHandler;

/**
 * Represents a response data for searches
 * to posts.
 * 
 * @see PostQueryHandler
 * 
 */
public class PostData extends ContentData {

	private Geolocation geolocation;
	
	private String author;
	private String affiliation;
	private String content;
	
	private String serverId;
	private String leafNodeName;
	private String leafNodeId;
	private String messageId;
	
	private String inReplyTo;
	private Date updated;
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getAffiliation() {
		return affiliation;
	}

	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLeafNodeName() {
		return leafNodeName;
	}

	public void setLeafNodeName(String leafNodeName) {
		this.leafNodeName = leafNodeName;
	}

	public String getLeafNodeId() {
		return leafNodeId;
	}

	public void setLeafNodeId(String leafNodeId) {
		this.leafNodeId = leafNodeId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getInReplyTo() {
		return inReplyTo;
	}

	public void setInReplyTo(String inReplyTo) {
		this.inReplyTo = inReplyTo;
	}

	public Date getUpdated() {
		return updated;
	}

	public void setUpdated(Date updated) {
		this.updated = updated;
	}

	public void setGeolocation(Geolocation geolocation) {
		this.geolocation = geolocation;
	}

	public Geolocation getGeolocation() {
		return geolocation;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getServerId() {
		return serverId;
	}
	
}
