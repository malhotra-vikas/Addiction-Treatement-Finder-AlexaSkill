package main.java.com.meridian.utilities;

import main.java.com.meridian.exception.FileReaderException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class NewsRSSFeedParser {
    static final String CHANNEL = "channel";
    static final String TITLE = "title";
    static final String LINK = "link";
    static final String DESCRIPTION = "description";
    static final String DOCS = "docs";
    static final String LANGUAGE = "language";
    static final String ITEM = "item";

    static final String CONTENTENCODED= "encoded";

    final URL url;

    public NewsRSSFeedParser (String feedUrl) {
        try {
            this.url = new URL(feedUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public NewsFeed readFeed() throws FileReaderException {
        NewsFeed newsFeed = null;
        try {
            boolean isFeedHeader = true;
            // Set header values intial to the empty string
            String title = "";
            String docs = "";
            String description = "";
            String channelTitle = "";
            String newsTitle = "";
            String link = "";
            String language = "";
            String summary = "";
            String encodedContent = "";

            // First create a new XMLInputFactory
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            // Setup a new eventReader
            InputStream in = read();
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
            // read the XML document
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (event.isStartElement()) {
                    String localPart = event.asStartElement().getName()
                            .getLocalPart();
                    switch (localPart) {
                        case ITEM:
                            if (isFeedHeader) {
                                isFeedHeader = false;
                                newsFeed = new NewsFeed(title, link, description, language, docs);
                            }
                            event = eventReader.nextEvent();
                            break;
                        case TITLE:
                            title = getCharacterData(event, eventReader);
                            break;
                        case DESCRIPTION:
                            description = getCharacterData(event, eventReader);
                            break;
                        case LINK:
                            link = getCharacterData(event, eventReader);
                            break;
                        case LANGUAGE:
                            language = getCharacterData(event, eventReader);
                            break;
                        case CONTENTENCODED:
                            encodedContent = getCharacterData(event, eventReader);
                            break;
                        case DOCS:
                            docs = getCharacterData(event, eventReader);
                            break;
                    }
                } else if (event.isEndElement()) {
                    if (event.asEndElement().getName().getLocalPart() == (ITEM)) {
                        NewsFeedMessage message = new NewsFeedMessage();
                        message.setTitle(title);
                        message.setDescription(description);
                        message.setEncodedContent(encodedContent);
                        newsFeed.getMessages().add(message);
                        event = eventReader.nextEvent();
                        continue;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new FileReaderException(e.toString());
        }
        return newsFeed;
    }

    private String getCharacterData(XMLEvent event, XMLEventReader eventReader)
            throws XMLStreamException {
        String result = "";
        event = eventReader.nextEvent();
        if (event instanceof Characters) {
            result = event.asCharacters().getData();
//            System.out.println(result);
        }
        return result;
    }

    private InputStream read() throws FileReaderException {
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new FileReaderException(e.toString());
        }
    }
}
