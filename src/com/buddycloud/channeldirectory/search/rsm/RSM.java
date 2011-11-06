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
package com.buddycloud.channeldirectory.search.rsm;

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
