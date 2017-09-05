package tcd.training.com.calendar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.LinkedHashMap;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;

public class EventDetailsActivity extends AppCompatActivity {

    public static final String ARG_CALENDAR_ENTRY = "calendarEntry";

    private LinearLayout mDateTimeLayout,
            mLocationLayout,
            mNotificationLayout,
            mGuestsLayout,
            mDescriptionLayout,
            mAccountDisplayNameLayout;
    private CalendarEvent mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeUiComponents();

        mEvent = getIntent().getParcelableExtra(ARG_CALENDAR_ENTRY);

        updateActionBar();

        showEventInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void updateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mEvent.getTitle());
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(CalendarUtils.getAccountColor(mEvent.getCalendarId())));
    }

    private void initializeUiComponents() {
        mDateTimeLayout = (LinearLayout) findViewById(R.id.ll_date_time);
        mLocationLayout = (LinearLayout) findViewById(R.id.ll_location);
        mNotificationLayout = (LinearLayout) findViewById(R.id.ll_notification);
        mGuestsLayout = (LinearLayout) findViewById(R.id.ll_guests);
        mDescriptionLayout = (LinearLayout) findViewById(R.id.ll_description);
        mAccountDisplayNameLayout = (LinearLayout) findViewById(R.id.ll_account);
    }

    private void showEventInfo() {
        displayDateTime();

        displayLocation();

        displayNotificationInfo();

        displayGuestsInfo();

        displayDescription();

        displayAccountDisplayName();
    }

    private void displayDateTime() {

        ((ImageView)mDateTimeLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_access_time_black_48dp);
        TextView dateTimeTextView = mDateTimeLayout.findViewById(R.id.tv_primary_description);

        if (CalendarUtils.getDate(mEvent.getStartDate(), "yyyy/MM/dd").equals(CalendarUtils.getDate(mEvent.getEndDate(), "yyyy/MM/dd"))) {
            String format = "EEEE, MMMM d, hh:mm a";
            String dateTime = CalendarUtils.getDate(mEvent.getStartDate(), format)
                    + " -\n"
                    + CalendarUtils.getDate(mEvent.getEndDate(), format);
            dateTimeTextView.setText(dateTime);
        } else {
            String dateTime = CalendarUtils.getDate(mEvent.getStartDate(), "EEEE, MMMM d")
                    + "\n"
                    + CalendarUtils.getDate(mEvent.getStartDate(), "hh:mm a")
                    + " - "
                    + CalendarUtils.getDate(mEvent.getEndDate(), "hh:mm a");
            dateTimeTextView.setText(dateTime);

        }
    }

    private void displayLocation() {
        if (mEvent.getLocation().length() > 0) {
            ((ImageView)mLocationLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_location_on_black_48dp);
            ((TextView)mLocationLayout.findViewById(R.id.tv_primary_description)).setText(mEvent.getLocation());
            mLocationLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayGuestsInfo() {
        // TODO: 9/1/17 implement guests
        ((ImageView)mGuestsLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_people_black_48dp);
        ((TextView)mGuestsLayout.findViewById(R.id.tv_primary_description)).setText("0");
        mGuestsLayout.setVisibility(View.VISIBLE);
    }

    private void displayNotificationInfo() {
        // TODO: 9/1/17 implement reminder
        ((ImageView)mNotificationLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_notifications_black_48dp);
        ((TextView)mNotificationLayout.findViewById(R.id.tv_primary_description)).setText("10 minutes before");
        mNotificationLayout.setVisibility(View.VISIBLE);
    }
    
    private void displayDescription() {
        if (mEvent.getDescription().length() > 0) {
            ((ImageView)mDescriptionLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_description_black_48dp);
            ((TextView)mDescriptionLayout.findViewById(R.id.tv_primary_description)).setText(mEvent.getDescription());
            mDescriptionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayAccountDisplayName() {
        String displayName = CalendarUtils.getAccountDisplayName(mEvent.getCalendarId());
        if (displayName.length() > 0) {
            ((ImageView)mAccountDisplayNameLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_action_today_black_24dp);
            ((TextView)mAccountDisplayNameLayout.findViewById(R.id.tv_primary_description)).setText(displayName);
            mAccountDisplayNameLayout.setVisibility(View.VISIBLE);
        }
    }
}
