package org.jivesoftware.smackx.pubsub;

import java.util.Collections;
import java.util.List;

import org.jivesoftware.smackx.pubsub.NodeExtension;
import org.jivesoftware.smackx.pubsub.PubSubElementType;

public class BuddycloudAffiliations extends NodeExtension {

	protected List<BuddycloudAffiliation> items = Collections.emptyList();

	public BuddycloudAffiliations() {
		super(PubSubElementType.AFFILIATIONS);
	}

	public BuddycloudAffiliations(List<BuddycloudAffiliation> subList) {
		super(PubSubElementType.AFFILIATIONS);
		items = subList;
	}

	public List<BuddycloudAffiliation> getAffiliations() {
		return items;
	}

	@Override
	public CharSequence toXML() {
		if ((items == null) || (items.size() == 0)) {
			return super.toXML();
		} else {
			StringBuilder builder = new StringBuilder("<");
			builder.append(getElementName());
			builder.append(">");

			for (BuddycloudAffiliation item : items) {
				builder.append(item.toXML());
			}

			builder.append("</");
			builder.append(getElementName());
			builder.append(">");
			return builder.toString();
		}
	}

}
