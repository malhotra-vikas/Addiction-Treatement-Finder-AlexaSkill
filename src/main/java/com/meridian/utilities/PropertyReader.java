package main.java.com.meridian.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {
    private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);

    private static PropertyReader propertyReader;
    private String skillName = "";
    private String newsFeedUrl= "";
    private String ratesFeedUrl= "";
    private boolean propertyRead = false;
    private String fatalError = "";
    private String welcomeMessage = "";
    private String speechHelp = "";
    private String goodBye = "";
    private String speechReprompt= "";
    private String speechSorry = "";
    private String skillId = "";

    private PropertyReader() {
        Properties skillProperties = new Properties();
        InputStream input = null;

        try {
            input = new FileInputStream("skill.properties");
            // load a properties file

            skillProperties.load(input);
            skillName = skillProperties.getProperty("skill");
            newsFeedUrl= skillProperties.getProperty("news-rss-feed-url");
            ratesFeedUrl= skillProperties.getProperty("rates-rss-feed-url");

            fatalError = skillProperties.getProperty("speech-fatal-error");
            welcomeMessage = skillProperties.getProperty("speech-welcome");
            speechHelp = skillProperties.getProperty("speech-help");
            goodBye = skillProperties.getProperty("speech-goodbye");
            speechSorry = skillProperties.getProperty("speech-sorry");
            speechReprompt = skillProperties.getProperty("speech-reprompt");
            log.debug("in SINGLETOn - speechSorry " + speechSorry);
            log.info("Coming from LOG 4 J - The skill name is :- " + skillName);
            log.debug("Coming from LOG 4 J - The skill name is :- " + skillName);

            skillId = skillProperties.getProperty("skill-id");

            propertyRead = true;

            log.info("Coming from LOG 4 J - The skill name is :- " + skillName);
            log.info("Coming from LOG 4 J - The news feed URL is :- " + newsFeedUrl);

        } catch (IOException ioException) {
             propertyRead = false;
            log.error("Coming from LOG 4 J - Skill Property file not loaded");
        }

    }

    public String getSkillId () {
        return skillId;
    }

    public String getSpeechReprompt () {
        return speechReprompt;
    }

    public String getSpeechSorry () {
        return speechSorry;
    }

    public String getGoodBye () {
        return goodBye;
    }

    public String getSpeechHelp () {
        return speechHelp;
    }

    public String getFatalError () {
        return fatalError;
    }

    public String getWelcomeMessage () {
        return welcomeMessage;
    }

    public boolean isPropertyRead () {
        return propertyRead;
    }

    public String getSkillName () {
        return skillName;
    }

    public String getNewsFeedUrl () {
        return newsFeedUrl;
    }

    public String getRatesFeedUrl () {
        return ratesFeedUrl;
    }

    public static PropertyReader getPropertyReader () {
        if (propertyReader == null) {
            propertyReader = new PropertyReader();
        }
        return propertyReader;
    }

}
