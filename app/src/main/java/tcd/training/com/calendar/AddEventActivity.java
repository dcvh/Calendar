package tcd.training.com.calendar;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Calendar.CalendarUtils;

public class AddEventActivity extends AppCompatActivity {

    private static final int MENU_SAVE_ID = 1;

    private EditText mTitleEditText;
    private LinearLayout mDateTimeLayout, mRepetitionLayout,
            mLocationLayout, mNotificationLayout, mColorLayout, mNoteLayout, mStatusLayout;

    private int mDefaultColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        updateActionBar();

        initializeUiComponents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_SAVE_ID, Menu.NONE, R.string.save).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case MENU_SAVE_ID:

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            mDefaultColor = CalendarUtils.getAccountColor(0);

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            actionBar.setBackgroundDrawable(new ColorDrawable(mDefaultColor));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Window window = getWindow();
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.setStatusBarColor(mDefaultColor);
            }
        }
    }

    private void initializeUiComponents() {
        mTitleEditText = (EditText) findViewById(R.id.edt_event_title);
        mTitleEditText.setBackgroundColor(mDefaultColor);

        initializePeopleOption();
        initializeDateTimeOption();
        initializeLocationOption();
        initializeNotificationOption();
        initializeColorOption();
        initializeNoteOption();
        initializeStatusOption();
    }

    private void initializePeopleOption() {
        LinearLayout peopleLayout = (LinearLayout) findViewById(R.id.ll_invite_people);

        ((ImageView)peopleLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_people_black_48dp);
        TextView content = peopleLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.invite_people);

        peopleLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add invite people function
            }
        });
    }

    private void initializeDateTimeOption() {
        // icon
        ((ImageView)findViewById(R.id.iv_date_time_icon)).setImageResource(R.mipmap.ic_time_black_48dp);

        // default date time values
        TextView startDateTextView = (TextView) findViewById(R.id.tv_start_date);
        TextView endDateTextView = (TextView) findViewById(R.id.tv_end_date);
        TextView startTimeTextView = (TextView) findViewById(R.id.tv_start_time);
        TextView endTimeTextView = (TextView) findViewById(R.id.tv_end_time);

        long curTime = Calendar.getInstance().getTimeInMillis();

        String today = CalendarUtils.getDate(curTime, CalendarUtils.getStandardDateFormat());
        startDateTextView.setText(today);
        endDateTextView.setText(today);

        startTimeTextView.setText(CalendarUtils.getDate(curTime, CalendarUtils.getStandardTimeFormat()));
        endTimeTextView.setText(CalendarUtils.getDate(curTime + TimeUnit.HOURS.toMillis(1), CalendarUtils.getStandardTimeFormat()));

        // more options
        final TextView moreOptionsTextView = (TextView) findViewById(R.id.tv_more_options);
        moreOptionsTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionsTextView.setVisibility(View.GONE);
                initializeTimeZoneOption();
                initializeRepeatOption();
            }
        });
    }

    private void initializeTimeZoneOption() {
        LinearLayout timeZoneLayout = (LinearLayout) findViewById(R.id.ll_timezone);
        timeZoneLayout.setVisibility(View.VISIBLE);

        ((ImageView)timeZoneLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_time_zone_black_48dp);
        TextView content = timeZoneLayout.findViewById(R.id.tv_primary_content);

        timeZoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add time zone function
            }
        });
    }

    private void initializeRepeatOption() {
        LinearLayout repeatLayout = (LinearLayout) findViewById(R.id.ll_repeat);
        repeatLayout.setVisibility(View.VISIBLE);

        ((ImageView)repeatLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_repeat_black_48dp);
        TextView content = repeatLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.does_not_repeat);

        repeatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add repeat function
            }
        });
    }

    private void initializeLocationOption() {
        LinearLayout locationLayout = (LinearLayout) findViewById(R.id.ll_location);

        ((ImageView)locationLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_location_black_48dp);
        TextView content = locationLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.add_location);

        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add location function
            }
        });
    }

    private void initializeNotificationOption() {
        LinearLayout notificationLayout = (LinearLayout) findViewById(R.id.ll_notification);

        ((ImageView)notificationLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_notifications_black_48dp);
        TextView content = notificationLayout.findViewById(R.id.tv_primary_content);
        // TODO: 9/7/17 default notification, get value from settings
        content.setText("");

        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add location function
            }
        });
    }

    private void initializeColorOption() {
        LinearLayout colorLayout = (LinearLayout) findViewById(R.id.ll_default_color);

        // TODO: 9/7/17 draw a circle to indicate the chosen color
        ((ImageView)colorLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_notifications_black_48dp);
        TextView content = colorLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.default_color);

        colorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 choose default color function
            }
        });
    }

    private void initializeNoteOption() {
        LinearLayout noteLayout = (LinearLayout) findViewById(R.id.ll_note);

        ((ImageView)noteLayout.findViewById(R.id.iv_note_icon)).setImageResource(R.mipmap.ic_description_black_48dp);
        EditText noteEditText = noteLayout.findViewById(R.id.edt_note);

        noteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add note function
            }
        });
    }

    private void initializeStatusOption() {
        LinearLayout timeZoneLayout = (LinearLayout) findViewById(R.id.ll_status);

        ((ImageView)timeZoneLayout.findViewById(R.id.iv_icon)).setImageResource(R.mipmap.ic_status_black_48dp);
        TextView content = timeZoneLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.busy);

        timeZoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 9/7/17 add status function
            }
        });
    }
}
