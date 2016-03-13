package com.alamkanak.weekview.sample;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class CalendarEventsContentResolver {
    private static final String[] FIELDS = new String[] {
            //CalendarContract.Events._ID,
            //CalendarContract.Events.DTSTART,
            //CalendarContract.Events.DTEND,
            //CalendarContract.Events.RRULE,
            CalendarContract.Events.TITLE
    };
    public static final Uri EVENTS_URI = CalendarContract.Events.CONTENT_URI;
    private static final String[] INSTANCE_FIELDS = new String[] {
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_ID
    };

    ContentResolver contentResolver;

    public CalendarEventsContentResolver(Context ctx) {
        contentResolver = ctx.getContentResolver();
    }

    public String timestampToHumanDate(long timestamp) {
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(timestamp);

            SimpleDateFormat dateFormat;
            dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            return dateFormat.format(cal.getTime());
        } catch (NumberFormatException nfe) {
            return String.valueOf(timestamp);
        }
    }

    public List<CalendarEvent> getCalendarEvents(long calendarId, Calendar from, Calendar to) {
        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, from.getTimeInMillis());
        ContentUris.appendId(eventsUriBuilder, to.getTimeInMillis());
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor = null;
        cursor = contentResolver.
                query(eventsUri,
                        INSTANCE_FIELDS,
                        null,
                        null,
                        CalendarContract.Instances.DTSTART + " ASC");
        List<CalendarEvent> toReturn = new ArrayList<>();
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    long begin = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.BEGIN));
                    long end = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.END));
                    long eventId = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.EVENT_ID));
                    CalendarEvent calendarEvent = new CalendarEvent(0l, eventId, begin, end, "", "");
                    toReturn.add(calendarEvent);
                    //Log.d("Instance event", String.valueOf(eventId) + ", " + timestampToHumanDate(begin) + ", " + timestampToHumanDate(end));
                }
            }
        } catch (AssertionError ex) {
            Log.e("", "Exception " + ex.toString());
        }
        cursor.close();

        for (CalendarEvent event : toReturn) {
            String FILTER = CalendarContract.Events.VISIBLE + " = 1"
                    + " AND " + CalendarContract.Events._ID + "=" + event.eventId;
                    //+ " AND " +
                    //CalendarContract.Events.DTSTART + " > " + from.getTimeInMillis(); //+
                    //" AND " + CalendarContract.Events.DTSTART + " < " + to.getTimeInMillis() + ")";
                    //Log.d("from", timestampToHumanDate(from.getTimeInMillis()));
                    //Log.d("to", timestampToHumanDate(to.getTimeInMillis()));
                    cursor =
                        contentResolver.
                                query(EVENTS_URI,
                                        FIELDS,
                                        FILTER,
                                        null,
                                        null);

            try {
                if (cursor.getCount() > 0) {
                    cursor.moveToFirst();
                    event.name = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
                    /*
                    long id = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events._ID));
                    long start = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
                    String rrule = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.RRULE));
                    long end;
                    if (rrule == null)
                        end = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));
                    else {
                        String rules[] = rrule.split(";");
                        String until = rules[1].split("=")[1];
                        end = start + cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));
                    }
                    String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
                    CalendarEvent calendarEvent = new CalendarEvent(calendarId, id, start, end, rrule, title);
                    toReturn.add(calendarEvent);
                    */
                }
            } catch (AssertionError ex) {
                Log.e("", "Exception " + ex.toString());
            }
            cursor.close();
        }

        return toReturn;
    }
}
