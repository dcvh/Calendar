package tcd.training.com.calendar;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Entities.Account;
import tcd.training.com.calendar.Entities.Attendee;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.Utils.ViewUtils;

public class EventDetailsActivity extends AppCompatActivity {

    private static final String TAG = EventDetailsActivity.class.getSimpleName();

    public static final String ARG_CALENDAR_EVENT = "calendarEvent";
    private static final int MENU_EDIT_ID = 1;
    private static final int MENU_DELETE_ID = 2;
    private static final int RC_EDIT_COMPLETE = 3;

    private LinearLayout mDateTimeLayout,
            mLocationLayout,
            mNotificationLayout,
            mGuestsLayout,
            mDescriptionLayout,
            mAccountDisplayNameLayout,
            mAvailabilityLayout;
    private Event mEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        initializeUiComponents();

        mEvent = getIntent().getParcelableExtra(ARG_CALENDAR_EVENT);

        updateActionBar();

        showEventInfo();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_EDIT_ID, Menu.NONE, R.string.edit).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        for (Account account : DataUtils.getPrimaryAccounts()) {
            if (account.getId() == mEvent.getCalendarId()) {
                menu.add(0, MENU_DELETE_ID, Menu.NONE, R.string.delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                break;
            }
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case MENU_EDIT_ID:
                Intent intent = new Intent(this, AddEventActivity.class);
                intent.putExtra(ARG_CALENDAR_EVENT, mEvent);
                startActivityForResult(intent, RC_EDIT_COMPLETE);
                break;
            case MENU_DELETE_ID:
                DataUtils.removeEvent(mEvent.getId(), this);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_EDIT_COMPLETE:
                if (resultCode == RESULT_OK) {
                    finish();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void updateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(new ColorDrawable(mEvent.getDisplayColor()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ViewUtils.getDarkerColor(mEvent.getDisplayColor()));
        }
    }

    private void initializeUiComponents() {
        mDateTimeLayout = (LinearLayout) findViewById(R.id.ll_date_time);
        mLocationLayout = (LinearLayout) findViewById(R.id.ll_location);
        mNotificationLayout = (LinearLayout) findViewById(R.id.ll_notification);
        mGuestsLayout = (LinearLayout) findViewById(R.id.ll_guests);
        mDescriptionLayout = (LinearLayout) findViewById(R.id.ll_description);
        mAccountDisplayNameLayout = (LinearLayout) findViewById(R.id.ll_account);
        mAvailabilityLayout = (LinearLayout) findViewById(R.id.ll_availability);
    }

    private void showEventInfo() {

        displayTitle();

        displayDateTime();

        displayLocation();

        displayNotificationInfo();

        displayGuestsInfo();

        displayDescription();

        displayAccountDisplayName();

        displayAvailability();
    }

    private void displayTitle() {
        String title = mEvent.getTitle() != null && mEvent.getTitle().length() > 0 ? mEvent.getTitle() : getString(R.string.no_title);
        ((TextView)findViewById(R.id.tv_event_title)).setText(title);

        findViewById(R.id.ll_event_title).setBackgroundColor(mEvent.getDisplayColor());
    }

    private void displayDateTime() {

        ((ImageView)mDateTimeLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_time_black_48dp);
        TextView dateTimeTextView = mDateTimeLayout.findViewById(R.id.tv_primary_content);

        String dateTime;
        long startDate = mEvent.getStartDate();
        long endDate = mEvent.getDuration() == null ? mEvent.getEndDate() : startDate + TimeUtils.getDurationValue(mEvent.getDuration());
        if (TimeUtils.isSameDay(startDate, endDate)) {
            dateTime = TimeUtils.getFormattedDate(startDate, "EEEE, MMMM d")
                    + "\n"
                    + TimeUtils.getFormattedDate(startDate, "hh:mm a")
                    + " - "
                    + TimeUtils.getFormattedDate(endDate, "hh:mm a");
        } else {
            String format = "EEEE, MMMM d, hh:mm a";
            dateTime = TimeUtils.getFormattedDate(startDate, format);
            if (!mEvent.isAllDay()) {
                dateTime += " -\n" + TimeUtils.getFormattedDate(endDate, format);
            }
        }
        dateTimeTextView.setText(dateTime);
    }

    private void displayLocation() {
        if (mEvent.getLocation() != null && mEvent.getLocation().length() > 0) {
            ((ImageView)mLocationLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_location_black_48dp);
            ((TextView)mLocationLayout.findViewById(R.id.tv_primary_content)).setText(mEvent.getLocation());
            mLocationLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayGuestsInfo() {
        ArrayList<Attendee> attendees = DataUtils.getEventAttendees(mEvent.getId());
        if (attendees.size() > 0) {
            ((ImageView) mGuestsLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_people_black_48dp);

            TextView content = mGuestsLayout.findViewById(R.id.tv_primary_content);
            content.setText(getResources().getQuantityString(R.plurals.x_guest, attendees.size(), attendees.size()));
            for (Attendee attendee : attendees) {
                content.append(String.format(getString(R.string.guest_email), attendee.getEmail()));
            }

            mGuestsLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayNotificationInfo() {
        if (mEvent.hasAlarm()) {
            int minutes = DataUtils.getReminderMinutes(mEvent.getId());
            if (minutes > -1) {
                ((ImageView) mNotificationLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_notifications_black_48dp);
                String notification = ViewUtils.getNotificationTimeFormat(minutes, this);
                ((TextView) mNotificationLayout.findViewById(R.id.tv_primary_content)).setText(notification);
                mNotificationLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void displayDescription() {
        if (mEvent.getDescription() != null && mEvent.getDescription().length() > 0) {
            ((ImageView)mDescriptionLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_description_black_48dp);
            ((TextView)mDescriptionLayout.findViewById(R.id.tv_primary_content)).setText(mEvent.getDescription());
            mDescriptionLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayAccountDisplayName() {
        String displayName = DataUtils.getAccountDisplayName(mEvent.getCalendarId());
        if (displayName != null) {
            ((ImageView)mAccountDisplayNameLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_action_today_black_48dp);
            ((TextView)mAccountDisplayNameLayout.findViewById(R.id.tv_primary_content)).setText(displayName);
            mAccountDisplayNameLayout.setVisibility(View.VISIBLE);
        }
    }

    private void displayAvailability() {
        int availability = mEvent.getAvailability();
        ((ImageView)mAvailabilityLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_availability_black_48dp);
        int avaiStringId = R.string.busy;
        switch (availability) {
            case CalendarContract.Events.AVAILABILITY_BUSY: avaiStringId = R.string.busy; break;
            case CalendarContract.Events.AVAILABILITY_FREE: avaiStringId = R.string.available; break;
            case CalendarContract.Events.AVAILABILITY_TENTATIVE: avaiStringId = R.string.tentative; break;
        }
        ((TextView)mAvailabilityLayout.findViewById(R.id.tv_primary_content)).setText(avaiStringId);
        mAvailabilityLayout.setVisibility(View.VISIBLE);
    }
}
