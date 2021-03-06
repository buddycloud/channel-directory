package com.buddycloud.channeldirectory.crawler.node;

import java.util.List;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Name;
import org.xbill.DNS.Record;
import org.xbill.DNS.SRVRecord;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

public class DiscoveryUtils {

	private static Logger LOGGER = Logger.getLogger(DiscoveryUtils.class);
	
	private static final String IDENTITY_CATEGORY = "pubsub";
    private static final String IDENTITY_TYPE = "channels";
	private static final String SRV_PREFIX = "_buddycloud-server._tcp.";
	
    public static String discoverChannelServer(XMPPConnection connection, String domain) {
    	try {
    		String channelServer = doDNSDiscovery(domain);
			if (channelServer != null) {
				return channelServer;
			}
		} catch (Exception e) {
			LOGGER.warn("No SRV records for " + domain + ", trying XMPP disco.");
		}
    	
    	try {
    		return doXMPPDiscovery(connection, domain);
    	} catch (Exception e) {
    		LOGGER.warn("No XMPP disco entries for " + domain + ", giving up.");
    		return null;
    	}
	}

	private static String doDNSDiscovery(String domain) throws TextParseException {
		Lookup lookup = new Lookup(SRV_PREFIX + domain, Type.SRV);
		Record recs[] = lookup.run();
		if (recs == null) {
			throw new RuntimeException("Could not lookup domain.");
		}

		for (Record rec : recs) {
			SRVRecord record = (SRVRecord) rec;
			Name target = record.getTarget();
			if (target != null) {
				String targetStr = target.toString();
				return targetStr.substring(0, targetStr.length() - 1);
			}
		}
		return null;
	}

	private static String doXMPPDiscovery(XMPPConnection connection,
			String domain) {
		ServiceDiscoveryManager discoManager = ServiceDiscoveryManager.getInstanceFor(connection);
		PubSubManager pubSubManager = new PubSubManager(connection, domain);
		
		DiscoverItems discoverItems = null;
		try {
			discoverItems = pubSubManager.discoverNodes(null);
		} catch (Exception e) {
			LOGGER.error("Error while trying to fetch domain " + domain
					+ "node", e);
			return null;
		}
		List<DiscoverItems.Item> items = discoverItems.getItems();
		for (DiscoverItems.Item item : items) {
			String entityID = item.getEntityID();
			DiscoverInfo discoverInfo = null;
			try {
				discoverInfo = discoManager.discoverInfo(entityID);
			} catch (Exception e) {
				continue;
			}
			List<DiscoverInfo.Identity> identities = discoverInfo
					.getIdentities();
			for (DiscoverInfo.Identity identity : identities) {
				if (isChannelServerIdentity(identity)) {
					return entityID;
				}
			}
		}
		return null;
	}
	
	private static boolean isChannelServerIdentity(DiscoverInfo.Identity identity) {
        return identity.getCategory().equals(IDENTITY_CATEGORY) && identity.getType().equals(IDENTITY_TYPE);
    }
	
}
