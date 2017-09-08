package tcd.training.com.calendar.Calendar;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarUtils {

    private static final String TAG = CalendarUtils.class.getSimpleName();

    private static final LinkedHashMap<String, Integer> mDefaultColors = new LinkedHashMap<>();
    private static ArrayList<CalendarEntry> mEntries;
    private static ArrayList<Account> mAccounts;
    private static ArrayList<Reminder> mReminders;
    private static ArrayList<Attendee> mAttendees;
    private static int mColorOffset = 0;

    public static void readCalendarEventsInfo(Context context) {
        mEntries = new ArrayList<>();
        mAccounts = new ArrayList<>();
        mReminders = new ArrayList<>();
        mAttendees = new ArrayList<>();

        readCalendarEntries(context);

        createDefaultColors(context);
        readCalendarAccounts(context);

        readCalendarReminders(context);

        readCalendarEventAttendees(context);
    }

    private static ArrayList<CalendarEntry> readCalendarEntries(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return mEntries;
        }

        // prepare Uri
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = CalendarContract.Events.CONTENT_URI;
        LinkedHashMap<String, Integer> projections = new LinkedHashMap<>();
        int i = 0;
        projections.put(CalendarContract.Events._ID, i++);
        projections.put(CalendarContract.Events.TITLE, i++);
        projections.put(CalendarContract.Events.CALENDAR_ID, i++);
        projections.put(CalendarContract.Events.EVENT_LOCATION, i++);
        projections.put(CalendarContract.Events.DESCRIPTION, i++);
        projections.put(CalendarContract.Events.DTSTART, i++);
        projections.put(CalendarContract.Events.DTEND, i++);
        projections.put(CalendarContract.Events.ALL_DAY, i++);
        projections.put(CalendarContract.Events.HAS_ALARM, i);

        // querying
        Cursor cursor = contentResolver.query(uri, projections.keySet().toArray(new String[0]), null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarEvents: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "readCalendarEntries: No entry found");
        } else {
            do {
                int id = cursor.getInt(projections.get(CalendarContract.Events._ID));
                String title = cursor.getString(projections.get(CalendarContract.Events.TITLE));
                int calendarId = cursor.getInt(projections.get(CalendarContract.Events.CALENDAR_ID));
                String location = cursor.getString(projections.get(CalendarContract.Events.EVENT_LOCATION));
                String description = cursor.getString(projections.get(CalendarContract.Events.DESCRIPTION));
                long startDate = cursor.getLong(projections.get(CalendarContract.Events.DTSTART));
                long endDate = cursor.getLong(projections.get(CalendarContract.Events.DTEND));
                boolean allDay = cursor.getInt(projections.get(CalendarContract.Events.ALL_DAY)) == 1;
                boolean hasAlarm = cursor.getInt(projections.get(CalendarContract.Events.HAS_ALARM)) == 1;

                if (title == null || title.length() == 0) {
                    title = context.getString(R.string.no_title);
                }
                CalendarEvent event = new CalendarEvent(id, title, calendarId, location, description, startDate, endDate, allDay, hasAlarm);

                String dateOfEvent = getDate(startDate, getStandardDateFormat());
                boolean isDisrupted = false;
                for (CalendarEntry entry : mEntries) {
                    if (entry.getDate().equals(dateOfEvent)) {
                        entry.addEvent(event);
                        isDisrupted = true;
                        break;
                    }
                }
                if (!isDisrupted) {
                    mEntries.add(new CalendarEntry(dateOfEvent, new ArrayList<>(Arrays.asList(event))));
                }

            } while (cursor.moveToNext());
        }

        // clean up
        if (cursor != null) {
            cursor.close();
        }

        Collections.sort(mEntries);
        return mEntries;
    }

    private static void readCalendarAccounts(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        String[] projection = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        // The indices for the projection array above.
        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        int PROJECTION_DISPLAY_NAME_INDEX = 2;
        int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

        // Run query
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        // Submit the query and get a Cursor object back.
        mColorOffset = 0;
        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarAccounts: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "readCalendarAccounts: No account found");
        } else {
            while (cursor.moveToNext()) {

                // Get the field values
                int calId = cursor.getInt(PROJECTION_ID_INDEX);
                String displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
                String accountName = cursor.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                String ownerName = cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

                Account account = new Account(calId, displayName, accountName, ownerName);
                int color = mDefaultColors.values().toArray(new Integer[0])[mColorOffset++ % mDefaultColors.size()];
                account.setColor(color);
                mAccounts.add(account);
            }

            cursor.close();
        }
    }

    private static void createDefaultColors(Context context) {
        mDefaultColors.put("Red", ContextCompat.getColor(context, R.color.red));
        mDefaultColors.put("Pink", ContextCompat.getColor(context, R.color.pink));
        mDefaultColors.put("Purple", ContextCompat.getColor(context, R.color.purple));
        mDefaultColors.put("Deep Purple", ContextCompat.getColor(context, R.color.deep_purple));
        mDefaultColors.put("Indigo", ContextCompat.getColor(context, R.color.indigo));
        mDefaultColors.put("Blue", ContextCompat.getColor(context, R.color.blue));
        mDefaultColors.put("Light Blue", ContextCompat.getColor(context, R.color.light_blue));
        mDefaultColors.put("Cyan", ContextCompat.getColor(context, R.color.cyan));
        mDefaultColors.put("Teal", ContextCompat.getColor(context, R.color.teal));
        mDefaultColors.put("Green", ContextCompat.getColor(context, R.color.green));
        mDefaultColors.put("Light Green", ContextCompat.getColor(context, R.color.light_green));
        mDefaultColors.put("Yellow", ContextCompat.getColor(context, R.color.yellow));
        mDefaultColors.put("Amber", ContextCompat.getColor(context, R.color.amber));
        mDefaultColors.put("Orange", ContextCompat.getColor(context, R.color.orange));
        mDefaultColors.put("Deep Orange", ContextCompat.getColor(context, R.color.deep_orange));
        mDefaultColors.put("Brown", ContextCompat.getColor(context, R.color.brown));
        mDefaultColors.put("Blue Grey", ContextCompat.getColor(context, R.color.blue_grey));
    }

    private static void readCalendarReminders(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        String[] projection = new String[]{
                CalendarContract.Reminders._ID,                           // 0
                CalendarContract.Reminders.EVENT_ID,                      // 1
                CalendarContract.Reminders.MINUTES,                       // 2
                CalendarContract.Reminders.METHOD                         // 3
        };

        // The indices for the projection array above.
        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_EVENT_ID_INDEX = 1;
        int PROJECTION_MINUTES_INDEX = 2;
        int PROJECTION_METHOD_INDEX = 3;

        // Run query
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Reminders.CONTENT_URI;

        // Submit the query and get a Cursor object back.
        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarReminders: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "readCalendarReminders: No reminder found");
        } else {
            while (cursor.moveToNext()) {
                int _id = cursor.getInt(PROJECTION_ID_INDEX);
                int eventId = cursor.getInt(PROJECTION_EVENT_ID_INDEX);
                int minutes = cursor.getInt(PROJECTION_MINUTES_INDEX);
                int method = cursor.getInt(PROJECTION_METHOD_INDEX);



                Reminder reminder = new Reminder(_id, eventId, minutes, method);
                mReminders.add(reminder);
            }
            cursor.close();
        }
    }

    private static void readCalendarEventAttendees(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
        String[] projection = new String[]{
                CalendarContract.Attendees._ID,                             // 0
                CalendarContract.Attendees.EVENT_ID,                        // 1
                CalendarContract.Attendees.ATTENDEE_NAME,                   // 2
                CalendarContract.Attendees.ATTENDEE_EMAIL,                  // 3
                CalendarContract.Attendees.ATTENDEE_STATUS,                 // 4
                CalendarContract.Attendees.ATTENDEE_RELATIONSHIP            // 5
        };

        // The indices for the projection array above.
        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_EVENT_ID_INDEX = 1;
        int PROJECTION_ATTENDEE_NAME_INDEX = 2;
        int PROJECTION_ATTENDEE_EMAIL_INDEX = 3;
        int PROJECTION_ATTENDEE_STATUS_INDEX = 4;
        int PROJECTION_ATTENDEE_RELATIONSHIP_INDEX = 5;

        // Run query
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Attendees.CONTENT_URI;

        // Submit the query and get a Cursor object back.
        Cursor cursor = cr.query(uri, projection, null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarEventAttendees: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "readCalendarEventAttendees: No attendee found");
        } else {
            while (cursor.moveToNext()) {
                int _id = cursor.getInt(PROJECTION_ID_INDEX);
                int eventId = cursor.getInt(PROJECTION_EVENT_ID_INDEX);
                String name = cursor.getString(PROJECTION_ATTENDEE_NAME_INDEX);
                String email = cursor.getString(PROJECTION_ATTENDEE_EMAIL_INDEX);
                int status = cursor.getInt(PROJECTION_ATTENDEE_STATUS_INDEX);
                int relationship = cursor.getInt(PROJECTION_ATTENDEE_RELATIONSHIP_INDEX);

                Attendee attendee = new Attendee(_id, eventId, name, email, status, relationship);
                mAttendees.add(attendee);
            }
            cursor.close();
        }
    }

    public static ArrayList<CalendarEntry> getAllEntries() {
        return mEntries;
    }

    public static String getAccountDisplayName(int id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account.getDisplayName();
            }
        }
        return "";
    }

    public static int getAccountColor(int id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account.getColor();
            }
        }
        return mDefaultColors.values().toArray(new Integer[0])[mDefaultColors.size() - 1];
    }

    public static LinkedHashMap<String, Integer> getAllColors() {
        return mDefaultColors;
    }
    
    public static int getReminderMinutes(int id) {
        for (Reminder reminder : mReminders) {
            if (reminder.getEventId() == id) {
                return reminder.getMinutes();
            }
        }
        return -1;
    }

    public static ArrayList<Attendee> getEventAttendees(int id) {
        ArrayList<Attendee> attendees = new ArrayList<>();
        for (Attendee attendee: mAttendees) {
            if (attendee.getEventId() == id) {
                attendees.add(attendee);
            }
        }
        return attendees;
    }

    /**
     *
     * @param date must be in "yyyy/MM/dd" format
     * @return entry with the specified date, null if there is no entry matches the date
     */
    public static CalendarEntry findEntryWithDate(String date) {
        // TODO: 9/1/17 this is temporary, must be fixed in the future for better performance (consider switching to binary search)
        assert mEntries != null;
        assert date.length() == 10;
        for (int i = 0; i< mEntries.size(); i++) {
            if (mEntries.get(i).getDate().equals(date)) {
                return mEntries.get(i);
            }
        }
        return null;
    }

    public static String getStandardDateFormat() {
        return "yyyy/MM/dd";
    }

    public static String getVisibleDateFormat() {
        return "EEE, MMM d, yyyy";
    }

    public static String getStandardTimeFormat() {
        return "hh:mm a";
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    public static String getDate(String standardDate, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(getStandardDateFormat(), Locale.getDefault());
//        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        formatter.setTimeZone(TimeZone.getDefault());
        try {
            Date parsedDate = formatter.parse(standardDate);
            formatter = new SimpleDateFormat(dateFormat, Locale.getDefault());
//            formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
            formatter.setTimeZone(TimeZone.getDefault());
            return formatter.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getVisibleTime(int minutes, Context context) {

        StringBuilder formattedTime = new StringBuilder();

        int _minutes = minutes;
        String format = "%d %s ";

        int weeks = (int) (TimeUnit.MINUTES.toDays(_minutes) / 7);
        if (weeks > 0) {
            formattedTime.append(String.format(format, weeks, weeks == 1 ? context.getString(R.string.week) : context.getString(R.string.weeks)));
            _minutes -= TimeUnit.DAYS.toMinutes(weeks) * 7;
        }

        int days = (int) (TimeUnit.MINUTES.toDays(_minutes));
        if (days > 0) {
            formattedTime.append(String.format(format, days, days == 1 ? context.getString(R.string.day) : context.getString(R.string.days)));
            _minutes -= TimeUnit.DAYS.toMinutes(days);
        }

        int hours = (int) (TimeUnit.MINUTES.toHours(_minutes));
        if (hours > 0) {
            formattedTime.append(String.format(format, hours, hours == 1 ? context.getString(R.string.hour) : context.getString(R.string.hours)));
            _minutes -= TimeUnit.HOURS.toMinutes(hours);
        }

        if (_minutes > 0) {
            formattedTime.append(String.format(format, _minutes, _minutes == 1 ? context.getString(R.string.minute) : context.getString(R.string.minutes)));
        }

        return formattedTime.toString();
    }
}
