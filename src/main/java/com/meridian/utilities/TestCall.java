package main.java.com.meridian.utilities;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;

import java.net.URI;
import java.net.URISyntaxException;

public class TestCall {
    // Find your Account Sid and Token at twilio.com/console
    public static final String ACCOUNT_SID = "ACff230bb14d5da16062cf9e8c433fa1fb";
    public static final String AUTH_TOKEN = "9fcd8ab1ce3892292fcc2c028d88054d";

    public static void main(String[] args) throws URISyntaxException {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        String from = "+14139236428 ";
        String to = "+14133184527";


        Call call = Call.creator(new PhoneNumber(to), new PhoneNumber(from),
                new URI("http://demo.twilio.com/docs/voice.xml")).create();


        System.out.println(call.getSid());
    }
}




