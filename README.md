A channel search engine, metadata crawler and content recommendation service.

Aim
---

The channel Directory project comprises three areas:

Gathering

- spidering open channels for content and followers
- receiving posts via a firehose

Searching

- Finding nearby content
- Searching channel metadata
- Searching channel content

Recommending

- show me channels similar to this one
- based on the channels I follow, recommend more

The aim of the channel directories is to help users find interesting content to follow. For example, being at a conference and wondering whether there is a channel covering that conference.

When channels are created and updated, and have an open access model, their existence and details are sent to one of a list of competing directory servers. These directory servers maintain a reasonably current list of all the open access channels in the channel-verse.

One could think of directory servers as the [ping-o-matic][] of the channel-verse.

Hidden channels are never shared with the directory servers.

Users can choose their preferred directory server or run their own.

Channel directory should support RSM-able queries, just like [XEP-0059][].

Right now, the directory is running at the host **crater.buddycloud.org**, under the address **search.buddycloud.org**.

Demo: 

https://demo.buddycloud.org/api/search?type=metadata&max=5&q=test
https://demo.buddycloud.org/api/search?type=metadata&max=5&q=imaginator

Show me nearby topic channels
-----------------------------

-   data comes from the channel metadata
    -   Topic channels are tagged with a lat/long.
    -   does not return personal channels. (Personal channels do not have location metadata attached to them (location sharing in personal channels is done via a dedicated pub-sub node))
-   postgis/mahout/solr or some other GIS based nearby query

### Protocol request

``` xml
 <iq to='directory.example.org' from='thedude@coffee-channels.com' type='get'>
    <query xmlns='http://buddycloud.com/channel_directory/nearby_query'>
       <point lat='48.167222' lon='11.586111' />
         <options>
           <feature var='title' />
           <feature var='description' />
           <feature var='location' />
         </options>
       <set xmlns='http://jabber.org/protocol/rsm'>
          <max>10</max>
       </set>
    </query>
 </iq>
```

### Protocol response

``` xml
<iq from="directory.example.org" type="result" to="thedude@coffee-channels.com" >
   <query xmlns="http://buddycloud.com/channel_directory/nearby_query">
      <item jid="topicchanne01@example.org" type="channel">
         <title>A channel about topic 01</title>
         <description>A channel description</title>
         <geoloc xmlns='http://jabber.org/protocol/geoloc'>
            <lat>45.44</lat>
            <lon>12.33</lon>
         </geoloc>
      </item>
      <item jid="topicchanne02@example.org" type="channel">
         <title>A channel about topic 01</title>
         <description>A channel description</title>
         <geoloc xmlns='http://jabber.org/protocol/geoloc'>
            <lat>45.44</lat>
            <lon>12.33</lon>
         </geoloc>
      </item>
      <set xmlns='http://jabber.org/protocol/rsm'>
         <first index='0'>topicchanne01@example.org</first>
         <last>topicchanne02@example.org</last>
         <count>2</count>
      </set>
   </query>
</iq>
```

Recommend me channels to follow
-------------------------------

-   data comes from the channe

  [ping-o-matic]: http://codex.wordpress.org/Update_Services
  [XEP-0059]: http://xmpp.org/extensions/xep-0059.html#schema


Architecture
---
![Buddycloud crawler  architecture](design%20docs/Architecture.png "Buddycloud crawler architecture")

(c) Buddycloud. 
Apache 2.0 license.
