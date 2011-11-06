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
package org.jivesoftware.smackx.packet;

/**
 * @author Abmar
 *
 */
public class RSMSet {

	public static String NAMESPACE = "http://jabber.org/protocol/rsm";
	
	private Integer index = 0;
	private String first;
	private String last;
	private Integer count;
	
	private String before;
	private String after;
	
	/**
	 * @return the index
	 */
	public Integer getIndex() {
		return index;
	}
	/**
	 * @param index the index to set
	 */
	public void setIndex(Integer index) {
		this.index = index;
	}
	/**
	 * @return the first
	 */
	public String getFirst() {
		return first;
	}
	/**
	 * @param first the first to set
	 */
	public void setFirst(String first) {
		this.first = first;
	}
	/**
	 * @return the last
	 */
	public String getLast() {
		return last;
	}
	/**
	 * @param last the last to set
	 */
	public void setLast(String last) {
		this.last = last;
	}
	/**
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}
	/**
	 * @param count the count to set
	 */
	public void setCount(Integer count) {
		this.count = count;
	}
	
	/**
	 * 
	 */
	public String toXML() {
		StringBuilder builder = new StringBuilder("<set xmlns='http://jabber.org/protocol/rsm'>");
		
		if (first != null) {
			builder.append("<first index='").append(index).append("'>").append(first).append("</first>");
		}
		
		if (last != null) {
			builder.append("<last>").append(last).append("</last>");
		}
		
		if (count != null) {
			builder.append("<count>").append(count).append("</count>");
		}
		
		if (before != null) {
			builder.append("<before>").append(before).append("</before>");
		}
		
		if (after != null) {
			builder.append("<after>").append(after).append("</after>");
		}
		
		builder.append("</set>");
		
		return builder.toString();
	}
	public String getBefore() {
		return before;
	}
	public void setBefore(String before) {
		this.before = before;
	}
	public String getAfter() {
		return after;
	}
	public void setAfter(String after) {
		this.after = after;
	}
	
}
