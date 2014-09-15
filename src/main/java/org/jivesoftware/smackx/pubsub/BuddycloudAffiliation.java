package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.packet.PacketExtension;

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
		return null;
	}

	public String toXML() {
		StringBuilder builder = new StringBuilder("<");
		builder.append(getElementName());
		appendAttribute(builder, "node", node);
		appendAttribute(builder, "affiliation", type.toString());

		builder.append("/>");
		return builder.toString();
	}

	private void appendAttribute(StringBuilder builder, String att, String value) {
		builder.append(" ");
		builder.append(att);
		builder.append("='");
		builder.append(value);
		builder.append("'");
	}

}
