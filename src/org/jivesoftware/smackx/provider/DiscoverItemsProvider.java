/**
 * $RCSfile$
 * $Revision: 7071 $
 * $Date: 2007-02-11 21:59:05 -0300 (dom, 11 fev 2007) $
 *
 * Copyright 2003-2007 Jive Software.
 *
 * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jivesoftware.smackx.provider;

import java.io.IOException;

import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smackx.packet.*;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
* The DiscoverInfoProvider parses Service Discovery items packets.
*
* @author Gaston Dombiak
*/
public class DiscoverItemsProvider implements IQProvider {

    public IQ parseIQ(XmlPullParser parser) throws Exception {
        DiscoverItems discoverItems = new DiscoverItems();
        boolean done = false;
        DiscoverItems.Item item;
        String jid = "";
        String name = "";
        String action = "";
        String node = "";
        discoverItems.setNode(parser.getAttributeValue("", "node"));
        
        while (!done) {
            int eventType = parser.next();
            
            if (eventType == XmlPullParser.START_TAG && "item".equals(parser.getName())) {
                // Initialize the variables from the parsed XML
                jid = parser.getAttributeValue("", "jid");
                name = parser.getAttributeValue("", "name");
                node = parser.getAttributeValue("", "node");
                action = parser.getAttributeValue("", "action");
            }
            else if (eventType == XmlPullParser.END_TAG && "item".equals(parser.getName())) {
                // Create a new Item and add it to DiscoverItems.
                item = new DiscoverItems.Item(jid);
                item.setName(name);
                item.setNode(node);
                item.setAction(action);
                discoverItems.addItem(item);
            } else if (eventType == XmlPullParser.START_TAG && "set".equals(parser.getName())) {
            	RSMSet rsmSet = parseRSM(parser);
            	discoverItems.setRsmSet(rsmSet);
            } 
            else if (eventType == XmlPullParser.END_TAG && "query".equals(parser.getName())) {
                done = true;
            }
        }

        return discoverItems;
    }

	private RSMSet parseRSM(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		RSMSet rsmSet = new RSMSet();
		
		boolean readingFirst = false;
		boolean readingLast = false;
		boolean readingCount = false;
		
		while (true) {
			int eventType = parser.next();
			
			if (eventType == XmlPullParser.END_TAG && "set".equals(parser.getName())) {
				break;
			} else if (eventType == XmlPullParser.START_TAG) {
				if ("first".equals(parser.getName())) {
					rsmSet.setIndex(Integer.parseInt(parser.getAttributeValue("", "index")));
					readingFirst = true;
				} else if ("last".equals(parser.getName())) {
					readingLast = true;
				} else if ("count".equals(parser.getName())) {
					readingCount = true;
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if ("first".equals(parser.getName())) {
					readingFirst = false;
				} else if ("last".equals(parser.getName())) {
					readingLast = false;
				} else if ("count".equals(parser.getName())) {
					readingCount = false;
				}
			} else if (eventType == XmlPullParser.TEXT) {
				if (readingFirst) {
					rsmSet.setFirst(parser.getText());
				} else if (readingLast) {
					rsmSet.setLast(parser.getText());
				} else if (readingCount) {
					rsmSet.setCount(Integer.parseInt(parser.getText()));
				}
			}
			
		}
		return rsmSet;
	}
}