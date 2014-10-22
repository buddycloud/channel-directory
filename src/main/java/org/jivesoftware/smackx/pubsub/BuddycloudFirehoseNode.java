package org.jivesoftware.smackx.pubsub;

import org.jivesoftware.smack.XMPPConnection;

public class BuddycloudFirehoseNode extends Node {

	public BuddycloudFirehoseNode(XMPPConnection connection) {
		super(connection, "/firehose");
	}

}
