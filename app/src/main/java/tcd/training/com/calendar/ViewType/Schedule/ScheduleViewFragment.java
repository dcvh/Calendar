package tcd.training.com.calendar.ViewType.Schedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ScheduleViewFragment extends Fragment {

    private static final String TAG = ScheduleViewFragment.class.getSimpleName();

    private ArrayList<CalendarEntry> mEntriesList;

    private LinearLayoutManager mLayoutManager;
    private CalendarEntriesAdapter mAdapter;

    public ScheduleViewFragment() {
    }

    public static ScheduleViewFragment newInstance() {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, CalendarUtils.getAllEntries());
        ScheduleViewFragment fragment = new ScheduleViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEntriesList = CalendarUtils.getAllEntries();
        if (mEntriesList != null) {
            // insert today (if it doesn't exist)
            mEntriesList = (ArrayList<CalendarEntry>) mEntriesList.clone();

            insertToday();
            createWeekEntries();

        } else {
            mEntriesList = new ArrayList<>();
        }
    }

    private void insertToday() {
        String today = CalendarUtils.getDate(Calendar.getInstance().getTimeInMillis(), CalendarUtils.getStandardDateFormat());
        for (int i = 0; i < mEntriesList.size(); i++) {
            String date = mEntriesList.get(i).getDate();
            if (date.equals(today)) {
                break;
            } else if (date.compareTo(today) > 0) {
                mEntriesList.add(i, new CalendarEntry(today, new ArrayList<CalendarEvent>()));
                break;
            }
        }
    }

    private void createWeekEntries() {

        // determine the first date
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String firstDay = sharedPreferences.getString(getString(R.string.pref_key_start_of_the_week), "Monday");
        int firstDayOfWeek;
        switch (firstDay) {
            case "Saturday": firstDayOfWeek = Calendar.SATURDAY; break;
            case "Sunday": firstDayOfWeek = Calendar.SUNDAY; break;
            case "Monday": firstDayOfWeek = Calendar.MONDAY; break;
            default:
                throw new UnsupportedOperationException("Unknown first day");
        }
        Calendar firstDate = Calendar.getInstance();
        firstDate.setTimeInMillis(CalendarUtils.getMilliSeconds(mEntriesList.get(0).getDate()));
        int previousWeekDays = firstDate.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
        firstDate.add(Calendar.DAY_OF_MONTH, -previousWeekDays);

        // add week entries
        for (int i = 0; i < mEntriesList.size(); i++) {
            long difference = firstDate.getTimeInMillis() - CalendarUtils.getMilliSeconds(mEntriesList.get(i).getDate());
            if (difference < 0) {

                String date = CalendarUtils.getDate(firstDate.getTimeInMillis(), "MMM, d");
                int curMonth = firstDate.get(Calendar.MONTH);
                firstDate.add(Calendar.DAY_OF_MONTH, 7);
                date += " - " + (curMonth == firstDate.get(Calendar.MONTH) ?
                        CalendarUtils.getDate(firstDate.getTimeInMillis(), "d") :
                        CalendarUtils.getDate(firstDate.getTimeInMillis(), "MMM, d"));
                if (firstDate.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
                    date += ", " + firstDate.get(Calendar.YEAR);
                }

                CalendarEntry weekEntry = new CalendarEntry(date, new ArrayList<CalendarEvent>());
                mEntriesList.add(i, weekEntry);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_schedule_view, container, false);

        // recycler view
        RecyclerView eventsRecyclerView = view.findViewById(R.id.rv_events_list);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        eventsRecyclerView.setLayoutManager(mLayoutManager);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new CalendarEntriesAdapter(view.getContext(), mEntriesList);
        eventsRecyclerView.setAdapter(mAdapter);

        scrollToToday();

        return view;
    }

    public void scrollToToday() {

        int mCurrentPosition = mLayoutManager.findFirstVisibleItemPosition();
        String today = CalendarUtils.getDate(Calendar.getInstance().getTimeInMillis(), CalendarUtils.getStandardDateFormat());
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        // TODO: 09/09/2017 this is temporary, must be fixed in the future for better performance
        for (int i = 0; i < mEntriesList.size(); i++) {
            String date = mEntriesList.get(i).getDate();
            if (date.equals(today)) {
                if (Math.abs(mCurrentPosition - i) > 100) {
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                } else {
                    smoothScroller.setTargetPosition(i);
                    mLayoutManager.startSmoothScroll(smoothScroller);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
