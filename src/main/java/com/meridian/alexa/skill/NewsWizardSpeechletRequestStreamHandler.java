/**
 Copyright  */

package main.java.com.meridian.alexa.skill;

import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.lambda.SpeechletRequestStreamHandler;
import com.amazon.speech.speechlet.services.DirectiveServiceClient;
import main.java.com.meridian.utilities.PropertyReader;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This class could be the handler for an AWS Lambda function powering an Alexa Skills Kit
 * experience. To do this, simply set the handler field in the AWS Lambda console to
 * "historybuff.HistoryBuffSpeechletRequestStreamHandler" For this to work, you'll also need to
 * build this project using the {@code lambda-compile} Ant task and upload the resulting zip file to
 * power your function.
 */
public class NewsWizardSpeechletRequestStreamHandler extends SpeechletRequestStreamHandler {

    private static final Set<String> supportedApplicationIds;
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(NewsWizardSpeechlet.class);


    private static PropertyReader propertyReader = PropertyReader.getPropertyReader();


    static {
        /*
         * This is the ID for the Alexa Skill
         */
        supportedApplicationIds = new HashSet<String>();
        //TODO replace with the real Alexa ID
        supportedApplicationIds.add(propertyReader.getSkillId());
    }

    public NewsWizardSpeechletRequestStreamHandler () {
        super(new NewsWizardSpeechlet(new DirectiveServiceClient()), supportedApplicationIds);
        log.debug("Test in NewsWizardSpeechletRequestStreamHandler - 1");
    }

    public NewsWizardSpeechletRequestStreamHandler (Speechlet speechlet,
                                                    Set<String> supportedApplicationIds) {
        super(speechlet, supportedApplicationIds);
        log.debug("Test in NewsWizardSpeechletRequestStreamHandler - 2");

    }

}
