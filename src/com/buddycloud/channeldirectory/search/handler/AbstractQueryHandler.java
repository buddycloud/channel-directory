package com.buddycloud.channeldirectory.search.handler;

import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * Generic QueryHandler. 
 * QueryHandler implementations should extend this class.
 * 
 * @see QueryHandler
 *  
 */
public abstract class AbstractQueryHandler implements QueryHandler {

	private final String namespace;
	private final Logger logger;
	private final Properties properties;

	/**
	 * Creates a QueryHandler for a given namespace 
	 * @param namespace
	 */
	public AbstractQueryHandler(String namespace, Properties properties) {
		this.namespace = namespace;
		this.properties = properties;
		this.logger = Logger.getLogger(getClass());
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
	protected Properties getProperties() {
		return properties;
	}
}
