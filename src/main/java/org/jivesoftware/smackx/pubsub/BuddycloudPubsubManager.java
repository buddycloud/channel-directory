package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.disco.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public class BuddycloudPubsubManager {

	static {
		// Use the BuddycloudAffiliationsProvider
		BuddycloudAffiliationsProvider affiliationsProvider = new BuddycloudAffiliationsProvider();
		ProviderManager.addExtensionProvider(PubSubElementType.AFFILIATIONS.getElementName(), 
				PubSubNamespace.OWNER.getXmlns(), affiliationsProvider);
		ProviderManager.addExtensionProvider(PubSubElementType.AFFILIATIONS.getElementName(), 
				PubSubNamespace.BASIC.getXmlns(), affiliationsProvider);
		
		// Use the BuddycloudAffiliationProvider for buddycloud-specific affiliations
		BuddycloudAffiliationProvider affiliationProvider = new BuddycloudAffiliationProvider();
		ProviderManager.addExtensionProvider("affiliation", 
				PubSubNamespace.OWNER.getXmlns(), affiliationProvider);
		ProviderManager.addExtensionProvider("affiliation", 
				PubSubNamespace.BASIC.getXmlns(), affiliationProvider);
	}
	
	private final PubSubManager manager;
	
	public BuddycloudPubsubManager(XMPPConnection connection, String toAddress) {
		this.manager = new PubSubManager(connection, toAddress);
	}

	public BuddycloudNode getNode(String id) throws NoResponseException,
			XMPPErrorException, NotConnectedException {
		return new BuddycloudNode(manager.getNode(id));
	}

	public DiscoverItems discoverNodes(String nodeId)
			throws NoResponseException, XMPPErrorException,
			NotConnectedException {
		return manager.discoverNodes(nodeId);
	}
}
