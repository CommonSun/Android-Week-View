package com.alamkanak.weekview.sample;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import de.greenrobot.event.EventBus;

/**
 * This is a base activity which contains week view and all the codes necessary to initialize the
 * week view.
 * Created by Raquib-ul-Alam Kanak on 1/3/2014.
 * Website: http://alamkanak.github.io
 */
public abstract class BaseActivity extends AppCompatActivity implements WeekView.EventClickListener,
        MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener,
        WeekView.EmptyViewClickListener {
    protected static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 101;
    protected static final String AVAILABLE_STRING = "Available";
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    int generator = 0;

    Toolbar toolbar;

    // Populate the week view with some events.
    List<WeekViewEvent> mEvents = new ArrayList<WeekViewEvent>();
    List<WeekViewEvent> mAvailableEvents = new ArrayList<WeekViewEvent>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);


        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.weekView);

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        // Set click for empty view
        mWeekView.setEmptyViewClickListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(false);

        mWeekViewType = TYPE_DAY_VIEW;
        mWeekView.setNumberOfVisibleDays(1);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_day_view).setChecked(true);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.action_week_view);
        switch (id){
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
            case R.id.action_done:
                onDone();
                return true;
            case R.id.action_day_view:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(1);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_three_day_view:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    mWeekView.setNumberOfVisibleDays(3);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                }
                return true;
            case R.id.action_week_view:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    mWeekView.setNumberOfVisibleDays(7);

                    // Lets change some dimensions to best fit the view.
                    mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                    mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        if (event.getGeneratedId() != null) {
            //Toast.makeText(this, "Clicked " + event.getGeneratedId(), Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
        }
        if (event.getName().equalsIgnoreCase(AVAILABLE_STRING)) {
            removeAvailableEvent(event);
        }
    }

    protected void removeAvailableEvent(WeekViewEvent event) {
        int toRemove = -1;
        int i = 0;
        for (WeekViewEvent weekViewEvent : mAvailableEvents ) {
            if (weekViewEvent.getGeneratedId().equalsIgnoreCase(event.getGeneratedId())) {
                toRemove = i;
                break;
            }
            i++;
        }

        if (toRemove != -1) {
            mAvailableEvents.remove(toRemove);
            mWeekView.setAvailableEvents(mAvailableEvents, true);
            //Toast.makeText(this, "Removing event", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        Toast.makeText(this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
    }

    public String generateUuid(){
        return UUID.randomUUID().toString();
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        //Toast.makeText(this, "Empty view clicked: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
        Calendar startTime = (Calendar)time.clone();
        startTime.set(Calendar.MINUTE, time.getActualMinimum(Calendar.MINUTE));
        Calendar endTime = (Calendar)time.clone();
        endTime.set(Calendar.MINUTE, time.getActualMaximum(Calendar.MINUTE));
        WeekViewEvent event = new WeekViewEvent(generateUuid(), AVAILABLE_STRING, startTime, endTime);
        event.setColor(getResources().getColor(R.color.event_color_03));
        mAvailableEvents.add(event);
        mWeekView.setAvailableEvents(mAvailableEvents, false);
        //Toast.makeText(this, "Created " + event.getGeneratedId(), Toast.LENGTH_SHORT).show();
        //mWeekView.notifyDatasetChanged(true);
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
    }

    public WeekView getWeekView() {
        return mWeekView;
    }

    private void onDone() {
        SelectedAvailabilityTimeSlotsEvent event = new SelectedAvailabilityTimeSlotsEvent(mAvailableEvents);
        EventBus.getDefault().postSticky(event);
        finish();
    }

    protected void checkPermission(){
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALENDAR)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        MY_PERMISSIONS_REQUEST_READ_CALENDAR);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
