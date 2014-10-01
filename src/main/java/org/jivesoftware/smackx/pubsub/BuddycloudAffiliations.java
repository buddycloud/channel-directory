package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.util.XmlStringBuilder;
import org.jivesoftware.smackx.pubsub.packet.PubSubNamespace;

public class BuddycloudAffiliations implements PacketExtension {

	public static final String ELEMENT_NAME = "affiliations";
	
	protected List<BuddycloudAffiliation> items = Collections.emptyList();

	public BuddycloudAffiliations(List<BuddycloudAffiliation> subList) {
		items = subList;
	}

	public List<BuddycloudAffiliation> getAffiliations() {
		return items;
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
		xml.openElement(getElementName());
		if (items != null) {
			for (BuddycloudAffiliation affiliation : items) {
				xml.append(affiliation.toXML());
			}
		}
		xml.closeElement(getElementName());
		return xml;
	}

}
