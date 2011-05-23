package com.buddycloud.channeldirectory.handler;

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

	/**
	 * Creates a QueryHandler for a given namespace 
	 * @param namespace
	 */
	public AbstractQueryHandler(String namespace) {
		this.namespace = namespace;
		this.logger = Logger.getLogger(getClass());
	}
	
	@Override
	public String getNamespace() {
		return namespace;
	}
	
	protected Logger getLogger() {
		return logger;
	}
	
}
