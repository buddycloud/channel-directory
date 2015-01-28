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

One could think of directory servers as the ping-o-matic of the channel-verse.

Hidden channels are never shared with the directory servers.
Users can choose their preferred directory server or run their own.
Channel directory should support RSM-able queries, just like XEP-0059.
Right now, the directory is running at the host crater.buddycloud.org, under the address search.buddycloud.org.

Show me nearby topic channels

- data comes from the channel metadata
- Topic channels are tagged with a lat/long.
- does not return personal channels. (Personal channels do not have location metadata attached to them (location sharing in personal channels is done via a dedicated pub-sub node))
- postgis/mahout/solr or some other GIS based nearby query

Protocol request

```xml
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

Protocol response
```xml
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
- data comes from the channel subscriber data (do we treat moderator roles differently? does affiliation data add any value at all?)
- probably Apache Mahout
- input: use the quering jid
- output: channel list ordered by ranking
- output: should exclude channels that the user is already following
- return only the type of channels asked for (personal/topic)

Protocol request
```xml
 <iq to='directory.example.org' from='thedude@coffee-channels.com' type='get'>
    <query xmlns='http://buddycloud.com/channel_directory/recommendation_query'>
       <user-jid>thedude@coffee-channels.com</user-jid>
         <options>
           <feature var='title' />
           <feature var='description' />
           <feature var='location' />
           <feature var='topic-channels' />
           <feature var='personal-channels' />
         </options>
         <set xmlns='http://jabber.org/protocol/rsm'>
           <max>10</max>
         </set>
      </query>
 </iq>
 ```
 
Protocol response

```xml
<iq from="directory.example.org" to="thedude@coffee-channels.com"  type="result" >
    <query xmlns='http://buddycloud.com/channel_directory/recommendation_query'>
      <item jid="recommendedchannel@example.org" type="channel">
         <title>A channel title</title>
         <description>A channel description</title>
         <channel-type>topic</channel-type>
         <geoloc xmlns='http://jabber.org/protocol/geoloc'>
            <lat>45.44</lat>
            <lon>12.33</lon>
          </geoloc>
      </item>
      <item jid="recommendedchannel02@example.org" type="channel">
         <title>Another channel title</title>
         <description>This is a channel - a personal channel type</title>
         <channel-type>personal</channel-type>
      </item>
      <set xmlns='http://jabber.org/protocol/rsm'>
         <first index='0'>recommendedchannel@example.org</first>
         <last>trecommendedchannel02@example.org</last>
         <count>2</count>
      </set>
   </query>
</iq>
```

Show similar channels
- "Show me 3 topic channels similar to coffee@coffee-lovers.com"
- "I recommend espressofreaks@java-junkies.com, brewmeisters@coffee.com and beangrinderdude@wide-awake.com"
- input: channel jid
- output: list of similar channels listed by relevancy

Protocol request
```xml
 <iq to='directory.example.org' from='thedude@coffee-channels.com' type='get'>
    <query xmlns='http://buddycloud.com/channel_directory/similar_channels'>
       <channel-jid>channel@example.org</channel-jid>
         <options>
           <feature var='topic-channels' />
           <feature var='personal-channels' />
           <feature var='title' />
           <feature var='description' />
           <feature var='location' />
           <feature var='channel-type' />
         </options>
         <set xmlns='http://jabber.org/protocol/rsm'>
           <max>10</max>
         </set>
      </query>
 </iq>
```
Protocol response
```xml
<iq from="directory.example.org" to="thedude@coffee-channels.com"  type="result" >
    <query xmlns='http://buddycloud.com/channel_directory/similar_channels'>
      <item jid="similarchannel@example.org" type="channel">
         <title>A channel title</title>
         <description>A channel description</title>
         <geoloc xmlns='http://jabber.org/protocol/geoloc'>
            <lat>45.44</lat>
            <lon>12.33</lon>
         </geoloc>
       <channel-type>topic</channel-type>
      </item>
      <item jid="similarchannel02@example.org" type="channel">
         <title>Another channel title</title>
         <description>This is a channel with no location set even though it was requested in the query</title>
      </item>
      <set xmlns='http://jabber.org/protocol/rsm'>
         <first index='0'>similarchannel@example.org</first>
         <last>similarchannel02@example.org</last>
         <count>2</count>
      </set>
   </query>
</iq>
```

Search channel metadata
- data comes from channel descriptions and title
- probably based on Apache Solr
- Should geolocation be also returned here?
- Protocol request

```xml
 <iq to='directory.example.org' from='thedude@coffee-channels.com' type='get'>
    <query xmlns='http://buddycloud.com/channel_directory/metadata_query'>
       <search>topic</search>
           <feature var='topic-channels' />
           <feature var='personal-channels' />
           <feature var='title' />
           <feature var='description' />
           <feature var='location' />
           <feature var='channel-type' />
       <set xmlns='http://jabber.org/protocol/rsm'>
          <max>10</max>
       </set>
    </query>
 </iq>
```
Protocol response
```xml
<iq from="directory.example.org" type="result" to="thedude@coffee-channels.com" >
   <query xmlns="http://buddycloud.com/channel_directory/metadata_query">
      <item jid="topicchanne01@example.org" type="channel">
         <title>A channel about topic 01</title>
         <geoloc xmlns='http://jabber.org/protocol/geoloc'>
            <lat>45.44</lat>
            <lon>12.33</lon>
         </geoloc>
      </item>
      <item jid="topicchanne02@example.org" type="channel">
         <title>A channel about topic 01</title>
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
Search the contents of channel posts
- data comes from comes from channel posts
- probably based on Apache Solr

Protocol request
```xml
 <iq to='directory.example.org' from='thedude@coffee-channels.com' type='get'>
    <query xmlns='http://buddycloud.com/channel_directory/content_query'>
       <search>topic</search>
       <set xmlns='http://jabber.org/protocol/rsm'>
          <max>10</max>
       </set>
    </query>
 </iq>
```
Protocol response
```xml
<iq from="directory.example.org" type="result" to="thedude@coffee-channels.com" >
   <query xmlns="http://buddycloud.com/channel_directory/content_query">
       <item id="1291048810046" type="post"> 
         <entry xmlns="http://www.w3.org/2005/Atom" xmlns:thr="http://purl.org/syndication/thread/1.0"> 
            <author>fahrertuer@example.com</author> 
            <content type="text">A comment, wondering what all this testing does</content> 
            <published>2010-11-29T16:40:10Z</published> 
            <updated>2010-11-29T16:40:10Z</updated> 
            <id>/user/channeluser@example.com/channel:1291048810046</id> 
            <geoloc xmlns="http://jabber.org/protocol/geoloc"> 
               <text>Bremen, Germany</text> 
               <locality>Bremen</locality> 
               <country>Germany</country> 
            </geoloc> 
            <thr:in-reply-to ref="1291048772456"/> 
         </entry> 
      </item>
 
      <item id="1291048810047" type="post"> 
         <entry xmlns="http://www.w3.org/2005/Atom" xmlns:thr="http://purl.org/syndication/thread/1.0"> 
            <author>foo@example.com</author> 
            <content type="text">Another comment for testing purposes</content> 
            <published>2010-11-29T16:40:15Z</published> 
            <updated>2010-11-29T16:40:15Z</updated> 
            <id>/user/channeluser@example.com/channel:1291048810047</id> 
            <geoloc xmlns="http://jabber.org/protocol/geoloc"> 
               <text>Rio de Janeiro, Brazil</text> 
               <locality>Rio de Janeiro</locality> 
               <country>Brazil</country> 
            </geoloc> 
         </entry> 
      </item>
 
      <set xmlns='http://jabber.org/protocol/rsm'>
         <first index='0'>1291048810046</first>
         <last>1291048810047</last>
         <count>2</count>
      </set>
   </query>
</iq>
```


Architecture
---
![Buddycloud crawler  architecture](design%20docs/Architecture.png "Buddycloud crawler architecture")

(c) Buddycloud. 
Apache 2.0 license.
