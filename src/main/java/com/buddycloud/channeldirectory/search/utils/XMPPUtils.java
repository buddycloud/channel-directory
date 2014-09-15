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
package com.buddycloud.channeldirectory.search.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
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
		return XMPPUtils.createErrorResponse(iq, errorMessage, 
				Condition.bad_request, Type.modify);
	}
	
	/**
	 * @param iq
	 * @param errorMessage
	 * @param logger
	 * @return
	 */
	public static IQ error(IQ iq, String errorMessage, Exception e, Logger logger) {
		logger.error(errorMessage, e);
		return XMPPUtils.createErrorResponse(iq, errorMessage, 
				Condition.bad_request, Type.modify);
	}
	
	/**
	 * Logs the RSM page not found error and returns an IQ error response
	 * 
	 * @param iq
	 * @param errorMessage
	 * @param logger
	 * @return
	 */
	public static IQ errorRSM(IQ iq, Logger logger) {
		String rsmMessage = "RSM: Page Not Found";
		logger.error(rsmMessage + " " + iq);
		return XMPPUtils.createErrorResponse(iq, rsmMessage, 
				Condition.item_not_found, Type.cancel);
	}
	
	/**
	 * Creates an error response for a given IQ request.  
	 * 
	 * @param request
	 * @param message
	 * @param condition
	 * @param type
	 * @return
	 */
	public static IQ createErrorResponse(final IQ request, final String message, 
			Condition condition, Type type) {
		final IQ result = request.createCopy();
		result.setID(request.getID());
		result.setFrom(request.getTo());
		result.setTo(request.getFrom());
		
		PacketError e = new PacketError(condition, type);
		if(message != null) {
			e.setText(message);
		}
		result.setError(e);
		
		return result;
	}

	public static XMPPConnection createCrawlerConnection(Properties configuration)
			throws Exception {
		
		String serviceName = configuration.getProperty("crawler.xmpp.servicename");
		String host = configuration.getProperty("crawler.xmpp.host");
		String userName = configuration.getProperty("crawler.xmpp.username");
		
		ConnectionConfiguration cc = new ConnectionConfiguration(
				host,
				Integer.parseInt(configuration.getProperty("crawler.xmpp.port")),
				serviceName);
		cc.setReconnectionAllowed(true);
		cc.setHostnameVerifier(new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, new TrustManager[]{createTrustAllManager()}, new java.security.SecureRandom());
		cc.setCustomSSLContext(sc);
		
		XMPPTCPConnection connection = new XMPPTCPConnection(cc);
		connection.connect();
		connection.setPacketReplyTimeout(10000L);
		connection.login(userName, 
				configuration.getProperty("crawler.xmpp.password"), 
				"crawler-" + new Random().nextLong());
		
		return connection;
	}

	private static TrustManager createTrustAllManager() {
		TrustManager tm = new X509TrustManager() {
			
			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
			
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				
			}
			
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				
			}
		};
		return tm;
	}
}
