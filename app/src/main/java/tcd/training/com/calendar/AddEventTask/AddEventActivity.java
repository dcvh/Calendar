package tcd.training.com.calendar.AddEventTask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Entities.Account;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Entities.Reminder;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.EventDetailsActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ReminderTask.ReminderUtils;
import tcd.training.com.calendar.Utils.ViewUtils;

public class AddEventActivity extends AppCompatActivity {

    private static final String TAG = AddEventActivity.class.getSimpleName();

    private static final int MENU_SAVE_ID = 1;

    private static final int STATUS_DIALOG_TYPE = 1;
    private static final int REPEAT_DIALOG_TYPE = 2;
    private static final int TIME_ZONE_DIALOG_TYPE = 3;
    private static final int NOTIFICATION_DIALOG_TYPE = 4;
    private static final int COLOR_DIALOG_TYPE = 5;
    private static final int ACCOUNT_DIALOG_TYPE = 6;

    private static final int RC_PLACE_AUTOCOMPLETE = 1;

    public static final int PRIORITY_NOTIFICATION = 0;
    public static final int PRIORITY_POPUP = 1;

    private static final int DATE_FORMAT_FLAGS =
            DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_ABBREV_ALL;
    private static final int TIME_FORMAT_FLAGS = DateUtils.FORMAT_SHOW_TIME;

    private TextView mStartDateTextView, mEndDateTextView, mStartTimeTextView, mEndTimeTextView, mLocationTextView, mTimeZoneTextView;
    private EditText mTitleEditText, mNoteEditText;
    private Switch mAllDaySwitch;
    private ImageView mCircleColor;

    private ArrayList<Account> mAccounts;
    private ArrayList<String> mAccountNames;
    private ArrayList<Integer> mAccountColors;
    private int mAccountIndex;

    private ArrayList<String> mColorNames;
    private ArrayList<Integer> mColorValues;
    private int mColorIndex;

    private ArrayList<String> mPriorityTitles;
    private int mPriorityIndex;

    private ArrayList<String> mAvailabilityTitles;
    private int mAvailabilityIndex;

    private ArrayList<String> mRepeatChoiceTitles;
    private int mRepeatIndex;

    private ArrayList<String> mTimeZoneTitles;
    private int mTimeZoneIndex;

    private int mDefaultReminderTime = 10;
    private ArrayList<String> mNotificationTitles;
    private ArrayList<Integer> mNotificationMinutes;
    private int mNotificationIndex;
    private int mUnitIndex;

    private Place mLocation;
    private Calendar mStart, mEnd;

    private Event mEvent;

    private boolean mEndActivity = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        updateActionBar();

        initializeVariables();
        if (mEndActivity) {
            finish();
            return;
        }

        initializeUiComponents();

        if (getIntent().hasExtra(EventDetailsActivity.ARG_CALENDAR_EVENT)) {
            mEvent = getIntent().getParcelableExtra(EventDetailsActivity.ARG_CALENDAR_EVENT);
            initializeValuesFromEvent();
        }

        changeActivityThemeColor();
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
                Event event = getEventFromInput();
                if (event != null) {
                    Snackbar.make(findViewById(android.R.id.content), R.string.wait_message, Snackbar.LENGTH_INDEFINITE);
                    Reminder reminder = event.hasAlarm() ?
                            new Reminder(mNotificationMinutes.get(mNotificationIndex), CalendarContract.Reminders.METHOD_DEFAULT) : null;

                    if (mEvent != null) {
                        long id = mEvent.getId();
                        mEvent = new Event(event);
                        mEvent.setId(id);
                        DataUtils.modifyEvent(mEvent, reminder, this);
                        setResult(RESULT_OK);
                    } else {
                        DataUtils.addEvent(event, reminder, this);
                    }

                    finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ?
                new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar) :
                new AlertDialog.Builder(this);
        builder
                .setMessage(getString(R.string.discard_event))
                .setPositiveButton(R.string.keep_editing, null)
                .setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .show();
    }

    private Event getEventFromInput() {

        String title = mTitleEditText.getText().toString();
        if (title.length() == 0) {
            Snackbar.make(findViewById(android.R.id.content), R.string.title_empty_error, Snackbar.LENGTH_SHORT).show();
            return null;
        }

        boolean isAllDay = mAllDaySwitch.isChecked();

        long calendarId = mAccounts.get(mAccountIndex).getId();

        String location = mLocation != null ? mLocation.getName().toString() : null;

        String description = mNoteEditText.getText().toString();

        String timeZone = mTimeZoneTitles.get(mTimeZoneIndex);

        boolean hasAlarm = mNotificationMinutes.get(mNotificationIndex) != 0;

        String rRule = null;
        if (mRepeatIndex > 0) {
            rRule = "FREQ=";
            switch (mRepeatIndex) {
                case 1: rRule += "DAILY"; break;
                case 2: rRule += "WEEKLY"; break;
                case 3: rRule += "MONTHLY"; break;
                case 4: rRule += "YEARLY"; break;
            }
            rRule += ";INTERVAL=1";
        }

        String duration = null;
        if (rRule != null) {
            duration = TimeUtils.getDurationString(mEnd.getTimeInMillis() - mStart.getTimeInMillis());
        }

        int displayColor = mColorValues.get(mColorIndex);

        int availability = mAvailabilityIndex == 0 ? CalendarContract.Events.AVAILABILITY_BUSY : CalendarContract.Events.AVAILABILITY_FREE;

        Event event = new Event(title, calendarId, location, description, timeZone, mStart.getTimeInMillis(), mEnd.getTimeInMillis(),
                isAllDay, hasAlarm, rRule, duration, displayColor, availability);
        event.setPriority(mPriorityIndex);
        return event;
    }

    private void updateActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
        }
    }

    private void initializeVariables() {

        mStart = Calendar.getInstance();
        mEnd = Calendar.getInstance();

        mAvailabilityIndex = 0;
        mColorIndex = 0;
        mRepeatIndex = -1;
        mNotificationIndex = 1;
        mAccountIndex = 0;

        mAccounts = DataUtils.getPrimaryAccounts();
        if (mAccounts.size() == 0) {
            Toast.makeText(getApplicationContext(), R.string.no_account_found, Toast.LENGTH_SHORT).show();
            mEndActivity = true;
            return;
        }
        mAccountNames = new ArrayList<>();
        mAccountColors = new ArrayList<>();
        for (Account account : mAccounts) {
            mAccountNames.add(account.getDisplayName());
            mAccountColors.add(account.getColor());
        }

        LinkedHashMap<String, Integer> colors = DataUtils.getAllColors();
        mColorNames = new ArrayList<>(colors.keySet());
        mColorNames.add(0, getString(R.string.default_color));
        mColorValues = new ArrayList<>(colors.values());
        mColorValues.add(0, mAccountColors.get(mAccountIndex));

        mPriorityTitles = new ArrayList<>();
        mPriorityTitles.add(getString(R.string.notification));
        mPriorityTitles.add(getString(R.string.popup));

        mAvailabilityTitles = new ArrayList<>();
        mAvailabilityTitles.add(getString(R.string.busy));
        mAvailabilityTitles.add(getString(R.string.available));

        mRepeatChoiceTitles = new ArrayList<>();
        mRepeatChoiceTitles.add(getString(R.string.does_not_repeat));
        mRepeatChoiceTitles.add(getString(R.string.every_day));
        mRepeatChoiceTitles.add(getString(R.string.every_week));
        mRepeatChoiceTitles.add(getString(R.string.every_month));
        mRepeatChoiceTitles.add(getString(R.string.every_year));

        createTimeZoneList();

        mNotificationTitles = new ArrayList<>();
        mNotificationMinutes = new ArrayList<>(Arrays.asList(0, mDefaultReminderTime, -1));
    }

    private void initializeValuesFromEvent() {

        // start date and time
        mStartDateTextView.setText(DateUtils.formatDateTime(this, mEvent.getStartDate(), DATE_FORMAT_FLAGS));
        mStartTimeTextView.setText(DateUtils.formatDateTime(this, mEvent.getStartDate(), TIME_FORMAT_FLAGS));

        // end date and time
        long endMillis = mEvent.getRRule() == null ? mEvent.getEndDate() :
                mEvent.getStartDate() + TimeUtils.getDurationValue(mEvent.getDuration());
        mEndDateTextView.setText(DateUtils.formatDateTime(this, endMillis, DATE_FORMAT_FLAGS));
        mEndTimeTextView.setText(DateUtils.formatDateTime(this, endMillis, TIME_FORMAT_FLAGS));

        // availability
        mAvailabilityIndex = mEvent.getAvailability() == CalendarContract.Events.AVAILABILITY_BUSY ? 0 : 1;

        // display color
        for (int i = 0; i < mColorValues.size(); i++) {
            if (mEvent.getDisplayColor() == mColorValues.get(i)) {
                mColorIndex = i;
                break;
            }
        }

        // repeat options
        if (mEvent.getRRule() == null) {
            mRepeatIndex = -1;
        } else {
            switch (mEvent.getRRule().charAt(5)) {
                case 'D': mRepeatIndex = 1; break;
                case 'W': mRepeatIndex = 2; break;
                case 'M': mRepeatIndex = 3; break;
                case 'Y': mRepeatIndex = 4; break;
            }
        }

        // title
        if (mEvent.getTitle() != null) {
            mTitleEditText.setText(mEvent.getTitle());
        }

        // all-day
        mAllDaySwitch.setChecked(mEvent.isAllDay());

        // location
        mLocationTextView.setText(mEvent.getLocation());

        // note
        mNoteEditText.setText(mEvent.getDescription());

        // notification
        int minutes = DataUtils.getReminderMinutes(mEvent.getId());
        if (minutes > -1) {
            if (minutes != mDefaultReminderTime  && minutes != 0) {
                mNotificationMinutes.add(mNotificationMinutes.size() - 1, minutes);
            }
        }

        // timezone
        for (int i = 0; i < mTimeZoneTitles.size(); i++) {
            if (mTimeZoneTitles.get(i).equals(mEvent.getTimeZone())) {
                mTimeZoneIndex = i;
                break;
            }
        }

        // limit to that account
        while (true){
            if (mEvent.getCalendarId() != mAccounts.get(0).getId()) {
                mAccounts.remove(0);
                continue;
            }
            for (int i = 1; i < mAccounts.size();) {
                mAccounts.remove(1);
            }
            break;
        }
    }

    private void createTimeZoneList() {
        ArrayList<TimeZone> timeZoneList = new ArrayList<>();
        for (String id : TimeZone.getAvailableIDs()) {
            timeZoneList.add(TimeZone.getTimeZone(id));
        }
        Collections.sort(timeZoneList, new Comparator<TimeZone>() {
            public int compare(TimeZone s1, TimeZone s2) {
                return s1.getRawOffset() - s2.getRawOffset();
            }});

        mTimeZoneTitles = new ArrayList<>();
        for (int i = 0; i < timeZoneList.size(); i++) {
            mTimeZoneTitles.add(getTimeZoneTitle(timeZoneList.get(i)));
            if (timeZoneList.get(i).equals(TimeZone.getDefault())) {
                mTimeZoneIndex = i;
            }
        }
    }

    private String getTimeZoneTitle(TimeZone tz) {

        long hours = TimeUnit.MILLISECONDS.toHours(tz.getRawOffset());
        long minutes = TimeUnit.MILLISECONDS.toMinutes(tz.getRawOffset())
                - TimeUnit.HOURS.toMinutes(hours);

        // avoid -4:-30 issue
        minutes = Math.abs(minutes);

        String result;
        if (hours > 0) {
            result = String.format("%s (GMT+%d:%02d)", tz.getID(), hours, minutes);
        } else {
            result = String.format("%s (GMT%d:%02d)", tz.getID(), hours, minutes);
        }

        return result;

    }

    private void initializeUiComponents() {

        if (mEndActivity) {
            return;
        }

        mTitleEditText = (EditText) findViewById(R.id.edt_event_title);

        initializeAccountsOption();
        initializePeopleOption();
        initializeDateTimeOption();
        initializePriorityOption();
        initializeLocationOption();
        initializeNotificationOption();
        initializeColorOption();
        initializeNoteOption();
        initializeStatusOption();
    }

    private void changeActivityThemeColor() {
        // action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(mColorValues.get(mColorIndex)));
        }

        // status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ViewUtils.getDarkerColor(mColorValues.get(mColorIndex)));
        }

        mTitleEditText.setBackgroundColor(mColorValues.get(mColorIndex));

        // circle color icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCircleColor.setImageTintList(ColorStateList.valueOf(mColorValues.get(mColorIndex)));
        } else {
            mCircleColor.setColorFilter(mColorValues.get(mColorIndex));
        }
    }

    private void initializeAccountsOption() {
        LinearLayout colorLayout = (LinearLayout) findViewById(R.id.ll_accounts);

        ((ImageView)colorLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_action_today_black_48dp);

        final TextView content = colorLayout.findViewById(R.id.tv_primary_content);
        content.setText(mAccountNames.get(0));

        colorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAccounts.size() > 1) {
                    showChoicePickerDialog(ACCOUNT_DIALOG_TYPE, content);
                }
            }
        });
    }

    private void initializePeopleOption() {
        LinearLayout peopleLayout = (LinearLayout) findViewById(R.id.ll_invite_people);

        ((ImageView)peopleLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_people_black_48dp);
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
        ((ImageView)findViewById(R.id.iv_date_time_icon)).setImageResource(R.drawable.ic_time_black_48dp);

        // references
        mAllDaySwitch = (Switch) findViewById(R.id.sw_all_day);
        mStartDateTextView = (TextView) findViewById(R.id.tv_start_date);
        mEndDateTextView = (TextView) findViewById(R.id.tv_end_date);
        mStartTimeTextView = (TextView) findViewById(R.id.tv_start_time);
        mEndTimeTextView = (TextView) findViewById(R.id.tv_end_time);

        // all day switch trigger
        mAllDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    mStartTimeTextView.setVisibility(View.INVISIBLE);
                    mEndTimeTextView.setVisibility(View.INVISIBLE);
                } else {
                    mStartTimeTextView.setVisibility(View.VISIBLE);
                    mEndTimeTextView.setVisibility(View.VISIBLE);
                }
            }
        });

        // default date time values
        long curTime = Calendar.getInstance().getTimeInMillis() + TimeUnit.MINUTES.toMillis(10);

        String today = DateUtils.formatDateTime(this, curTime, DATE_FORMAT_FLAGS);
        mStartDateTextView.setText(today);
        mEndDateTextView.setText(today);

        mStartTimeTextView.setText(DateUtils.formatDateTime(this, curTime, TIME_FORMAT_FLAGS));
        int defaultEventDuration = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_default_event_duration), getString(R.string.pref_default_event_duration_values_default)));
        mEndTimeTextView.setText(DateUtils.formatDateTime(this, curTime + TimeUnit.MINUTES.toMillis(defaultEventDuration), TIME_FORMAT_FLAGS));

        // on click listener
        View.OnClickListener dateListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog((TextView) view);
            }
        };
        mStartDateTextView.setOnClickListener(dateListener);
        mEndDateTextView.setOnClickListener(dateListener);
        View.OnClickListener timeListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePickerDialog((TextView) view);
            }
        };
        mStartTimeTextView.setOnClickListener(timeListener);
        mEndTimeTextView.setOnClickListener(timeListener);

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

    private void showDatePickerDialog(final TextView textView) {
        Calendar today = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                        Calendar date = textView.getId() == R.id.tv_start_date ? mStart : mEnd;
                        date.set(year, month, dayOfMonth);
                        textView.setText(DateUtils.formatDateTime(AddEventActivity.this, date.getTimeInMillis(), DATE_FORMAT_FLAGS));
                    }
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showTimePickerDialog(final TextView textView) {
        final Calendar today = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        Calendar date = textView.getId() == R.id.tv_start_time ? mStart : mEnd;
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);
                        textView.setText(DateUtils.formatDateTime(AddEventActivity.this, date.getTimeInMillis(), TIME_FORMAT_FLAGS));
                    }
                },
                today.get(Calendar.HOUR_OF_DAY),
                today.get(Calendar.MINUTE),
                false);
        timePickerDialog.show();
    }

    private void initializeTimeZoneOption() {
        LinearLayout timeZoneLayout = (LinearLayout) findViewById(R.id.ll_timezone);
        timeZoneLayout.setVisibility(View.VISIBLE);

        ((ImageView)timeZoneLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_time_zone_black_48dp);
        mTimeZoneTextView = timeZoneLayout.findViewById(R.id.tv_primary_content);
        mTimeZoneTextView.setText(mTimeZoneTitles.get(mTimeZoneIndex));

        timeZoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoicePickerDialog(TIME_ZONE_DIALOG_TYPE, mTimeZoneTextView);
            }
        });
    }

    private void initializeRepeatOption() {
        LinearLayout repeatLayout = (LinearLayout) findViewById(R.id.ll_repeat);
        repeatLayout.setVisibility(View.VISIBLE);

        mRepeatIndex = 0;

        ((ImageView)repeatLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_repeat_black_48dp);
        final TextView content = repeatLayout.findViewById(R.id.tv_primary_content);
        content.setText(mRepeatChoiceTitles.get(mRepeatIndex));

        repeatLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoicePickerDialog(REPEAT_DIALOG_TYPE, content);
            }
        });
    }

    private void initializePriorityOption() {
        LinearLayout colorLayout = (LinearLayout) findViewById(R.id.ll_default_color);

        mCircleColor = colorLayout.findViewById(R.id.iv_icon);
        mCircleColor.setImageResource(R.drawable.ic_filled_circle_48dp);

        final TextView content = colorLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.default_color);

        colorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoicePickerDialog(COLOR_DIALOG_TYPE, content);
            }
        });
    }

    private void initializeLocationOption() {
        LinearLayout locationLayout = (LinearLayout) findViewById(R.id.ll_location);

        ((ImageView)locationLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_location_black_48dp);
        mLocationTextView = locationLayout.findViewById(R.id.tv_primary_content);
        mLocationTextView.setText(R.string.add_location);

        locationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN).build(AddEventActivity.this);
                    startActivityForResult(intent, RC_PLACE_AUTOCOMPLETE);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializeNotificationOption() {
        LinearLayout notificationLayout = (LinearLayout) findViewById(R.id.ll_notification);

        ((ImageView)notificationLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_notifications_black_48dp);
        final TextView content = notificationLayout.findViewById(R.id.tv_primary_content);
        content.setText(ViewUtils.getNotificationTimeFormat(mDefaultReminderTime, AddEventActivity.this));

        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mNotificationTitles = new ArrayList<>();
                for (int minutes : mNotificationMinutes) {
                    if (minutes == 0) {
                        mNotificationTitles.add(getString(R.string.no_notification));
                    } else if (minutes == -1) {
                        mNotificationTitles.add(getString(R.string.custom));
                    } else {
                        mNotificationTitles.add(ViewUtils.getNotificationTimeFormat(minutes, AddEventActivity.this));
                    }
                }
                showChoicePickerDialog(NOTIFICATION_DIALOG_TYPE, content);
            }
        });
    }

    private void initializeColorOption() {
        LinearLayout colorLayout = (LinearLayout) findViewById(R.id.ll_default_color);

        mCircleColor = colorLayout.findViewById(R.id.iv_icon);
        mCircleColor.setImageResource(R.drawable.ic_filled_circle_48dp);

        final TextView content = colorLayout.findViewById(R.id.tv_primary_content);
        content.setText(R.string.default_color);

        colorLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoicePickerDialog(COLOR_DIALOG_TYPE, content);
            }
        });
    }

    private void initializeNoteOption() {
        LinearLayout noteLayout = (LinearLayout) findViewById(R.id.ll_note);

        ((ImageView)noteLayout.findViewById(R.id.iv_note_icon)).setImageResource(R.drawable.ic_description_black_48dp);
        mNoteEditText = noteLayout.findViewById(R.id.edt_note);
    }

    private void initializeStatusOption() {
        LinearLayout timeZoneLayout = (LinearLayout) findViewById(R.id.ll_status);

        ((ImageView)timeZoneLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_availability_black_48dp);
        final TextView content = timeZoneLayout.findViewById(R.id.tv_primary_content);
        content.setText(mAvailabilityTitles.get(mAvailabilityIndex));

        timeZoneLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChoicePickerDialog(STATUS_DIALOG_TYPE, content);
            }
        });
    }

    private void showChoicePickerDialog(final int type, final TextView displayView) {
        final Dialog dialog;
        final ListView listView = new ListView(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        ArrayAdapter adapter;
        switch (type) {
            case STATUS_DIALOG_TYPE:
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog, mAvailabilityTitles, mAvailabilityIndex);
                break;
            case REPEAT_DIALOG_TYPE:
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog, mRepeatChoiceTitles, mRepeatIndex);
                break;
            case TIME_ZONE_DIALOG_TYPE:
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog, mTimeZoneTitles, mTimeZoneIndex);
                break;
            case NOTIFICATION_DIALOG_TYPE:
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog, mNotificationTitles, mNotificationIndex);
                break;
            case COLOR_DIALOG_TYPE:
                adapter = new ColorAdapter(this, mColorNames, mColorValues, mColorIndex);
                break;
            case ACCOUNT_DIALOG_TYPE:
                adapter = new ColorAdapter(this, mAccountNames, mAccountColors, mAccountIndex);
                break;
            default:
                throw new UnsupportedOperationException("Unknown dialog type");
        }

        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setPadding(0, ViewUtils.dpToPixel(24), 0, 0);
        builder.setView(listView);

        dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (type) {
                    case STATUS_DIALOG_TYPE:
                        mAvailabilityIndex = i;
                        displayView.setText(mAvailabilityTitles.get(i));
                        break;
                    case REPEAT_DIALOG_TYPE:
                        mRepeatIndex = i;
                        displayView.setText(mRepeatChoiceTitles.get(i));
                        break;
                    case TIME_ZONE_DIALOG_TYPE:
                        mTimeZoneIndex = i;
                        displayView.setText(mTimeZoneTitles.get(i));
                        break;
                    case NOTIFICATION_DIALOG_TYPE:
                        mNotificationIndex = i;
                        displayView.setText(mNotificationTitles.get(i));
                        if (i == mNotificationTitles.size() - 1) {
                            showCustomNotificationDialog(displayView);
                        }
                        break;
                    case COLOR_DIALOG_TYPE:
                        mColorIndex = i;
                        displayView.setText(mColorNames.get(i));
                        changeActivityThemeColor();
                        break;
                    case ACCOUNT_DIALOG_TYPE:
                        mAccountIndex = i;
                        displayView.setText(mAccountNames.get(i));
                        mColorValues.set(0, mAccountColors.get(i));
                        changeActivityThemeColor();
                        break;
                }
                dialog.dismiss();
            }
        });
    }

    private void showCustomNotificationDialog(final TextView displayView) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_custom_notification);
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        // edit text
        final EditText valueTextView = dialog.findViewById(R.id.edt_notification_value);
        valueTextView.setText(String.valueOf(mDefaultReminderTime));

        // list view
        ListView unitListView = dialog.findViewById(R.id.lv_unit);
        ArrayList<String> units = new ArrayList<>(Arrays.asList(getString(R.string.minute), getString(R.string.hour), getString(R.string.day), getString(R.string.week)));
        mUnitIndex = 0;
        DialogListAdapter adapter = new DialogListAdapter(this, R.layout.list_item_dialog, units, mUnitIndex);
        unitListView.setAdapter(adapter);
        unitListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mUnitIndex = i;
            }
        });

        // list view
        ListView priorityListView = dialog.findViewById(R.id.lv_priority);
        ArrayList<String> priorities = new ArrayList<>(Arrays.asList(getString(R.string.notification), getString(R.string.popup)));
        mPriorityIndex = 0;
        adapter = new DialogListAdapter(this, R.layout.list_item_dialog, priorities, mPriorityIndex);
        priorityListView.setAdapter(adapter);
        priorityListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mPriorityIndex = i;
            }
        });

        // done
        TextView done = dialog.findViewById(R.id.tv_done);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int time = Integer.parseInt(valueTextView.getText().toString());
                switch (mUnitIndex) {
                    case 0: time = (int) TimeUnit.MINUTES.toMinutes(time); break;
                    case 1: time = (int) TimeUnit.HOURS.toMinutes(time); break;
                    case 2: time = (int) TimeUnit.DAYS.toMinutes(time); break;
                    case 3: time = (int) TimeUnit.DAYS.toMinutes(time) * 7; break;
                }
                String timeString = ViewUtils.getNotificationTimeFormat(time, AddEventActivity.this) + getString(R.string.before);
                displayView.setText(timeString);
                mNotificationMinutes.add(mNotificationMinutes.size() - 1, time);
                mNotificationIndex = mNotificationMinutes.size() - 2;
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_PLACE_AUTOCOMPLETE) {
            if (resultCode == RESULT_OK) {
                mLocation = PlaceAutocomplete.getPlace(this, data);
                mLocationTextView.setText(mLocation.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, R.string.cancelled, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
