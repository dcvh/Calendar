package tcd.training.com.calendar.ContentView.Schedule;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ScheduleViewFragment extends Fragment {

    private static final String TAG = ScheduleViewFragment.class.getSimpleName();

    private ArrayList<Entry> mEntriesList;

    private LinearLayoutManager mLayoutManager;
    private CalendarEntriesAdapter mAdapter;

    public ScheduleViewFragment() {
    }

    public static ScheduleViewFragment newInstance() {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, DataUtils.getAllEntries());
        ScheduleViewFragment fragment = new ScheduleViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEntriesList = DataUtils.getAllEntries();
        if (mEntriesList != null) {
            // insert today (if it doesn't exist)
            mEntriesList = (ArrayList<Entry>) mEntriesList.clone();

            insertToday();
            createMonthAndWeekEntries();

        } else {
            mEntriesList = new ArrayList<>();
        }
    }

    private void insertToday() {
        long today = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < mEntriesList.size(); i++) {
            int comparison = TimeUtils.compareDay(mEntriesList.get(i).getTime(), today);
            if (comparison == 0) {
                break;
            } else if (comparison > 0) {
                mEntriesList.add(i, new Entry(today, new ArrayList<Event>()));
                break;
            }
        }
    }

    private void createMonthAndWeekEntries() {

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
        firstDate.setTimeInMillis(mEntriesList.get(0).getTime());
        int previousWeekDays = firstDate.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
        firstDate.add(Calendar.DAY_OF_MONTH, -previousWeekDays);

        // add week entries
        boolean isNewMonth = false;
        for (int i = 0; i < mEntriesList.size(); i++) {
            long difference = firstDate.getTimeInMillis() - mEntriesList.get(i).getTime();
            if (difference < 0) {

                if (isNewMonth) {
                    String month = TimeUtils.getFormattedDate(firstDate.getTimeInMillis(), "MMMM yyyy");
                    Entry monthEntry = new Entry(firstDate.getTimeInMillis(), month, null);
                    mEntriesList.add(i++, monthEntry);
                    isNewMonth = false;
                }

                String date = TimeUtils.getFormattedDate(firstDate.getTimeInMillis(), "MMM, d") + " - ";
                int curMonth = firstDate.get(Calendar.MONTH);
                firstDate.add(Calendar.DAY_OF_MONTH, 6);
                if (curMonth == firstDate.get(Calendar.MONTH)) {
                    date += TimeUtils.getFormattedDate(firstDate.getTimeInMillis(), "d");
                } else {
                    date += TimeUtils.getFormattedDate(firstDate.getTimeInMillis(), "MMM, d");
                    isNewMonth = true;
                }
                if (firstDate.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
                    date += ", " + firstDate.get(Calendar.YEAR);
                }

                Entry weekEntry = new Entry(firstDate.getTimeInMillis(), date, null);
                mEntriesList.add(i, weekEntry);

            } else if (isNewMonth) {
                if (TimeUtils.compareMonth(firstDate.getTimeInMillis(), mEntriesList.get(i).getTime()) < 0) {
                    String month = TimeUtils.getFormattedDate(firstDate.getTimeInMillis(), "MMMM yyyy");
                    Entry monthEntry = new Entry(firstDate.getTimeInMillis(), month, null);
                    mEntriesList.add(i, monthEntry);
                    isNewMonth = false;
                }
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
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        long today = Calendar.getInstance().getTimeInMillis();
        // TODO: 09/09/2017 this is temporary, must be fixed in the future for better performance
        for (int i = 0; i < mEntriesList.size(); i++) {
            if (TimeUtils.isSameDay(mEntriesList.get(i).getTime(), today)) {
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
