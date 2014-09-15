package org.jivesoftware.smackx.pubsub;

import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.IQ.Type;
import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.pubsub.packet.PubSub;

public class BuddycloudNode {

	private final Node node;

	public BuddycloudNode(Node node) {
		this.node = node;
	}

	public List<BuddycloudAffiliation> getBuddycloudAffiliations(List<PacketExtension> additionalExtensions,
			Collection<PacketExtension> returnedExtensions) throws NoResponseException, XMPPErrorException,
			NotConnectedException {
		
		PubSub pubSub = node.createPubsubPacket(Type.get, new NodeExtension(
				PubSubElementType.AFFILIATIONS, node.getId()));
		if (additionalExtensions != null) {
			for (PacketExtension pe : additionalExtensions) {
				pubSub.addExtension(pe);
			}
		}
		PubSub reply = (PubSub) node.sendPubsubPacket(pubSub);
		if (returnedExtensions != null) {
			returnedExtensions.addAll(reply.getExtensions());
		}
		BuddycloudAffiliations affilElem = (BuddycloudAffiliations) reply
				.getExtension(PubSubElementType.AFFILIATIONS);
		return affilElem.getAffiliations();
	}

	public String getId() {
		return node.getId();
	}

	public DiscoverInfo discoverInfo() throws NoResponseException, XMPPErrorException, NotConnectedException {
		return node.discoverInfo();
	}

	@SuppressWarnings("unchecked")
	public List<Item> getItems(List<PacketExtension> additionalExtensions,
			List<PacketExtension> returnedExtensions) throws NoResponseException, XMPPErrorException, NotConnectedException {
		PubSub request = node.createPubsubPacket(Type.get, new GetItemsRequest(getId()));
		if (additionalExtensions != null) {
			for (PacketExtension pe : additionalExtensions) {
				request.addExtension(pe);
			}
		}
		PubSub result = (PubSub) node.con.createPacketCollectorAndSend(request)
				.nextResultOrThrow();
		if (returnedExtensions != null) {
			returnedExtensions.addAll(result.getExtensions());
		}
		ItemsExtension itemsElem = (ItemsExtension) result
				.getExtension(PubSubElementType.ITEMS);
		return (List<Item>) itemsElem.getItems();
	}
}
