package tcd.training.com.calendar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;

import tcd.training.com.calendar.Settings.SettingsActivity;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private final static String TAG = MainActivity.class.getSimpleName();

    private static final int RC_CALENDAR_PERMISSION = 1;

    private DrawerLayout mDrawerLayout;

    RecyclerView.LayoutManager mLayoutManager;
    private CalendarEntriesAdapter mDatesAdapter;
    private ArrayList<CalendarEntry> mCalendarEntriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUiComponents();

        readCalendarEventDates();
    }

    @Override
    public void onBackPressed() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
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
                String today = CalendarUtils.getDate(Calendar.getInstance().getTimeInMillis(), "yyyy/MM/dd");
                for (int i = 0; i < mCalendarEntriesList.size(); i++) {
                    String date = mCalendarEntriesList.get(i).getDate();
                    if (date.equals(today)) {
                        mLayoutManager.scrollToPosition(i);
                    } else if (date.compareTo(today) < 0) {
                        break;
                    }
                }
                return true;
            case R.id.action_refresh:
                readCalendarEventDates();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_schedule) {

        } else if (id == R.id.nav_day) {

        } else if (id == R.id.nav_month) {

        } else if (id == R.id.nav_week) {

        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_help_feedback) {

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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        // recycler view
        RecyclerView eventsRecyclerView = (RecyclerView) findViewById(R.id.rv_events_list);
        mLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setLayoutManager(mLayoutManager);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mCalendarEntriesList = new ArrayList<>();
        mDatesAdapter = new CalendarEntriesAdapter(this, mCalendarEntriesList);
        eventsRecyclerView.setAdapter(mDatesAdapter);
    }

    private void readCalendarEventDates() {
        if (!requestWriteCalendarPermission()) {
            return;
        }

        ArrayList<CalendarEntry> entriesList = CalendarUtils.readCalendarEvent(this);
        if (entriesList.size() > 0) {
            mCalendarEntriesList.addAll(entriesList);
            Collections.sort(mCalendarEntriesList);
            mDatesAdapter.notifyDataSetChanged();
        } else {
//            for (int i = 1; i < 5; i++) {
//                ArrayList<CalendarEvent> events = new ArrayList<>();
//                for (int j = 0; j < i; j++) {
//                    events.add(new CalendarEvent(j, "Event " + j, "Event " + j, 1504080564587L, 1504080564587L));
//                }
//                mCalendarEntriesList.add(new CalendarEntry("2017/08/29", events));
//            }
//            mDatesAdapter.notifyDataSetChanged();
            Toast.makeText(this, R.string.no_calendar_events_error, Toast.LENGTH_LONG).show();
        }
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
                    readCalendarEventDates();
                } else {
                    requestWriteCalendarPermission();
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
