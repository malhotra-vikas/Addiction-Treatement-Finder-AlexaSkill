package main.java.com.meridian.utilities;

import java.util.ArrayList;
import java.util.List;

/*
 * Stores an RSS feed
 */

public class NewsFeed {
    final String title;
    final String link;
    final String description;
    final String language;
    final String docs;

    final List<NewsFeedMessage> entries = new ArrayList<NewsFeedMessage>();

    public NewsFeed (String title, String link, String description, String language, String docs) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.language = language;
        this.docs = docs;
    }

    public List<NewsFeedMessage> getMessages() {
        return entries;
    }

    public String getTitle () {
        return title;
    }

    public String getLink () {
        return link;
    }

    public String getDescription () {
        return description;
    }

    public String getLanguage () {
        return language;
    }

    public String getDocs () {
        return docs;
    }

    public List<NewsFeedMessage> getEntries () {
        return entries;
    }
/*
    @Override
    public String toString() {
        return "Feed [copyright=" + copyright + ", description=" + description
                + ", language=" + language + ", link=" + link + ", createDate="
                + createDate + ", updateDate=" + updateDate+ ", channel-title=" + channelTitle + "]";
    }
*/
}
