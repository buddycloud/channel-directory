package com.buddycloud.channeldirectory.search.handler;

import org.xmpp.packet.IQ;

/**
 * Handle content queries (iq gets) to this component.
 * 
 */
public interface QueryHandler {

	IQ handle(IQ query);
	
	String getNamespace();
	
}
