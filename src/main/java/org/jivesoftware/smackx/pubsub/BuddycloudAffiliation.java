package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.pubsub.packet.PubSub;

public class BuddycloudAffiliation implements PacketExtension {

	protected String node;
	protected Type type;

	public enum Type {
		member, none, outcast, owner, publisher, moderator, followerPlus
	}

	public BuddycloudAffiliation(String nodeId, Type affiliation) {
		node = nodeId;
		type = affiliation;
	}

	public String getNodeId() {
		return node;
	}

	public Type getType() {
		return type;
	}

	public String getElementName() {
		return "subscription";
	}

	public String getNamespace() {
		return PubSub.NAMESPACE;
	}

	@Override
	public CharSequence toXML() {
		XmlStringBuilder xml = new XmlStringBuilder();
		xml.halfOpenElement(getElementName());
		xml.optAttribute("node", node);
		xml.optAttribute("affiliation", type.toString());
		xml.closeEmptyElement();
		return xml;
	}

}
