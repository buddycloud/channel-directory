package com.buddycloud.channeldirectory.utils;

import org.apache.log4j.Logger;
import org.xmpp.packet.IQ;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.packet.PacketError.Type;

public class XMPPUtils {

	
	/**
	 * Logs the error and returns an IQ error response
	 * 
	 * @param iq
	 * @param errorMessage
	 * @param logger
	 * @return
	 */
	public static IQ error(IQ iq, String errorMessage, Logger logger) {
		logger.error(errorMessage);
		return XMPPUtils.createErrorResponse(iq, errorMessage);
	}
	
	/**
	 * Creates an error response for a given IQ request.  
	 * 
	 * @param request
	 * @param message
	 * @return
	 */
	public static IQ createErrorResponse(final IQ request, final String message) {
		final IQ result = request.createCopy();
		result.setID(request.getID());
		result.setFrom(request.getTo());
		result.setTo(request.getFrom());
		
		PacketError e = new PacketError(Condition.bad_request, Type.modify);
		if(message != null) {
			e.setText(message);
		}
		result.setError(e);
		
		return result;
	}
	
}
