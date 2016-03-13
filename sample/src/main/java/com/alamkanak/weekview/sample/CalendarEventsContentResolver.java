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

    public List<CalendarEvent> getCalendarEvents(long calendarId, long timeStampFrom, long timeStampTo) {
        Log.d("time", "from "+ timestampToHumanDate(timeStampFrom));
        Log.d("time", "to   "+ timestampToHumanDate(timeStampTo));


        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, timeStampFrom);
        ContentUris.appendId(eventsUriBuilder, timeStampTo);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cursor;
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
                }
            }
        } catch (AssertionError ex) {
            Log.e("", "Exception " + ex.toString());
        }
        cursor.close();

        for (CalendarEvent event : toReturn) {
            String FILTER = CalendarContract.Events.VISIBLE + " = 1"
                    + " AND " + CalendarContract.Events._ID + "=" + event.eventId;
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
                }
            } catch (AssertionError ex) {
                Log.e("", "Exception " + ex.toString());
            }
            cursor.close();
        }

        return toReturn;
    }
}
