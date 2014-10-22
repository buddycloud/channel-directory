package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public class BuddycloudAffiliation implements PacketExtension {

	public static final String ELEMENT_NAME = "affiliation";
	
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
		return ELEMENT_NAME;
	}

	public String getNamespace() {
		return PubSubNamespace.OWNER.getXmlns();
	}

	@Override
	public CharSequence toXML() {
		XmlStringBuilder xml = new XmlStringBuilder();
		xml.halfOpenElement(getElementName());
		xml.optAttribute("node", node);
		xml.optAttribute(ELEMENT_NAME, type.toString());
		xml.closeEmptyElement();
		return xml;
	}

}
