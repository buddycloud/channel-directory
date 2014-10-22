package org.jivesoftware.smackx.pubsub;

import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.packet.PacketExtension;
import org.jivesoftware.smack.provider.EmbeddedExtensionProvider;

public class BuddycloudAffiliationProvider extends EmbeddedExtensionProvider {

	@Override
	protected PacketExtension createReturnExtension(String currentElement,
			String currentNamespace, Map<String, String> attributeMap,
			List<? extends PacketExtension> content) {
		return new BuddycloudAffiliation(attributeMap.get("jid"),
				BuddycloudAffiliation.Type.valueOf(attributeMap.get("affiliation")));
	}

}
