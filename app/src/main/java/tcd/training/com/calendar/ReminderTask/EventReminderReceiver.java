package tcd.training.com.calendar.ReminderTask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.util.Log;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.R;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by ADMIN on 17/09/2017.
 */

public class EventReminderReceiver extends BroadcastReceiver {

    private static final String TAG = EventReminderReceiver.class.getSimpleName();

    private static final int VIBRATE_DURATION = 150;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equalsIgnoreCase(CalendarContract.ACTION_EVENT_REMINDER)) {

            // notify on this device setting
            boolean notifyOnThisDevice = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.pref_key_notify_on_this_device), true);
            if (!notifyOnThisDevice) {
                return;
            }

            // get the event
            Uri uri = intent.getData();
            String alertTime = uri.getLastPathSegment();
            int eventId = getEventId(alertTime, context);

            Event event = DataUtils.findEventById(eventId);
            if (event == null) {
                DataUtils.readCalendarEventsInfo(context);
                event = DataUtils.findEventById(eventId);
                if (event == null) {
                    Log.e(TAG, "Event ID: " + eventId);
                    return;
                }
            }

            // vibrate
            boolean vibrate = PreferenceManager.getDefaultSharedPreferences(context)
                    .getBoolean(context.getString(R.string.pref_key_vibrate), false);
            if (vibrate) {
                Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                } else {
                    vibrator.vibrate(VIBRATE_DURATION);
                }
            }

            // show reminder
            String time = TimeUtils.getFormattedDate(event.getStartDate(), TimeUtils.getStandardTimeFormat());
            switch (event.getPriority()) {
                case AddEventActivity.PRIORITY_NOTIFICATION:
                    ReminderUtils.showReminderNotification(context, event.getTitle(), time);
                    break;
                case AddEventActivity.PRIORITY_POPUP:
                    ReminderUtils.showReminderPopup(context, event.getTitle(), time);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown method");
            }
        }
    }

    private int getEventId(String alertTime, Context context) {
        String selection = CalendarContract.CalendarAlerts.ALARM_TIME + "=?";
        Cursor cursor = context.getContentResolver().query(CalendarContract.CalendarAlerts.CONTENT_URI_BY_INSTANCE,
                new String[]{CalendarContract.CalendarAlerts.EVENT_ID},
                selection,
                new String[]{alertTime},
                null);

        int eventId = -1;
        if (cursor == null) {
            Log.e(TAG, "getEventId: There was a problem handling the cursor");
        } else if (!cursor.moveToFirst()) {
            Log.d(TAG, "getEventId: No event ID found");
        } else {
            eventId = cursor.getInt(0);
        }

        if (cursor != null) {
            cursor.close();
        }

        return eventId;
    }
}
