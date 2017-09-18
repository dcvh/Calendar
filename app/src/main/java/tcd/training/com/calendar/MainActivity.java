package tcd.training.com.calendar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.ReminderTask.EventPopup;
import tcd.training.com.calendar.ReminderTask.ReminderUtils;
import tcd.training.com.calendar.Settings.SettingsActivity;
import tcd.training.com.calendar.ContentView.Day.DayViewFragment;
import tcd.training.com.calendar.ContentView.Month.MonthViewFragment;
import tcd.training.com.calendar.ContentView.Schedule.ScheduleViewFragment;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    public static final String ARG_ENTRIES_LIST = "entriesList";

    public static final String UPDATE_CONTENT_VIEW_ACTION = "updateContentViewAction";
    public static final String ARG_CONTENT_VIEW_TYPE = "contentViewType";
    public static final String ARG_TIME_IN_MILLIS = "timeInMillis";

    public static final String UPDATE_MONTH_ACTION = "updateMonthAction";
    public static final String ARG_CALENDAR = "calendar";

    public static final String UPDATE_EVENT_ACTION = "updateEventAction";
    public static final String ARG_UPDATE_TYPE = "updateType";
    public static final int UPDATE_REMOVE = 0;
    public static final int UPDATE_ADD = 1;

    private static final int RC_CALENDAR_PERMISSION = 1;
    private static final int RC_SETTINGS = 2;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    private FragmentManager mFragmentManager;
    private Fragment mCurrentFragment;
    private BroadcastReceiver mUpdateContentViewReceiver;
    private BroadcastReceiver mUpdateMonthReceiver;
    private BroadcastReceiver mUpdateEventReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUiComponents();
        initializeBasicComponents();

        readCalendarEntries();

        ReminderUtils.clearAllNotifications(this);

        if (mCurrentFragment instanceof ContentViewBehaviors) {
            ((ContentViewBehaviors)mCurrentFragment).addEvent();
        }

        // TODO: 9/18/17 handle custom event
        // https://developer.android.com/reference/android/provider/CalendarContract.html#ACTION_HANDLE_CUSTOM_EVENT
    }

    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateContentViewReceiver, new IntentFilter(UPDATE_CONTENT_VIEW_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateMonthReceiver, new IntentFilter(UPDATE_MONTH_ACTION));
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateEventReceiver, new IntentFilter(UPDATE_EVENT_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateContentViewReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateMonthReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mUpdateEventReceiver);
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
                if (mCurrentFragment instanceof ContentViewBehaviors) {
                    ((ContentViewBehaviors)mCurrentFragment).scrollToToday();
                }
                return true;
            case R.id.action_refresh:
                readCalendarEntries();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeUiComponents() {
        // status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

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

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);
    }

    private void initializeBasicComponents() {
        mFragmentManager = getSupportFragmentManager();

        mUpdateContentViewReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int navMenuId = intent.getIntExtra(ARG_CONTENT_VIEW_TYPE, R.id.nav_schedule);
                final long timeInMillis = intent.getLongExtra(ARG_TIME_IN_MILLIS, Calendar.getInstance().getTimeInMillis());
                selectItemNavigation(navMenuId);

                if (mCurrentFragment instanceof DayViewFragment) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            ((DayViewFragment)mCurrentFragment).scrollTo(timeInMillis, false);
                            super.onPostExecute(aVoid);
                        }
                    }.execute();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateContentViewReceiver, new IntentFilter(UPDATE_CONTENT_VIEW_ACTION));

        mUpdateMonthReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Calendar calendar = (Calendar) intent.getSerializableExtra(ARG_CALENDAR);
                String month = TimeUtils.getFormattedDate(calendar.getTimeInMillis(), "MMMM");
                getSupportActionBar().setTitle(month);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateMonthReceiver, new IntentFilter(UPDATE_MONTH_ACTION));

        mUpdateEventReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int type = intent.getIntExtra(ARG_UPDATE_TYPE, 0);
                switch (type) {
                    case UPDATE_ADD:
                        if (mCurrentFragment instanceof ContentViewBehaviors) {
                            ((ContentViewBehaviors)mCurrentFragment).addEvent();
                        }
                        break;
                    case UPDATE_REMOVE:
                        if (mCurrentFragment instanceof ContentViewBehaviors) {
                            ((ContentViewBehaviors)mCurrentFragment).removeEvent();
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown update type");
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mUpdateEventReceiver, new IntentFilter(UPDATE_EVENT_ACTION));
    }

    private void replaceFragment(Class fragmentClass) {
        try {
            assert fragmentClass != null;
            mCurrentFragment = (Fragment) fragmentClass.newInstance();
            mFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.fl_content, mCurrentFragment)
                    .commitAllowingStateLoss();
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, R.string.no_calendar_events_error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mDialog.dismiss();
                mDialog = null;
                selectItemNavigation(R.id.nav_schedule);
            }

        }.execute();
    }

    private void selectItemNavigation(int id) {
        mNavigationView.setCheckedItem(id);
        mNavigationView.getMenu().performIdentifierAction(id, 0);
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
                startActivityForResult(intent, RC_SETTINGS);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SETTINGS:
                if (resultCode == RESULT_OK) {
//                    if (mCurrentFragment instanceof ContentViewBehaviors) {
//                        ((ContentViewBehaviors)mCurrentFragment).invalidate();
//                    }
                    replaceFragment(mCurrentFragment.getClass());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
