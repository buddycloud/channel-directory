package com.buddycloud.channeldirectory.search.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;

/**
 * Utility methods for options filtering.
 * 
 */
public class FeatureUtils {

	/**
	 * Parses options features from an IQ request
	 * Returns an empty map if there is no <options> tag
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Set<String> parseOptions(Element queryElement) {
		Element optionsElement = queryElement.element("options");
		Set<String> options = new HashSet<String>();
		
		if (optionsElement == null) {
			return options;
		}
		
		List<Element> features = optionsElement.elements("feature");
		for (Element element : features) {
			options.add(element.attributeValue("var"));
		}
		
		return options;
	}

	/**
	 * Adds an attribute to the parent element if the corresponding 
	 * feature is contained in the options map or this map is empty.
	 * 
	 * @param options
	 * @param parentElement
	 * @param key
	 * @param value
	 * @return
	 */
	public static boolean addAttribute(Set<String> options,
			Element parentElement, String key, String value) {
		if (options.contains(key) || options.isEmpty()) {
			parentElement.addAttribute(key, value);
			return true;
		}
		return false;
	}

	/**
	 * Adds an element to the parent element and set its text
	 * if the corresponding feature is contained in the options map
	 * or this map is empty.
	 * 
	 * @param options
	 * @param parentElement
	 * @param key
	 * @param value
	 * @return
	 */
	public static Element addElement(Set<String> options,
			Element parentElement, String key, String value) {
		if (options.contains(key) || options.isEmpty()) {
			Element el = parentElement.addElement(key);
			if (value != null) {
				el.setText(value);
			}
			return el; 
		}
		return null;
	}

	/**
	 * Adds an element to the parent element and set its namespace
	 * if the corresponding feature is contained in the options map
	 * or this map is empty.
	 * 
	 * @param options
	 * @param parentElement
	 * @param key
	 * @param namespace
	 * @return
	 */
	public static Element addNamespaceElement(Set<String> options,
			Element parentElement, String key, String namespace) {
		if (options.contains(key) || options.isEmpty()) {
			return parentElement.addElement(key, namespace);
		}
		return null;
	}

}
