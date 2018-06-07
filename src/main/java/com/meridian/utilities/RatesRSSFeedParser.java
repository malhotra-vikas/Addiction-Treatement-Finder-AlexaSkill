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

public class RatesRSSFeedParser {

    static final String NAME = "name";
    static final String QUOTE_DATE = "quotedate";
    static final String VALUE = "value";
    static final String SYMBOL = "symbol";

    static final String RECORD = "record";
    static final String TFC_RECORDS = "TFCrecords";


    final URL url;

    public RatesRSSFeedParser (String feedUrl) {
        try {
            this.url = new URL(feedUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public RatesFeed readFeed() throws FileReaderException {
        RatesFeed feed = null;
        try {
            boolean isFeedHeader = true;
            // Set header values intial to the empty string
            String name = "";
            String quoteDate = "";
            String value = "";
            String symbol = "";

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
                        case TFC_RECORDS:
                            if (isFeedHeader) {
                                isFeedHeader = false;
                                feed = new RatesFeed();
                            }
                            event = eventReader.nextEvent();
                            break;
                        case NAME:
                            name = getCharacterData(event, eventReader);
                            break;
                        case QUOTE_DATE:
                            quoteDate = getCharacterData(event, eventReader);
                            break;
                        case VALUE:
                            value= getCharacterData(event, eventReader);
                            break;
                        case SYMBOL:
                            symbol = getCharacterData(event, eventReader);
                            break;
                    }
                } else if (event.isEndElement()) {
                    if (event.asEndElement().getName().getLocalPart() == (RECORD)) {
                        RatesFeedMessage message = new RatesFeedMessage();
                        message.setQuoteDate(quoteDate);
                        message.setName(name);
                        message.setValue(value);
                        message.setSymbol(symbol);

                        feed.getMessages().add(message);
                        event = eventReader.nextEvent();
                        continue;
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new FileReaderException(e.toString());
        }
        return feed;
    }

    private String getCharacterData(XMLEvent event, XMLEventReader eventReader)
            throws XMLStreamException {
        String result = "";
        event = eventReader.nextEvent();
        if (event instanceof Characters) {
            result = event.asCharacters().getData();
            System.out.println(result);
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
