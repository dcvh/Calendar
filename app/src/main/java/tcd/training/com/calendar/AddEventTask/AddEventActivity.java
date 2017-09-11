package tcd.training.com.calendar.AddEventTask;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ContentView.ViewUtils;

public class AddEventActivity extends AppCompatActivity {

    private static final int MENU_SAVE_ID = 1;
    private static final int STATUS_DIALOG_TYPE = 1;
    private static final int REPEAT_DIALOG_TYPE = 2;
    private static final int NOTIFICATION_DIALOG_TYPE = 3;
    private static final int COLOR_DIALOG_TYPE = 4;
    private static final String TAG = AddEventActivity.class.getSimpleName();
    private static final int RC_PLACE_AUTOCOMPLETE = 1;

    private EditText mTitleEditText, mNoteEditText;
    private TextView mStartDateTextView, mEndDateTextView, mStartTimeTextView, mEndTimeTextView, mLocationTextView;
    private Switch mAllDaySwitch;
    private ImageView mCircleColor;

    private ArrayList<String> mColorNames;
    private ArrayList<Integer> mColorValues;
    private int mStatusIndex;

    private ArrayList<String> mStatusTitles;
    private int mColorIndex;

    private ArrayList<String> mRepeatChoiceTitles;
    private int mRepeatIndex;

    private int mDefaultReminderTime = 10;
    private ArrayList<String> mNotificationTitles;
    private int mNotificationIndex;
    private int mUnitIndex;

    private Place mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        updateActionBar();

        initializeVariables();

        initializeUiComponents();

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

                break;
        }
        return super.onOptionsItemSelected(item);
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
        LinkedHashMap<String, Integer> colors = DataUtils.getAllColors();
        mColorNames = new ArrayList<>(colors.keySet());
        mColorValues = new ArrayList<>(colors.values());

        mStatusTitles = new ArrayList<>();
        mStatusTitles.add(getString(R.string.busy));
        mStatusTitles.add(getString(R.string.available));

        mRepeatChoiceTitles = new ArrayList<>();
        mRepeatChoiceTitles.add(getString(R.string.does_not_repeat));
        mRepeatChoiceTitles.add(getString(R.string.every_day));
        mRepeatChoiceTitles.add(getString(R.string.every_week));
        mRepeatChoiceTitles.add(getString(R.string.every_month));
        mRepeatChoiceTitles.add(getString(R.string.every_year));

        mNotificationTitles = new ArrayList<>();
        mNotificationTitles.add(getString(R.string.no_notification));
        mNotificationTitles.add(ViewUtils.getNotificationTimeFormat(mDefaultReminderTime, this) + getString(R.string.before));
        mNotificationTitles.add(getString(R.string.custom));

        mStatusIndex = 0;
        mColorIndex = 0;
        mRepeatIndex = 0;
        mNotificationIndex = 1;
    }

    private void initializeUiComponents() {
        mTitleEditText = (EditText) findViewById(R.id.edt_event_title);

        initializePeopleOption();
        initializeDateTimeOption();
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
            window.setStatusBarColor(mColorValues.get(mColorIndex));
        }

        mTitleEditText.setBackgroundColor(mColorValues.get(mColorIndex));

        // circle color icon
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCircleColor.setImageTintList(ColorStateList.valueOf(mColorValues.get(mColorIndex)));
        } else {
            mCircleColor.setColorFilter(mColorValues.get(mColorIndex));
        }
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
        long curTime = Calendar.getInstance().getTimeInMillis();

        String today = TimeUtils.getFormattedDate(curTime, ViewUtils.getAddEventDateFormat());
        mStartDateTextView.setText(today);
        mEndDateTextView.setText(today);

        mStartTimeTextView.setText(TimeUtils.getFormattedDate(curTime, TimeUtils.getStandardTimeFormat()));
        int defaultEventDuration = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_key_default_event_duration), getString(R.string.pref_default_event_duration_values_default)));
        mEndTimeTextView.setText(TimeUtils.getFormattedDate(curTime + TimeUnit.MINUTES.toMillis(defaultEventDuration), TimeUtils.getStandardTimeFormat()));

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
//                initializeTimeZoneOption();
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
                        Calendar date = Calendar.getInstance();
                        date.set(year, month, dayOfMonth);
                        textView.setText(TimeUtils.getFormattedDate(date.getTimeInMillis(), ViewUtils.getAddEventDateFormat()));
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
                        Calendar date = Calendar.getInstance();
                        date.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        date.set(Calendar.MINUTE, minute);
                        textView.setText(TimeUtils.getFormattedDate(date.getTimeInMillis(), TimeUtils.getStandardTimeFormat()));
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
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initializeNotificationOption() {
        LinearLayout notificationLayout = (LinearLayout) findViewById(R.id.ll_notification);

        ((ImageView)notificationLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_notifications_black_48dp);
        final TextView content = notificationLayout.findViewById(R.id.tv_primary_content);
        content.setText(mNotificationTitles.get(mNotificationIndex));

        notificationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

        ((ImageView)timeZoneLayout.findViewById(R.id.iv_icon)).setImageResource(R.drawable.ic_status_black_48dp);
        final TextView content = timeZoneLayout.findViewById(R.id.tv_primary_content);
        content.setText(mStatusTitles.get(mStatusIndex));

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
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog,
                        mStatusTitles,
                        mStatusIndex);
                break;
            case REPEAT_DIALOG_TYPE:
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog,
                        mRepeatChoiceTitles,
                        mRepeatIndex);
                break;
            case NOTIFICATION_DIALOG_TYPE:
                adapter = new DialogListAdapter(AddEventActivity.this, R.layout.list_item_dialog,
                        mNotificationTitles,
                        mNotificationIndex);
                break;
            case COLOR_DIALOG_TYPE:
                adapter = new ColorAdapter(this,
                        mColorNames,
                        mColorValues,
                        mColorIndex);
                break;
            default:
                throw new UnsupportedOperationException("Unknown dialog type");
        }

        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        listView.setPadding(0, 0, 0, ViewUtils.dpToPixel(24));
        builder.setView(listView);

        dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (type) {
                    case STATUS_DIALOG_TYPE:
                        mStatusIndex = i;
                        displayView.setText(mStatusTitles.get(i));
                        break;
                    case REPEAT_DIALOG_TYPE:
                        mRepeatIndex = i;
                        displayView.setText(mRepeatChoiceTitles.get(i));
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
                }
                dialog.dismiss();
            }
        });
    }

    private void showCustomNotificationDialog(final TextView displayView) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_custom_notification);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        // edit text
        final EditText valueTextView = dialog.findViewById(R.id.edt_notification_value);
        valueTextView.setText(String.valueOf(mDefaultReminderTime));

        // list view
        ListView listView = dialog.findViewById(R.id.lv_unit);
        ArrayList<String> units = new ArrayList<>(Arrays.asList(getString(R.string.minutes), getString(R.string.hours), getString(R.string.days), getString(R.string.weeks)));
        mUnitIndex = 0;
        final DialogListAdapter adapter = new DialogListAdapter(this, R.layout.list_item_dialog, units, mUnitIndex);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mUnitIndex = i;
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
                mNotificationTitles.add(mNotificationTitles.size() - 1, timeString);
                mNotificationIndex = mNotificationTitles.size() - 2;
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

            }
        }
    }
}
