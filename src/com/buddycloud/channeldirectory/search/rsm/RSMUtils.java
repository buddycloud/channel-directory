package com.buddycloud.channeldirectory.search.rsm;

import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;

import com.buddycloud.channeldirectory.search.handler.response.ContentData;

/**
 * It is responsible for providing utility methods related to RSM format 
 * (http://xmpp.org/extensions/xep-0059.html),
 * which are used on the query processing and response.
 * 
 * @see RSM
 *  
 */
public class RSMUtils {

	/**
	 * Appends RSM info to query response.
	 * @param queryElement
	 * @param rsm
	 */
	public static void appendRSMElement(Element queryElement, RSM rsm) {
		Element setElement = queryElement.addElement("set", RSM.NAMESPACE);
		
		if (rsm.getFirst() != null) {
			Element firstElement = setElement.addElement("first");
			firstElement.addAttribute("index", rsm.getIndex().toString());
			firstElement.setText(rsm.getFirst());
		}
		
		if (rsm.getLast() != null) {
			Element lastElement = setElement.addElement("last");
			lastElement.setText(rsm.getLast());
		}
		
		setElement.addElement("count").setText(String.valueOf(rsm.getCount()));
	}
	
	/**
	 * Parses an RSM from a query XML element
	 * @param queryElement
	 * @return The parsed RSM object
	 */
	public static RSM parseRSM(Element queryElement) {
		RSM rsm = new RSM();
		
		Element setElement = queryElement.element("set");
		if (setElement == null) {
			return rsm;
		}
		
		Element after = setElement.element("after");
		if (after != null) {
			rsm.setAfter(after.getText());
		}
		
		Element before = setElement.element("before");
		if (before != null) {
			String beforeText = before.getText();
			rsm.setBefore(beforeText == null ? "" : beforeText);
		}
		
		Element index = setElement.element("index");
		if (index != null) {
			rsm.setIndex(Integer.parseInt(index.getText()));
		}
		
		Element max = setElement.element("max");
		if (max != null) {
			rsm.setMax(Integer.parseInt(max.getText()));
		}
		
		return rsm;
	}
	
	/**
	 * Filters response objects based on the RSM parameters.
	 * Updates the RSM object with item count, first and last jid
	 * 
	 * @param objects
	 * @param rsm
	 * @return 
	 * @throws IllegalArgumentException If the item specified by the requesting 
	 * 			entity via the UID in the <after/> or <before/> element does not exist
	 */
	public static <T extends ContentData> List<T> filterRSMResponse(
			List<T> objects, RSM rsm)
			throws IllegalArgumentException {
		
		String after = rsm.getAfter();
		String before = rsm.getBefore();
		
		int initialIndex = rsm.getIndex();
		int lastIndex = objects.size();
		
		if (after != null || (before != null && !before.isEmpty())) {
			
			boolean afterItemFound = false;
			boolean beforeItemFound = false;
			
			int i = 0;
			for (T object : objects) {
				if (after != null && after.equals(object.getId())) {
					initialIndex = i + 1;
					afterItemFound = true;
				}
				if (before != null && before.equals(object.getId())) {
					lastIndex = i;
					beforeItemFound = true;
				}
				i++;
			}
			
			if (after != null && !afterItemFound) {
				throw new IllegalArgumentException();
			}
			
			if (before != null && !before.isEmpty() && !beforeItemFound) {
				throw new IllegalArgumentException();
			}
		}
		
		if (rsm.getMax() != null) {
			if (before != null) {
				initialIndex = lastIndex - rsm.getMax();
			} else {
				lastIndex = initialIndex + rsm.getMax();
			}
		}
		
		boolean outOfRange = initialIndex > lastIndex || initialIndex < 0
				|| lastIndex > objects.size();

		List<T> filteredList = outOfRange ? new LinkedList<T>() : objects
				.subList(initialIndex, lastIndex);
		
		rsm.setCount(objects.size());
		rsm.setIndex(initialIndex);
		
		if (!filteredList.isEmpty()) {
			rsm.setFirst(filteredList.get(0).getId());
			rsm.setLast(filteredList.get(filteredList.size() - 1).getId());
		}
		
		return filteredList;
	}
	
}
