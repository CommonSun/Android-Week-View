package com.alamkanak.weekview.sample;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by BX on 3/12/2016.
 */
public class CalendarEvent {
    public long eventId;
    public long calendarId;
    public long startDate, endDate;
    public String rrule;
    public String name;

    public CalendarEvent(long calendarId, long eventId, long start, long end, String rrule, String name) {
        this.calendarId = calendarId;
        this.eventId = eventId;
        this.startDate = start;
        this.endDate = end;
        this.rrule = rrule;
        this.name = name;
    }

    public String timestampToHumanDate(long timestamp) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);

            SimpleDateFormat dateFormat;
            dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(eventId);
        sb.append(", start:");
        sb.append(timestampToHumanDate(startDate));
        sb.append(", end:");
        sb.append(timestampToHumanDate(endDate));
        sb.append(", rule:");
        sb.append(rrule);
        sb.append(", name:");
        sb.append(name);
        return sb.toString();
    }

}
