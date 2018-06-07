package main.java.com.meridian.utilities;

/*
 * Represents one RSS message
 */

public class NewsFeedMessage {
    String title;
    String description;
    String encodedContent;


    public String getTitle () {
        return title;
    }

    public void setTitle (String title) {
        this.title = title;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public String getEncodedContent () {
        return encodedContent;
    }

    public void setEncodedContent (String encodedContent) {
        this.encodedContent = encodedContent;
    }

    /*
    @Override
    public String toString() {
        return "FeedMessage [news-title=" + title + ", Description=" + description+ ", EncodedContent=" + encodedContent+ "]";
    }
    */
}
