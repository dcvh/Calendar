package tcd.training.com.calendar.ReminderTask;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 9/7/17.
 */

public class ReminderJobService extends JobService {

    private static final String TAG = ReminderJobService.class.getSimpleName();

    private static final int VIBRATE_DURATION = 150;

    private AsyncTask<Void, Void, Void> mBackgroundTask;

    @Override
    public boolean onStartJob(final JobParameters job) {

        mBackgroundTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {

                Bundle bundle = job.getExtras();

                if (bundle != null) {
                    int id = bundle.getInt(ReadTodayRemindersJobService.ARG_EVENT_ID);
                    String title = bundle.getString(ReadTodayRemindersJobService.ARG_EVENT_TITLE);
                    long startTime = bundle.getLong(ReadTodayRemindersJobService.ARG_EVENT_START_TIME);
                    long priority = bundle.getInt(ReadTodayRemindersJobService.ARG_EVENT_PRIORITY);

                    boolean vibrate = PreferenceManager.getDefaultSharedPreferences(ReminderJobService.this)
                            .getBoolean(getString(R.string.pref_key_vibrate), false);
                    if (vibrate) {
                        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        if (Build.VERSION.SDK_INT >= 26) {
                            vibrator.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION, VibrationEffect.DEFAULT_AMPLITUDE));
                        } else {
                            vibrator.vibrate(VIBRATE_DURATION);
                        }
                    }

                    if (priority == AddEventActivity.PRIORITY_NOTIFICATION) {
                        ReminderUtils.showReminderNotification(
                                ReminderJobService.this,
                                title,
                                DateUtils.formatDateTime(ReminderJobService.this, startTime, DateUtils.FORMAT_SHOW_TIME)
                        );
                    } else if (priority == AddEventActivity.PRIORITY_POPUP) {
                        ReminderUtils.showReminderPopup(
                                ReminderJobService.this,
                                title,
                                DateUtils.formatDateTime(ReminderJobService.this, startTime, DateUtils.FORMAT_SHOW_TIME)
                        );
                    }
                } else {
                    Log.e(TAG, "doInBackground: There was a problem retrieving the event data");
                }

                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
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
