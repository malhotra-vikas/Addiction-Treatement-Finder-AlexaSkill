/**
    Copyright  */
package main.java.com.meridian.alexa.skill;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.*;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.amazon.speech.speechlet.services.DirectiveEnvelope;
import com.amazon.speech.speechlet.services.DirectiveEnvelopeHeader;
import com.amazon.speech.speechlet.services.DirectiveService;
import com.amazon.speech.speechlet.services.SpeakDirective;
import com.amazon.speech.ui.*;
import main.java.com.meridian.exception.FileReaderException;
import main.java.com.meridian.utilities.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * Creating the Lambda function for handling News Wizard Alexa Skill requests:
 *
 * <ul>
 * <li><b>News RSS Feed</b>: communicate with an external RSS Feed to get News </li>
 * <li><b>Pagination</b>: after obtaining a list of events, read a small subset of events (5) and wait
 * for user prompt to read the next subset of news by maintaining session state</li>
 * <p>
 * <li><b>Dialog and Session state</b>: Handles two models, both a one-shot ask and tell model, and
 * a multi-turn dialog model</li>
 * <li><b>SSML</b>: Using SSML tags to control how Alexa renders the text-to-speech</li>
 * </ul>
 * <p>
 * <h2>Examples</h2>
 * <p>
 * <b>One-shot model</b>
 * <p>
 * User: "Alexa, ask meridian for latest news."
 * <p>
 * Alexa: "The real estate in NYC is on a new boom since 2010, [...]. Do you want to hear more?"
 * <p>
 * User: "yes."
 * <p>
 * Alexa: "The real estate in Seattle is on a new boom since 2010, [...]. Do you want to hear more?"
 * <p>
 * User: "no."
 * <p>
 * Alexa: "Good bye!"
 * <p>
 *
 * <b>Dialog model</b>
 * <p>
 * User: "Alexa, open meridian"
 * <p>
 * Alexa: "Welcome to meridian news. I can get latest real estate news that you care for. If you would like me to read your news say Read News or get news?"
 * <p>
 * User: "get news"
 * <p>
 * Alexa: "Real Estate is on boom, [...] . Do you want to hear more?"
 * <p>
 * User: "Yes."
 * <p>
 * Alexa: "Real Estate is on boom, [...] . Do you want to hear more?"
 * <p>
 * User: "Next."
 * <p>
 * Alexa: "Real Estate is on boom, [...] . Do you want to hear more?"
 * <p>
 * User: "No."
 * <p>
 * Alexa: "Good bye!"
 * <p>
 */
public class NewsWizardSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(NewsWizardSpeechlet.class);
    private static PropertyReader propertyReader = PropertyReader.getPropertyReader();

    /**
     * The key to get the item from the intent.
     */
    private static final String NAME_SLOT = "intentname";

    /**
     * Constant defining number of events to be read at one time.
     */
    private final int PAGINATION_SIZE = 1;

    private final int ARTICLE_WORD_SIZE = 100;

    /**
     * Length of the delimiter between individual events.
     */
//    private static final int DELIMITER_SIZE = 2;

    /**
     * Constant defining session attribute key for the event index.
     */
    private static final String SESSION_INDEX = "index";
    private static final String SESSION_RATES_INDEX = "rates-index";

    /**
     * Constant defining session attribute key for the event text key for date of events.
     */
    private static final String SESSION_TEXT = "text";

    private static final String SESSION_RATES_TEXT = "rates";


    /**
     * Size of events from Wikipedia response.
     */
    private final int LENGTH_OF_NEWS = 20;


    /**
     * Service to send progressive response directives.
     */
    private DirectiveService directiveService;

    /**
     * Constructs an instance of {@link NewsWizardSpeechlet}.
     *
     * @param directiveService implementation of directive service
     */
    public NewsWizardSpeechlet (DirectiveService directiveService) {
        this.directiveService = directiveService;
    }


    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        SessionStartedRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any further initialization logic goes here
        // cant think of any. May be reading the RSS?
        //todo
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        LaunchRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        if (!propertyReader.isPropertyRead()) {
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + propertyReader.getFatalError() + "</speak>");
        }

        log.debug(" Launch Action " );

        return getWelcomeResponse();

    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        log.info("onIntent requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());

        String intentName = requestEnvelope.getRequest().getIntent().getName();
        log.debug("Intent name is : - " + intentName);

        if ("GetNewsEventIntent".equals(intentName)) {
            return handleGetNewsEventRequest(requestEnvelope);
        } else if ("GetRatesEventIntent".equals(intentName)) {
            return handleGetRatesEventRequest(requestEnvelope);
        } else if ("GetNextNewsEventIntent".equals(intentName)) {
            return handleNextNewsEventRequest(requestEnvelope.getSession());
        } else if ("GetNextRatesEventIntent".equals(intentName)) {
            return handleNextRatesEventRequest(requestEnvelope.getSession());
        } else if ("GetRateForIntent".equals(intentName)) {
            return handleRateForIntentEventRequest(requestEnvelope.getRequest().getIntent(), requestEnvelope.getSession());
        } else if ("Birth".equals(intentName)) {
            return handleBirthEventRequest(requestEnvelope);
        } else if ("AMAZON.HelpIntent".equals(intentName)) {
            return  newAskResponse(propertyReader.getSpeechHelp(), false, "", false);
        } else if ("AMAZON.StopIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText(propertyReader.getGoodBye());
            log.debug(propertyReader.getGoodBye());
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else if ("AMAZON.CancelIntent".equals(intentName)) {
            PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
            outputSpeech.setText(propertyReader.getGoodBye());
            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            String outputSpeech = propertyReader.getSpeechSorry();
            String repromptText = propertyReader.getSpeechReprompt();
            log.debug(propertyReader.getSpeechSorry());
            log.debug(propertyReader.getSpeechReprompt());

            return newAskResponse(outputSpeech, true, repromptText, true);
        }
    }

    private SpeechletResponse handleGetRatesEventRequest (SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        SystemState systemState = getSystemState(requestEnvelope.getContext());
        String apiEndpoint = systemState.getApiEndpoint();
        // Dispatch a progressive response to engage the user while fetching events
        dispatchProgressiveResponse(request.getRequestId(), "Rates may be delayed by 20 minutes", systemState, apiEndpoint);
        String speechOutput = "";

        ArrayList<String> events = null;
        try {
            events = getJsonEventsFromRatesFeed();
        } catch (FileReaderException e) {
            speechOutput =
                    "There is a problem connecting to the Rates Feed at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
            log.debug("Say --- " + speechOutput);

            return SpeechletResponse.newTellResponse(outputSpeech);
        }
        if (events.isEmpty()) {
            speechOutput =
                    "There is a problem connecting to the Rates Feed at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
//            speechOutputBuilder.append("Latest News");
            StringBuilder cardOutputBuilder = new StringBuilder();
//            cardOutputBuilder.append("Latest News");
            String articleBuffer = "";
            boolean splitArticle = false;
            int articleSize = 0;

            log.debug("Pagination set to :- " + PAGINATION_SIZE);

            for (int i = 0; i < PAGINATION_SIZE; i++) {
                splitArticle = false;
                articleSize = 0;
                articleBuffer = events.get(i);
                if (articleBuffer != null && !articleBuffer.isEmpty()) {
                    articleSize = articleBuffer.length();
                    if (articleSize > ARTICLE_WORD_SIZE) {
                        splitArticle = true;
                        //Read Split Article
                    }
                    speechOutputBuilder.append("<p>");
                    speechOutputBuilder.append(events.get(i));
                    if (i < PAGINATION_SIZE - 1) {
                        speechOutputBuilder.append("Next Rate");
                    }
                    speechOutputBuilder.append("</p> ");

                    cardOutputBuilder.append(events.get(i));
                    if (i < PAGINATION_SIZE - 1) {
                        cardOutputBuilder.append("Next rate");
                    }
                    cardOutputBuilder.append("\n");
                    //todo say next news. Create some division between news
                }
            }

            log.debug("After Pagination, cardOutputBuilder set to :- " + cardOutputBuilder);

            speechOutputBuilder.append("Say Next Rate if you would like to hear more rates?");
            cardOutputBuilder.append("Say Next Rate if you would like to hear more rates?");
            speechOutput = speechOutputBuilder.toString();

            String repromptText = "I can get the latest real estate rates that you care for. " +
                    "If you would like me to read the latest real estate rates say Read Rates or get rates?" +
                    "You could also say Prime, swap, LYEBER or Treasury";

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("meridian rates");
            card.setContent(cardOutputBuilder.toString());

            // After reading the first 3 events, set the count to 3 and add the events
            // to the session attributes
            session.setAttribute(SESSION_RATES_INDEX, PAGINATION_SIZE);
            session.setAttribute(SESSION_RATES_TEXT, events);

            log.debug("ABOUT to call back for the dynamic response");

            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
            response.setCard(card);
            return response;
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        SessionEndedRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        // any session cleanup logic would go here
    }

    /**
     * Function to handle the onLaunch skill behavior.
     *
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse getWelcomeResponse() {
        String speechOutput = propertyReader.getWelcomeMessage();
        // If the user either does not reply to the welcome message or says something that is not
        // understood, they will be prompted again with this text.
        String repromptText = "If you would like me to read your news say Read News or get news. " +
                "If you would like me to read the latest real estate rates say Read Rates or get rates? " +
                "You could also say Prime, swap, LYEBER or Treasury";

        return newAskResponse(speechOutput, false, repromptText, false);
    }



    /**
     *
     * @param requestEnvelope
     *            the intent request envelope to handle
     * @return SpeechletResponse object with voice/card response to return to the user
     */

    private SpeechletResponse handleBirthEventRequest(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        SystemState systemState = getSystemState(requestEnvelope.getContext());
        String apiEndpoint = systemState.getApiEndpoint();
        String speechOutput = "I was launched on May 5th 2018 by Malhotra Consulting and Cosmos Communications. " +
                "If you would like me to read your news say Read News or get news. You could also say Prime, swap, LYEBER or Treasury";

        String repromptText = "If you would like me to read your news say Read News or get news. " +
                "If you would like me to read the latest real estate rates say Read Rates or get rates? " +
                "You could also say Prime, swap, LYEBER or Treasury";

        return newAskResponse(speechOutput, false, repromptText, false);
    }


    /**
     *
     * @param requestEnvelope
     *            the intent request envelope to handle
     * @return SpeechletResponse object with voice/card response to return to the user
     */

    private SpeechletResponse handleGetNewsEventRequest (SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {

        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();
        SystemState systemState = getSystemState(requestEnvelope.getContext());
        String apiEndpoint = systemState.getApiEndpoint();
        // Dispatch a progressive response to engage the user while fetching events
        dispatchProgressiveResponse(request.getRequestId(), "Getting you the news", systemState, apiEndpoint);
        String speechOutput = "";

        ArrayList<String> events = null;
        try {
            events = getJsonEventsFromNewsFeed();
        } catch (FileReaderException e) {
            speechOutput =
                    "There is a problem connecting to the News Feed at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
            log.debug("Say --- " + speechOutput);

            return SpeechletResponse.newTellResponse(outputSpeech);
        }
        if (events.isEmpty()) {
            speechOutput =
                    "There is a problem connecting to the News Feed at this time."
                            + " Please try again later.";

            // Create the plain text output
            SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
            outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

            return SpeechletResponse.newTellResponse(outputSpeech);
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
//            speechOutputBuilder.append("Latest News");
            StringBuilder cardOutputBuilder = new StringBuilder();
//            cardOutputBuilder.append("Latest News");
            String articleBuffer = "";
            boolean splitArticle = false;
            int articleSize = 0;

            log.debug("Pagination set to :- " + PAGINATION_SIZE);

            for (int i = 0; i < PAGINATION_SIZE; i++) {
                splitArticle = false;
                articleSize = 0;
                articleBuffer = events.get(i);
                if (articleBuffer != null && !articleBuffer.isEmpty()) {
                    articleSize = articleBuffer.length();
                    if (articleSize>ARTICLE_WORD_SIZE) {
                        splitArticle = true;
                        //Read Split Article
                    }
                    speechOutputBuilder.append("<p>");
                    speechOutputBuilder.append(events.get(i));
                    if (i<PAGINATION_SIZE-1) {
                        speechOutputBuilder.append("Next news");
                    }
                    speechOutputBuilder.append("</p> ");

                    cardOutputBuilder.append(events.get(i));
                    if (i<PAGINATION_SIZE-1) {
                        cardOutputBuilder.append("Next news");
                    }
                    cardOutputBuilder.append("\n");
                    //todo say next news. Create some division between news
                }
            }

            log.debug("After Pagination, cardOutputBuilder set to :- " + cardOutputBuilder);

            speechOutputBuilder.append("Do you want hear more?");
            cardOutputBuilder.append(" Do you want hear more?");
            speechOutput = speechOutputBuilder.toString();

            String repromptText = "If you would like me to read your news say Read News or get news. " +
                    "If you would like me to read the latest real estate rates say Read Rates or get rates? " +
                    "You could also say Prime, swap, LYEBER or Treasury";

            // Create the Simple card content.
            SimpleCard card = new SimpleCard();
            card.setTitle("meridian news");
            card.setContent(cardOutputBuilder.toString());

            // After reading the first 3 events, set the count to 3 and add the events
            // to the session attributes
            session.setAttribute(SESSION_INDEX, PAGINATION_SIZE);
            session.setAttribute(SESSION_TEXT, events);

            log.debug("ABOUT to call back for the dynamic response");

            SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
            response.setCard(card);
            return response;
        }
    }

    /**
     * Prepares the speech to reply to the user. Obtains the list of events as well as the current
     * index from the session attributes. After getting the next set of events, increment the index
     * and store it back in session attributes. This allows us to obtain new events without making
     * repeated network calls, by storing values (events, index) during the interaction with the
     * user.
     *
     * @param session
     *            object containing session attributes with events list and index
     * @return SpeechletResponse object with voice/card response to return to the user
     */
    private SpeechletResponse handleNextNewsEventRequest (Session session) {

        log.debug("in the next event request");

        String cardTitle = "meridian news";
        ArrayList<String> events = (ArrayList<String>) session.getAttribute(SESSION_TEXT);
        int index = (Integer) session.getAttribute(SESSION_INDEX);
        String speechOutput = "";
        String cardOutput = "";
        if (events == null) {
            speechOutput =
                    "If you would like me to read your news say Read News or get news. " +
                            "If you would like me to read the latest real estate rates say Read Rates or get rates? " +
                            "You could also say Prime, swap, LYEBER or Treasury";
        } else if (index >= events.size()) {
            speechOutput =
                    "There are no more news articles for today.";
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            StringBuilder cardOutputBuilder = new StringBuilder();
            for (int i = 0; i < PAGINATION_SIZE && index < events.size(); i++) {
                speechOutputBuilder.append("<p>");
                speechOutputBuilder.append(events.get(index));
                if (i<PAGINATION_SIZE-1) {
                    speechOutputBuilder.append("Next news");
                }
                speechOutputBuilder.append("</p> ");
                cardOutputBuilder.append(events.get(index));
                if (i<PAGINATION_SIZE-1) {
                    cardOutputBuilder.append("Next news");
                }
                cardOutputBuilder.append(" ");
                index++;
            }
            if (index < events.size()) {
                speechOutputBuilder.append("Do you want hear more?");
                cardOutputBuilder.append("Do you want hear more?");
            }
            session.setAttribute(SESSION_INDEX, index);
            speechOutput = speechOutputBuilder.toString();
            cardOutput = cardOutputBuilder.toString();
        }
        String repromptText = "Do you want hear more?";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(cardTitle);
        card.setContent(cardOutput.toString());

        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        response.setCard(card);
        return response;
    }

    /**
     * Download JSON-formatted list of events from Wikipedia, for a defined day/date, and return a
     * String array of the events, with each event representing an element in the array.
     *
     * @return String array of events for that date, 1 event per element of the array
     */

    private ArrayList<String> getJsonEventsFromNewsFeed() throws FileReaderException {

        ArrayList messages = new ArrayList();
        NewsRSSFeedParser parser = new NewsRSSFeedParser(propertyReader.getNewsFeedUrl());
        NewsFeed newsFeed = parser.readFeed();

        for (NewsFeedMessage message : newsFeed.getMessages()) {
            log.debug(" Populating the feed messages in the list :" + message.getEncodedContent());
            messages.add(message.getEncodedContent());
        }

        return messages;

    }

    private SpeechletResponse handleRateForIntentEventRequest (Intent intent, Session session) {
        log.debug("in the handleRateForIntentEventRequest request");
        String repromptText = null;
        SimpleCard card = new SimpleCard();
        String cardTitle = "meridian rates";
        Slot nameSlot = intent.getSlot(NAME_SLOT);
        log.debug("The SLOT name is -: " + nameSlot.getValue());

        ArrayList<String> events = (ArrayList<String>) session.getAttribute(SESSION_RATES_TEXT);
        String speechOutput = "";
        String cardOutput = "";
        if (events == null) {
            try {
                events = getJsonEventsFromRatesFeed();
            } catch (FileReaderException e) {
                speechOutput =
                        "There is a problem connecting to the Rates Feed at this time."
                                + " Please try again later.";

                // Create the plain text output
                SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
                outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
                log.debug("Say --- " + speechOutput);

                return SpeechletResponse.newTellResponse(outputSpeech);
            }
        }

            StringBuilder speechOutputBuilder = new StringBuilder();
//            speechOutputBuilder.append("Latest News");
            StringBuilder cardOutputBuilder = new StringBuilder();
//            cardOutputBuilder.append("Latest News");
            String tempRateMessage = null;
            String tempName = nameSlot.getValue();

            for (int i = 0; i < events.size(); i++) {
                tempRateMessage = events.get(i);

                log.debug("While iterating on rate messages, trying to find the right rate message - " + tempRateMessage);
                log.debug("While iterating on rate messages, looking for - " + tempName);
                if (tempName.equalsIgnoreCase("LYEBER")) {
                    tempName = "LIBOR";
                }

                if (tempRateMessage.toLowerCase().contains(nameSlot.getValue().toLowerCase())) {
                    log.debug("For the slot chosen the message is : " + tempRateMessage);
                    speechOutputBuilder.append(tempRateMessage);
                    speechOutputBuilder.append("<break time=\"1s\"/>" );
                }
            }

            if (tempRateMessage == null) {
                //handle error message. No rate by that name.
                speechOutputBuilder.append("Sorry i could not find any rate by the name of" +
                        nameSlot.getValue() + "I can get the latest real estate rates that you care for. " +
                        "If you would like me to read the latest real estate rates say Read Rates or get rates?" +
                        "You could also say Prime, swap, LYEBER or Treasury");
            }

            speechOutputBuilder.append("If you would like me to read any other rate, you can say Swap, Prime, LYEBER or Treasury");
            cardOutputBuilder.append("If you would like me to read any other rate, you can say Swap, Prime, LYEBER or Treasury");
            speechOutput = speechOutputBuilder.toString();

            repromptText = "I can get the latest real estate rates that you care for. " +
                    "If you would like me to read the latest real estate rates say Read Rates or get rates?" +
                    "You could also say Prime, swap, LYEBER or Treasury";

        // Create the Simple card content.

            card.setTitle("meridian rates");
            card.setContent(cardOutputBuilder.toString());

            // After reading the first 3 events, set the count to 3 and add the events
            // to the session attributes
            session.setAttribute(SESSION_INDEX, PAGINATION_SIZE);
            session.setAttribute(SESSION_RATES_TEXT, events);

            log.debug("ABOUT to call back for the dynamic response");


        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        response.setCard(card);
        return response;
    }

/*
        ArrayList<RatesFeedMessage> rates = (ArrayList) session.getAttribute("FEED-MESSAGE-SESSION");
        if (null == rates) {
            try {
                rates = readFeedAndBuildSlotList();
            } catch (FileReaderException e) {
                String speechOutput =
                        "There is a problem connecting to the Rates Feed at this time."
                                + " Please try again later.";

                // Create the plain text output
                SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
                outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");
                log.debug("Say --- " + speechOutput);

                return SpeechletResponse.newTellResponse(outputSpeech);
            }
            if (rates.isEmpty()) {
                String speechOutput =
                        "There is a problem connecting to the Rates Feed at this time."
                                + " Please try again later.";

                // Create the plain text output
                SsmlOutputSpeech outputSpeech = new SsmlOutputSpeech();
                outputSpeech.setSsml("<speak>" + speechOutput + "</speak>");

                return SpeechletResponse.newTellResponse(outputSpeech);
            } else {
                session.setAttribute("FEED-MESSAGE-SESSION", rates);
            }
        }

        log.debug("Read and populated the FeedMessages in the session object");
        if (nameSlot != null && nameSlot.getValue() != null) {
            String slotName = nameSlot.getValue();
            String rateString = getRelevantRates(rates, slotName);
            //String rateString = Rates.get(slotName);
            if (rateString != null) {
                // If we have the recipe, return it to the user.
                PlainTextOutputSpeech outputSpeech = new PlainTextOutputSpeech();
                outputSpeech.setText(rateString);

                SimpleCard card = new SimpleCard();
                card.setTitle("Recipe for " + slotName);
                card.setContent(rateString);

                //return SpeechletResponse.newTellResponse(outputSpeech, card);
                String repromptText = "Do you want hear more?";
                log.debug("Just before setting the response" + rateString);
                SpeechletResponse response = newAskResponse("<speak>" + rateString + "</speak>", true, repromptText, false);
                response.setCard(card);
                return response;
            } else {
                String speechOutput = propertyReader.getSpeechHelp();

                String repromptText = propertyReader.getSpeechReprompt();

                return newAskResponse(propertyReader.getSpeechHelp(), false, "", false);
            }
        } else {
            // There was no item in the intent so return the help prompt.
            String speechOutput = propertyReader.getSpeechHelp();

            String repromptText = propertyReader.getSpeechReprompt();

            return newAskResponse(propertyReader.getSpeechHelp(), false, "", false);        }




    private String getRelevantRates(ArrayList rates, String slotName) {
//        ArrayList rateList = new ArrayList();
        String currentName;
        RatesFeedMessage message;
        StringBuilder builder = new StringBuilder();
        String returnMessage = "";

        for (int i=0; i<rates.size(); i++) {
            message = (RatesFeedMessage) rates.get(i);
            currentName = message.getName();
            log.debug("in getRelevantRates - currentNamne" + currentName);
            log.debug("in getRelevantRates - sloName" + slotName);

            if (currentName.toLowerCase().startsWith(slotName.toLowerCase())) {
                builder.append(message.getDataToBeRead());
                builder.append("         ");

                log.debug("Added a message for " + slotName);
                log.debug("message name added" + message.getName());
                log.debug("message that Alexa will read " + builder.toString());
            }
        }
        if (builder.toString().isEmpty()) {
            returnMessage = "I was not able to find the rate by that name. " +
                    "If you would like me to read the latest real estate rates say Read Rates or get rates?" ;
        } else {
            returnMessage = builder.toString();
        }
        return returnMessage;
    }
*/
    private SpeechletResponse handleNextRatesEventRequest(Session session) {

        log.debug("in the next event request");

        String cardTitle = "meridian rates";
        ArrayList<String> events = (ArrayList<String>) session.getAttribute(SESSION_RATES_TEXT);
        int index = (Integer) session.getAttribute(SESSION_RATES_INDEX);
        String speechOutput = "";
        String cardOutput = "";
        if (events == null) {
            speechOutput =
                    "I can get latest real estate rates that you care for. " +
                            "If you would like me to read the latest real estate rates say Read Rates or get rates?" +
                            "You could also say Prime, swap, LYEBER or Treasury";
        } else if (index >= events.size()) {
            speechOutput =
                    "There are no more rates to share.";
        } else {
            StringBuilder speechOutputBuilder = new StringBuilder();
            StringBuilder cardOutputBuilder = new StringBuilder();
            for (int i = 0; i < PAGINATION_SIZE && index < events.size(); i++) {
                speechOutputBuilder.append("<p>");
                speechOutputBuilder.append(events.get(index));
                if (i<PAGINATION_SIZE-1) {
                    speechOutputBuilder.append("Next rate");
                }
                speechOutputBuilder.append("</p> ");
                cardOutputBuilder.append(events.get(index));
                if (i<PAGINATION_SIZE-1) {
                    cardOutputBuilder.append("Next rate");
                }
                cardOutputBuilder.append(" ");
                index++;
            }
            if (index < events.size()) {
                speechOutputBuilder.append("Say Next Rate if you would like to hear more rates");
                cardOutputBuilder.append("Say Next Rate if you would like to hear more rates");
            }
            session.setAttribute(SESSION_INDEX, index);
            speechOutput = speechOutputBuilder.toString();
            cardOutput = cardOutputBuilder.toString();
        }
        String repromptText = "Say Next Rate if you would like to hear more rates";

        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle(cardTitle);
        card.setContent(cardOutput.toString());

        SpeechletResponse response = newAskResponse("<speak>" + speechOutput + "</speak>", true, repromptText, false);
        response.setCard(card);
        return response;
    }


    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput
     *            the output to be spoken
     * @param isOutputSsml
     *            whether the output text is of type SSML
     * @param repromptText
     *            the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml
     *            whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
            String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    /**
     * Dispatches a progressive response.
     *
     * @param requestId
     *            the unique request identifier
     * @param text
     *            the text of the progressive response to send
     * @param systemState
     *            the SystemState object
     * @param apiEndpoint
     *            the Alexa API endpoint
     */
    private void dispatchProgressiveResponse(String requestId, String text, SystemState systemState, String apiEndpoint) {
        DirectiveEnvelopeHeader header = DirectiveEnvelopeHeader.builder().withRequestId(requestId).build();
        SpeakDirective directive = SpeakDirective.builder().withSpeech(text).build();
        DirectiveEnvelope directiveEnvelope = DirectiveEnvelope.builder()
                .withHeader(header).withDirective(directive).build();

        if(systemState.getApiAccessToken() != null && !systemState.getApiAccessToken().isEmpty()) {
            String token = systemState.getApiAccessToken();
            try {
                directiveService.enqueue(directiveEnvelope, apiEndpoint, token);
            } catch (Exception e) {
                log.error("FAtal error  - Failed to dispatch a progressive response", e);
            }
        }
    }

    /**
     * Helper method that retrieves the system state from the request context.
     * @param context request context.
     * @return SystemState the systemState
     */
    private SystemState getSystemState(Context context) {
        return context.getState(SystemInterface.class, SystemState.class);
    }

    /**
     * Download JSON-formatted list of events from Wikipedia, for a defined day/date, and return a
     * String array of the events, with each event representing an element in the array.
     *
     * @return String array of events for that date, 1 event per element of the array
     */

    private ArrayList<String> getJsonEventsFromRatesFeed() throws FileReaderException {

        String text = "";
        String line;
        ArrayList messages = new ArrayList();
        RatesRSSFeedParser parser = new RatesRSSFeedParser(propertyReader.getRatesFeedUrl());
        RatesFeed feed = parser.readFeed();

        String name = "";
        String rate = "";
        String date = "";
        String symbol = "";
        String month = "";
        String day = "";

        StringBuilder builder = null;
        for (RatesFeedMessage message : feed.getMessages()) {
            builder = new StringBuilder();
            name = message.getName();
            rate = message.getValue();
            symbol = message.getSymbol();

            builder.append("As of ");

            date = message.getQuoteDate();
            month = date.substring(0, date.indexOf("/"));
            day = date.substring(date.indexOf("/")+1, date.lastIndexOf("/"));

            builder.append(month);
            builder.append(" ");
            builder.append(day);


            builder.append(" the ");
            if (!message.getName().toLowerCase().endsWith("rate")) {
                builder.append(message.getName() + " rate ");
            } else {
                builder.append(message.getName());
            }
            builder.append(" is ");
            builder.append(message.getValue());
            log.debug("JSON Message builder " + builder.toString());

            messages.add(builder.toString());

        }

        return messages;

    }
/*
    private ArrayList<RatesFeedMessage> readFeedAndBuildSlotList() throws FileReaderException {

        String text = "";
        String line;
        ArrayList feedMessages = new ArrayList();
        RatesRSSFeedParser parser = new RatesRSSFeedParser(propertyReader.getRatesFeedUrl());
        RatesFeed feed = parser.readFeed();

        String name = "";
        String rate = "";
        String date = "";
        String symbol = "";
        String month = "";
        String day = "";

        StringBuilder builder = null;
        for (RatesFeedMessage message : feed.getMessages()) {
            builder = new StringBuilder();
            name = message.getName();
            rate = message.getValue();
            symbol = message.getSymbol();

            builder.append("As of ");

            date = message.getQuoteDate();
            month = date.substring(0, date.indexOf("/"));
            day = date.substring(date.indexOf("/")+1, date.lastIndexOf("/"));

            builder.append(month);
            builder.append(" ");
            builder.append(day);


            builder.append(" the ");
            if (!message.getName().toLowerCase().endsWith("rate")) {
                builder.append(message.getName() + " rate ");
            } else {
                builder.append(message.getName());
            }
            builder.append(" is ");
            builder.append(message.getValue());
            log.debug("JSON Message builder " + builder.toString());

            message.setDataToBeRead(builder.toString());
            feedMessages.add(message);
        }
        return feedMessages;

    }
*/
}
