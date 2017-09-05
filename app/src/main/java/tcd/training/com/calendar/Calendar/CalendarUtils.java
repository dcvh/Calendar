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
import java.util.HashMap;
import java.util.LinkedHashMap;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarUtils {

    private static final String TAG = CalendarUtils.class.getSimpleName();

    private static final HashMap<String, Integer> mDefaultColors = new HashMap<>();

    private static ArrayList<CalendarEntry> mEntries;
    private static ArrayList<Account> mAccounts;
    private static int mColorOffset = 0;

    public static ArrayList<CalendarEntry> getAllEntries() {
        return mEntries;
    }

    public static ArrayList<CalendarEntry> readCalendarEntries(Context context) {

        mEntries = new ArrayList<>();
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

        // querying
        Cursor cursor = contentResolver.query(uri, projections.keySet().toArray(new String[0]), null, null, null);
        if (cursor == null) {
            Log.e(TAG, "readCalendarEvents: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(context, R.string.no_calendar_events_error, Toast.LENGTH_SHORT).show();
        } else {
            do {
                long id = cursor.getLong(projections.get(CalendarContract.Events._ID));
                String title = cursor.getString(projections.get(CalendarContract.Events.TITLE));
                long calendarId = cursor.getLong(projections.get(CalendarContract.Events.CALENDAR_ID));
                String location = cursor.getString(projections.get(CalendarContract.Events.EVENT_LOCATION));
                String description = cursor.getString(projections.get(CalendarContract.Events.DESCRIPTION));
                long startDate = cursor.getLong(projections.get(CalendarContract.Events.DTSTART));
                long endDate = cursor.getLong(projections.get(CalendarContract.Events.DTEND));
                boolean allDay = cursor.getInt(projections.get(CalendarContract.Events.ALL_DAY)) == 1;

                if (title.length() == 0) {
                    title = context.getString(R.string.no_title);
                }

                CalendarEvent event = new CalendarEvent(id, title, calendarId, location, description, startDate, endDate, allDay);

                String dateOfEvent = getDate(startDate, "yyyy/MM/dd");
                for (CalendarEntry entry : mEntries) {
                    if (entry.getDate().equals(dateOfEvent)) {
                        entry.addEvent(event);
                        continue;
                    }
                }
                CalendarEntry newEventDate = new CalendarEntry(dateOfEvent, new ArrayList<>(Arrays.asList(event)));
                mEntries.add(newEventDate);
            } while (cursor.moveToNext());
        }

        // clean up
        if (cursor != null) {
            cursor.close();
        }

        return mEntries;
    }

    public static void readCalendarAccounts(Context context) {

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
        
        mAccounts = new ArrayList<>();
        createDefaultColors(context);

        // Submit the query and get a Cursor object back.
        Cursor cursor = cr.query(uri, projection, null, null, null);
        while (cursor.moveToNext()) {
            long calID = 0;
            String displayName = null;
            String accountName = null;
            String ownerName = null;

            // Get the field values
            calID = cursor.getLong(PROJECTION_ID_INDEX);
            displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);
            accountName = cursor.getString(PROJECTION_ACCOUNT_NAME_INDEX);
            ownerName = cursor.getString(PROJECTION_OWNER_ACCOUNT_INDEX);

            Account account = new Account(calID, displayName, accountName, ownerName);
            int color = mDefaultColors.values().toArray(new Integer[0])[mColorOffset++ % mDefaultColors.size()];
            Log.e(TAG, "readCalendarAccounts: " + color);
            account.setColor(color);
            mAccounts.add(account);
        }

        if (cursor != null) {
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

    public static String getAccountDisplayName(long id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account.getDisplayName();
            }
        }
        return "";
    }

    public static String getAccountName(long id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account.getAccountName();
            }
        }
        return "";
    }

    public static int getAccountColor(long id) {
        for (Account account : mAccounts) {
            if (account.getId() == id) {
                return account.getColor();
            }
        }
        return Resources.getSystem().getColor(R.color.colorAccent);
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

    public static String getDate(long milliSeconds, String dateFormat) {
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }
}
