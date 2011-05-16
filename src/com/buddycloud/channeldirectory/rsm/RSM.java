package com.buddycloud.channeldirectory.rsm;


/**
 * Represents a container for attributes related 
 * to the RSM format (http://xmpp.org/extensions/xep-0059.html).
 * 
 */
public class RSM {

	public static String NAMESPACE = "http://jabber.org/protocol/rsm";

	private Integer max;
	private Integer index = 0;
	private String before;
	private String after;
	
	private String first;
	private String last;
	private Integer count;
	
	public String getFirst() {
		return first;
	}

	public void setFirst(String first) {
		this.first = first;
	}

	public String getLast() {
		return last;
	}

	public void setLast(String last) {
		this.last = last;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	public Integer getMax() {
		return max;
	}

	public Integer getIndex() {
		return index;
	}

	public String getBefore() {
		return before;
	}

	public String getAfter() {
		return after;
	}

}
