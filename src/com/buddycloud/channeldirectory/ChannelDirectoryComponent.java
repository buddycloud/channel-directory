package com.buddycloud.channeldirectory;

import org.xmpp.component.AbstractComponent;

/**
 * Channel Directory XMPP Component
 * Follows the XEP-0114 (http://xmpp.org/extensions/xep-0114.html)
 * 
 */
public class ChannelDirectoryComponent extends AbstractComponent {

	private static final String DESCRIPTION = "A pub sub search engine, " +
			"metadata crawler and recomendation service";
	private static final String NAME = "Channel Directory";

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}

}
