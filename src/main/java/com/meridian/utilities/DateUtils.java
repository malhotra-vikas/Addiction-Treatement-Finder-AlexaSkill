package main.java.com.meridian.utilities;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    /**
     * Constant defining session attribute key for the intent slot key for the date of events.
     */
    private static final String SLOT_DAY = "day";

    /**
     * Function to accept an intent containing a Day slot (date object) and return the Calendar
     * representation of that slot value. If the user provides a date, then use that, otherwise use
     * today. The date is in server time, not in the user's time zone. So "today" for the user may
     * actually be tomorrow.
     *
     * @param intent
     *            the intent object containing the day slot
     * @return the Calendar representation of that date
     */


    private Calendar getCalendar(Intent intent) {
        Slot daySlot = intent.getSlot(SLOT_DAY);
        Date date;
        Calendar calendar = Calendar.getInstance();
        if (daySlot != null && daySlot.getValue() != null) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-d");
            try {
                date = dateFormat.parse(daySlot.getValue());
            } catch (ParseException e) {
                date = new Date();
            }
        } else {
            date = new Date();
        }
        calendar.setTime(date);
        return calendar;
    }

}
