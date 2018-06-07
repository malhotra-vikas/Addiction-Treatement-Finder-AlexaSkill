package main.java.com.meridian.utilities.main.java.com.meridian.utilities.ctc;

import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.rest.api.v2010.account.CallCreator;
import com.twilio.type.PhoneNumber;

import java.net.URISyntaxException;
import java.net.URI;

public class TestCall {
    public static void main(String[] args) throws URISyntaxException {
        String TWILIO_ACCOUNT_SID= "ACff230bb14d5da16062cf9e8c433fa1fb";
        String AUTH = "9fcd8ab1ce3892292fcc2c028d88054d";

        Twilio.init(TWILIO_ACCOUNT_SID, AUTH);
        String from = "19149537025";
        String to = "+14133184527";

        public TwilioRestClient twilioRestClient = new TwilioRestClient().

        public TwilioRestClient twilioRestClient(@Value("${TWILIO_ACCOUNT_SID}") String accountSid,
                @Value("${TWILIO_AUTH_TOKEN}") String authToken){
            return new TwilioRestClient.Builder(accountSid, authToken).build();

        Call call = Call.creator(new PhoneNumber(to), new PhoneNumber(from), new URI("https://demo.twilio.com/docs/voice.xml")).create();
        System.out.println(call.getSid());

    }

}
