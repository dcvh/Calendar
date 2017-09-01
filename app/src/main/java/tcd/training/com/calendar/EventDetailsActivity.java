package tcd.training.com.calendar;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
            mOrganizerLayout;
    private CalendarEvent mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeUiComponents();

        mEvent = getIntent().getParcelableExtra(ARG_CALENDAR_ENTRY);

        getSupportActionBar().setTitle(mEvent.getTitle());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        showEventInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void initializeUiComponents() {
        mDateTimeLayout = (LinearLayout) findViewById(R.id.ll_date_time);
        mLocationLayout = (LinearLayout) findViewById(R.id.ll_location);
        mNotificationLayout = (LinearLayout) findViewById(R.id.ll_notification);
        mGuestsLayout = (LinearLayout) findViewById(R.id.ll_guests);
        mDescriptionLayout = (LinearLayout) findViewById(R.id.ll_description);
        mOrganizerLayout = (LinearLayout) findViewById(R.id.ll_account);
    }

    private void showEventInfo() {
        displayDateTime();

        displayLocation();

        displayNotificationInfo();

        displayGuestsInfo();

        displayDescription();

        displayOrganizer();
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

    private void displayOrganizer() {
        if (mEvent.getOrganizer().length() > 0) {
            ((ImageView)mOrganizerLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_action_today_black_24dp);
            ((TextView)mOrganizerLayout.findViewById(R.id.tv_primary_description)).setText(mEvent.getOrganizer());
            mOrganizerLayout.setVisibility(View.VISIBLE);
        }
    }
}
