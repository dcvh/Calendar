package tcd.training.com.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.CalendarContract;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Event;

/**
 * Created by cpu10661-local on 9/18/17.
 */

public class EventChangesReceiver extends BroadcastReceiver {


    private static final String TAG = EventChangesReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_PROVIDER_CHANGED)) {

            new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... voids) {

                    final String selection =
                            CalendarContract.Events.DIRTY + "=" + 1 + " OR "
                            + CalendarContract.Events.DELETED + "=" + 1;
                    ArrayList<Event> events = DataUtils.readCalendarEvents(selection, context);

                    if (events.size() != 0) {
                        sendUpdateBroadcast(MainActivity.UPDATE_ADD, context);
                    } else {
                        int numberOfEvents = DataUtils.getNumberOfEvents();

                        DataUtils.readCalendarEventsInfo(context);

                        int difference = DataUtils.getNumberOfEvents() - numberOfEvents;
                        if (difference != 0) {
                            sendUpdateBroadcast(MainActivity.UPDATE_REMOVE, context);
                        }

                        Log.d(TAG, "old: " + numberOfEvents);
                        Log.d(TAG, "new: " + DataUtils.getNumberOfEvents());
                    }

                    return null;

                }


            }.execute();
        }
    }

    private void sendUpdateBroadcast(int type, Context context) {
        Intent updateEvent = new Intent(MainActivity.UPDATE_EVENT_ACTION);
        updateEvent.putExtra(MainActivity.ARG_UPDATE_TYPE, type);
//                difference < 0 ? MainActivity.UPDATE_REMOVE : MainActivity.UPDATE_ADD);
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateEvent);
    }
}
