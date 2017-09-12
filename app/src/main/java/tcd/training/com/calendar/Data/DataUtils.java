package tcd.training.com.calendar.Data;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class DataUtils {

    private static final String TAG = DataUtils.class.getSimpleName();

    private static final LinkedHashMap<String, Integer> mDefaultColors = new LinkedHashMap<>();
    private static ArrayList<Entry> mEntries;
    private static ArrayList<Account> mAccounts;
    private static ArrayList<Reminder> mReminders;
    private static ArrayList<Attendee> mAttendees;


    public static void readCalendarEventsInfo(Context context) {
        mEntries = new ArrayList<>();
        mAccounts = new ArrayList<>();
        mReminders = new ArrayList<>();
        mAttendees = new ArrayList<>();

        createDefaultColors(context);
        readCalendarAccounts(context);

        readCalendarEntries(context);

        readCalendarReminders(context);

        readCalendarEventAttendees(context);
    }

    private static ArrayList<Entry> readCalendarEntries(Context context) {

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
        projections.put(CalendarContract.Events.EVENT_TIMEZONE, i++);
        projections.put(CalendarContract.Events.DTSTART, i++);
        projections.put(CalendarContract.Events.DTEND, i++);
        projections.put(CalendarContract.Events.ALL_DAY, i++);
        projections.put(CalendarContract.Events.HAS_ALARM, i++);
        projections.put(CalendarContract.Events.RRULE, i++);
        projections.put(CalendarContract.Events.DISPLAY_COLOR, i++);
        projections.put(CalendarContract.Events.AVAILABILITY, i++);

        // querying
        Cursor cursor = contentResolver.query(uri, projections.keySet().toArray(new String[0]), null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarEvents: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "readCalendarEntries: No entry found");
        } else {
            do {
                int calendarId = cursor.getInt(projections.get(CalendarContract.Events.CALENDAR_ID));
                if (getAccountDisplayName(calendarId).length() == 0) {
                    continue;
                }

                int id = cursor.getInt(projections.get(CalendarContract.Events._ID));
                String title = cursor.getString(projections.get(CalendarContract.Events.TITLE));
                String location = cursor.getString(projections.get(CalendarContract.Events.EVENT_LOCATION));
                String description = cursor.getString(projections.get(CalendarContract.Events.DESCRIPTION));
                String timezone = cursor.getString(projections.get(CalendarContract.Events.EVENT_TIMEZONE));
                long startDate = cursor.getLong(projections.get(CalendarContract.Events.DTSTART));
                long endDate = cursor.getLong(projections.get(CalendarContract.Events.DTEND));
                boolean allDay = cursor.getInt(projections.get(CalendarContract.Events.ALL_DAY)) == 1;
                boolean hasAlarm = cursor.getInt(projections.get(CalendarContract.Events.HAS_ALARM)) == 1;
                String rRule = cursor.getString(projections.get(CalendarContract.Events.RRULE));
                int displayColor = cursor.getInt(projections.get(CalendarContract.Events.DISPLAY_COLOR));
                int availability = cursor.getInt(projections.get(CalendarContract.Events.DISPLAY_COLOR));

                if (title == null || title.length() == 0) {
                    title = context.getString(R.string.no_title);
                }

                addEventToEntries(new Event(id, title, calendarId, location, description, timezone,
                        startDate, endDate, allDay, hasAlarm, rRule, displayColor, availability));

            } while (cursor.moveToNext());
        }

        // clean up
        if (cursor != null) {
            cursor.close();
        }

        return mEntries;
    }

    private static void addEventToEntries(Event event) {

        if (getAccountDisplayName(event.getCalendarId()).length() == 0) {
            return;
        }

        int index = findEntryIndexWithDate(event.getStartDate());
        if (index >= 0) {
            mEntries.get(index).addEvent(event);
        } else {
            index = -index - 1;
            Entry newEntry = new Entry(event.getStartDate(), new ArrayList<>(Arrays.asList(event)));
            mEntries.add(index, newEntry);
        }
    }

    private static void readCalendarAccounts(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.

        ArrayList<String> projection = new ArrayList<>(Arrays.asList(
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        ));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            projection.add(CalendarContract.Calendars.IS_PRIMARY);
        }

        // The indices for the projection array above.
        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        int PROJECTION_DISPLAY_NAME_INDEX = 2;
        int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
        int PROJECTION_IS_PRIMARY_INDEX = 3;

        // Run query
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        // Submit the query and get a Cursor object back.
        Cursor cursor = cr.query(uri, projection.toArray(new String[0]), null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarAccounts: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "readCalendarAccounts: No account found");
        } else {
            while (cursor.moveToNext()) {

                // Get the field values
                int id = cursor.getInt(PROJECTION_ID_INDEX);
                String displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
                String accountName = cursor.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                String ownerName = cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
                boolean isPrimary = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    isPrimary = cursor.getInt(PROJECTION_IS_PRIMARY_INDEX) == 1;
                }

                Account account = new Account(id, displayName, accountName, ownerName, isPrimary);
                if (isThisDuplicateHoliday(account)) {
                    continue;
                }
                mAccounts.add(account);
            }

            cursor.close();
        }
    }

    private static boolean isThisDuplicateHoliday(Account account) {
        if (!account.getDisplayName().startsWith("Holidays in ")) {
            return false;
        }
        for (Account curAccount : mAccounts) {
            if (curAccount.getDisplayName().equals(account.getDisplayName())) {
                return true;
            }
        }
        return false;
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



    public static ArrayList<Entry> getAllEntries() {
        return mEntries;
    }

    public static ArrayList<Entry> getEntriesInYear(int year) {
        ArrayList<Entry> entries = new ArrayList<>();

        Calendar cal = Calendar.getInstance();
        for (Entry entry : mEntries) {
            cal.setTimeInMillis(entry.getTime());
            if (cal.get(Calendar.YEAR) == year) {
                entries.add(entry);
            }
        }
        return entries;
    }

    public static ArrayList<Entry> getEntriesBetween(long start, long end) {
        ArrayList<Entry> entries = new ArrayList<>();

        for (Entry entry : mEntries) {
            if (entry.getTime() > start && entry.getTime() < end) {
                entries.add(entry);
            }
        }

        return entries;
    }

    public static LinkedHashMap<String, Integer> getAllColors() {
        return mDefaultColors;
    }

    public static int getPrimaryAccountId() {
        for (Account account : mAccounts) {
            if (account.isPrimary()) {
                return account.getId();
            }
        }
        return mAccounts.get(0).getId();
    }

    public static String getAccountDisplayName(int id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account.getDisplayName();
            }
        }
        return "";
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
        for (Attendee attendee : mAttendees) {
            if (attendee.getEventId() == id) {
                attendees.add(attendee);
            }
        }
        return attendees;
    }

    /**
     *
     * @param millis  the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @return entry with the specified date, null if there is no entry matches the date
     */
    public static Entry findEntryWithDate(long millis) {
        int index = findEntryIndexWithDate(millis);
        return index < 0 ? null : mEntries.get(index);
    }

    private static int findEntryIndexWithDate(final long millis) {
//            int low = 0;
//            int high = mEntries.size() - 1;
//            while (low <= high) {
//                int mid = low + (high - low) / 2;
//                if (TimeUtils.compareDay(millis, mEntries.get(mid).getTime()) < 0) {
//                    high = mid - 1;
//                } else if (TimeUtils.compareDay(millis, mEntries.get(mid).getTime()) > 0) {
//                    low = mid + 1;
//                } else {
//                    return mid;
//                }
//            }
        Comparator<Entry> comparator = new Comparator<Entry>() {
            @Override
            public int compare(Entry entry, Entry t1) {
                return TimeUtils.compareDay(entry.getTime(), t1.getTime());
            }
        };
        Entry entry = new Entry(millis, null);
        return Collections.binarySearch(mEntries, entry, comparator);
    }

    public static long addEvent(Event event, Reminder reminder, Context context) {

        ContentResolver cr = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.TITLE, event.getTitle());
        values.put(CalendarContract.Events.CALENDAR_ID, event.getCalendarId());
        values.put(CalendarContract.Events.EVENT_LOCATION, event.getLocation());
        values.put(CalendarContract.Events.DESCRIPTION, event.getDescription());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, event.getTimeZone());
        values.put(CalendarContract.Events.DTSTART, event.getStartDate());
        values.put(CalendarContract.Events.DTEND, event.getEndDate());
        values.put(CalendarContract.Events.ALL_DAY, event.isAllDay());
        values.put(CalendarContract.Events.HAS_ALARM, event.hasAlarm());
        values.put(CalendarContract.Events.RRULE, event.getRRule());
        if (event.getDisplayColor() != -1) {
            values.put(CalendarContract.Events.EVENT_COLOR, event.getDisplayColor());
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }

        addEventToEntries(event);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        long id = uri != null ? Long.parseLong(uri.getLastPathSegment()) : -1;

        if (event.hasAlarm() && id != -1) {
            values = new ContentValues();
            values.put(CalendarContract.Reminders.EVENT_ID, id);
            values.put(CalendarContract.Reminders.MINUTES, reminder.getMinutes());
            values.put(CalendarContract.Reminders.METHOD, reminder.getMethod());

            cr.insert(CalendarContract.Reminders.CONTENT_URI, values);
            mReminders.add(reminder);
        }

        return id;
    }
}
