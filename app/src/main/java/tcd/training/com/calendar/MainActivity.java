package tcd.training.com.calendar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Calendar;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.ReminderTask.ReminderUtils;
import tcd.training.com.calendar.Settings.SettingsActivity;
import tcd.training.com.calendar.ContentView.Day.DayViewFragment;
import tcd.training.com.calendar.ContentView.Month.MonthViewFragment;
import tcd.training.com.calendar.ContentView.Schedule.ScheduleViewFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String ARG_ENTRIES_LIST = "entriesList";
    public static final String UPDATE_MONTH_ACTION = "updateMonthAction";
    public static final String ARG_CALENDAR = "calendar";
    private static final int RC_CALENDAR_PERMISSION = 1;

    private DrawerLayout mDrawerLayout;

    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUiComponents();
        initializeBasicComponents();

        readCalendarEntries();

        ReminderUtils.clearAllNotifications(this);
        ReminderUtils.scheduleForReadingReminders(this);

//        Intent intent = new Intent(this, AddEventActivity.class);
//        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(UPDATE_MONTH_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_today:
                scrollToToday();
                return true;
            case R.id.action_refresh:
                readCalendarEntries();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void scrollToToday() {
        if (mCurrentFragment instanceof ScheduleViewFragment) {
            ((ScheduleViewFragment) mCurrentFragment).scrollToToday();
        } else if (mCurrentFragment instanceof  MonthViewFragment) {
            ((MonthViewFragment) mCurrentFragment).scrollToToday();
        } else if (mCurrentFragment instanceof  DayViewFragment) {
            ((DayViewFragment) mCurrentFragment).scrollToToday();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_schedule: replaceFragment(ScheduleViewFragment.class); break;
            case R.id.nav_day: replaceFragment(DayViewFragment.class); break;
            case R.id.nav_week: replaceFragment(ScheduleViewFragment.class); break;
            case R.id.nav_month: replaceFragment(MonthViewFragment.class); break;
            case R.id.nav_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_help_feedback:
                break;
            default:
                throw new UnsupportedOperationException("Unknown selected item: " + item.getTitle());
        }

        if (id != R.id.nav_settings && id != R.id.nav_help_feedback) {
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initializeUiComponents() {
        // toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // floating action button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addEventIntent = new Intent(MainActivity.this, AddEventActivity.class);
                startActivity(addEventIntent);
            }
        });

        // navigation drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void initializeBasicComponents() {
        mFragmentManager = getSupportFragmentManager();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Calendar calendar = (Calendar) intent.getSerializableExtra(ARG_CALENDAR);
                String month = TimeUtils.getFormattedDate(calendar.getTimeInMillis(), "MMMM");
                getSupportActionBar().setTitle(month);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(UPDATE_MONTH_ACTION));
    }

    private void replaceFragment(Class fragmentClass) {
        try {
            assert fragmentClass != null;
            mCurrentFragment = (Fragment) fragmentClass.newInstance();
            mFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fl_content, mCurrentFragment)
                    .commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void readCalendarEntries() {
        if (!requestWriteCalendarPermission()) {
            return;
        }

        new AsyncTask<Void, Void, Void>() {
            private ProgressDialog mDialog;

            @Override
            protected void onPreExecute() {
                mDialog = new ProgressDialog(MainActivity.this);
                mDialog.setMessage(getString(R.string.wait_load_events_message));
                mDialog.setCancelable(false);
                mDialog.setInverseBackgroundForced(false);
                mDialog.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                DataUtils.readCalendarEventsInfo(MainActivity.this);
                if (DataUtils.getAllEntries().size() == 0) {
                    Toast.makeText(MainActivity.this, R.string.no_calendar_events_error, Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "doInBackground: " + DataUtils.getAllEntries().size());
                    for (Entry entry : DataUtils.getAllEntries()) {
                        for (Event event : entry.getEvents()) {
                            Log.e(TAG, "doInBackground: " + event.getTitle());
                            Log.e(TAG, "doInBackground: " + TimeUtils.getFormattedDate(event.getStartDate(), "dd/MM/yyyy"));
                        }
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mDialog.dismiss();
                mDialog = null;
                replaceFragment(ScheduleViewFragment.class);
            }

        }.execute();
    }

    private boolean requestWriteCalendarPermission() {
        final String[] permissions = new String[] { Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                R.string.require_calendar_permission_error, Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.grant, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissions, RC_CALENDAR_PERMISSION);
                            }
                        });
                        snackbar.show();
                    } else {
                        ActivityCompat.requestPermissions(this, permissions, RC_CALENDAR_PERMISSION);
                    }
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RC_CALENDAR_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readCalendarEntries();
                } else {
                    requestWriteCalendarPermission();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
