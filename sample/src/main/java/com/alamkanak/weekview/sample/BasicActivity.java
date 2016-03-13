package com.alamkanak.weekview.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A basic example of how to use week view library.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
public class BasicActivity extends BaseActivity {

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {

        // Assume thisActivity is the current activity
        checkPermission();
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR);

        List<CalendarEvent> allEvents = null;
        if (permissionCheck == android.content.pm.PackageManager.PERMISSION_GRANTED) {

            // if we wanna get only specific Account calendar events
            //CalendarContentResolver calendarContentResolver = new CalendarContentResolver(this);
            //Set<String> calendarSet = calendarContentResolver.getCalendars();

            CalendarEventsContentResolver calendarEventsContentResolver = new CalendarEventsContentResolver(this);

            long thisMonthStart = getFirstDateOfMonthAndYear(newMonth, newYear);
            long thisMonthEnd = getLastDateOfMonthAndYear(newMonth, newYear);
            allEvents = calendarEventsContentResolver.getCalendarEvents(1, thisMonthStart, thisMonthEnd);
        }

        int i = 0;
        if (allEvents != null) {
            for (CalendarEvent calendarEvent : allEvents) {
                Calendar startTime = Calendar.getInstance();
                startTime.setTimeInMillis(calendarEvent.startDate);
                Calendar endTime = Calendar.getInstance();
                endTime.setTimeInMillis(calendarEvent.endDate);
                WeekViewEvent event = new WeekViewEvent(1, calendarEvent.name, startTime, endTime);
                event.setColor(getResources().getColor(R.color.event_color_01));
                mEvents.add(event);
                Log.d(String.valueOf(i), calendarEvent.toString());
                i++;
            }
        } else {
            // we do not read phone events, but allow selection of event dates
            /*
            Calendar startTime = Calendar.getInstance();
            startTime.set(Calendar.HOUR_OF_DAY, 3);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.MONTH, newMonth - 1);
            startTime.set(Calendar.YEAR, newYear);
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.HOUR, 1);
            endTime.set(Calendar.MONTH, newMonth - 1);
            WeekViewEvent event = new WeekViewEvent(1, getEventTitle(startTime), startTime, endTime);
            event.setColor(getResources().getColor(R.color.event_color_01));
            events.add(event);

            ....

            */
        }

        return mEvents;
    }

    private long getFirstDateOfMonthAndYear(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

    private long getLastDateOfMonthAndYear(int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTimeInMillis();
    }

}
