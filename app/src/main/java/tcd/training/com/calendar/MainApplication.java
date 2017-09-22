package tcd.training.com.calendar;

import android.app.Application;
import android.content.Context;

import tcd.training.com.calendar.Settings.LocaleHelper;

/**
 * Created by cpu10661 on 9/22/17.
 */

public class MainApplication extends Application {

    private static Application mApplication;

    public static Context getContext() {
        return mApplication.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

}
