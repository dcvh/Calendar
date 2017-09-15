package tcd.training.com.calendar.ReminderTask;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Data.TimeUtils;

/**
 * Created by cpu10661-local on 9/7/17.
 */

public class ReminderJobService extends JobService {

    private static final String TAG = ReminderJobService.class.getSimpleName();

    private AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {

        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                Bundle bundle = job.getExtras();

                if (bundle != null) {
                    int id = bundle.getInt(ReadTodayRemindersJobService.ARG_EVENT_ID);
                    String title = bundle.getString(ReadTodayRemindersJobService.ARG_EVENT_TITLE);
                    long startTime = bundle.getLong(ReadTodayRemindersJobService.ARG_EVENT_START_TIME);
                    long priority = bundle.getInt(ReadTodayRemindersJobService.ARG_EVENT_PRIORITY);

                    if (priority == AddEventActivity.PRIORITY_NOTIFICATION) {
                        Log.e(TAG, "doInBackground: notification");
                        ReminderUtils.showReminderNotification(
                                ReminderJobService.this,
                                title,
                                TimeUtils.getFormattedDate(startTime, TimeUtils.getStandardTimeFormat())
                        );
                    } else if (priority == AddEventActivity.PRIORITY_POPUP) {
                        Log.e(TAG, "doInBackground: popup");
                        ReminderUtils.showReminderPopup(
                                ReminderJobService.this,
                                title,
                                TimeUtils.getFormattedDate(startTime, TimeUtils.getStandardTimeFormat())
                        );
                    }
                } else {
                    Log.e(TAG, "doInBackground: There was a problem retrieving the event data");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                jobFinished(job, false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        if (mBackgroundTask != null) {
            mBackgroundTask.cancel(true);
        }
        return true;
    }
}
