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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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
    private ArrayList<Long> mWeekPeriods;
    private int mStartWeekIndex;
    private int mEndWeekIndex;

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

        breakUpIntoWeekPeriods();
        // determine start and end periods of current year
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < mWeekPeriods.size(); i++) {
            if (TimeUtils.getYear(mWeekPeriods.get(i)) == curYear) {
                mStartWeekIndex = i;
                break;
            }
        }
        for (int i = mWeekPeriods.size() - 1; i >= 0; i--) {
            if (TimeUtils.getYear(mWeekPeriods.get(i)) == curYear) {
                mEndWeekIndex = i;
                break;
            }
        }

        mEntriesList = DataUtils.getEntriesBetween(getContext(), mWeekPeriods.get(mStartWeekIndex), mWeekPeriods.get(mEndWeekIndex));
        if (mEntriesList != null) {
            // insert today (if it doesn't exist)
            mEntriesList = (ArrayList<Entry>) mEntriesList.clone();

            insertToday(mEntriesList);

            insertMonthAndWeekEntries(mEntriesList, mWeekPeriods.get(mStartWeekIndex), mWeekPeriods.get(mEndWeekIndex));

        } else {
            mEntriesList = new ArrayList<>();
        }
    }

    private void breakUpIntoWeekPeriods() {

        Calendar start = Calendar.getInstance();
        start.set(1970, 0, 8, 0, 0);
        Calendar end = Calendar.getInstance();
        end.set(2030, 11, 31, 23, 59);

        // determine the first date of week
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
        int previousWeekDays = start.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek;
        start.add(Calendar.DAY_OF_MONTH, -previousWeekDays);

        mWeekPeriods = new ArrayList<>();
        while (start.compareTo(end) < 0) {
            mWeekPeriods.add(start.getTimeInMillis());
            start.add(Calendar.DAY_OF_MONTH, 7);
        }
    }

    private void insertToday(ArrayList<Entry> entries) {
        long today = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < entries.size(); i++) {
            int comparison = TimeUtils.compareDay(entries.get(i).getTime(), today);
            if (comparison == 0) {
                break;
            } else if (comparison > 0) {
                entries.add(i, new Entry(today, new ArrayList<Event>()));
                break;
            }
        }
    }

    private void insertMonthAndWeekEntries(ArrayList<Entry> entries, long start, long end) {

        assert start < end;

        Calendar firstDate = Calendar.getInstance();
        firstDate.setTimeInMillis(start);

        // add week entries
        boolean isNewMonth = false;
        for (int i = 0; i < entries.size(); i++) {
            long difference = firstDate.getTimeInMillis() - entries.get(i).getTime();
            if (difference < 0) {

                if (isNewMonth) {
                    insertMonthEntry(firstDate, entries, i);
                    i++;
                }

                isNewMonth = insertWeekEntry(firstDate, entries, i);

            } else if (isNewMonth) {
                Calendar curMonth = (Calendar) firstDate.clone();
                curMonth.set(Calendar.DAY_OF_MONTH, 1);
                if (TimeUtils.compareDay(curMonth.getTimeInMillis(), entries.get(i).getTime()) <= 0) {
                    insertMonthEntry(firstDate, entries, i);
                    isNewMonth = false;
                }
            }
        }

        while (firstDate.getTimeInMillis() < end) {
            isNewMonth = insertWeekEntry(firstDate, entries, entries.size());
            if (isNewMonth) {
                insertMonthEntry(firstDate, entries, entries.size());
            }
        }
    }

    private boolean insertWeekEntry(Calendar date, ArrayList<Entry> entries, int index) {

        boolean isNewMonth = false;

        // current date
        String dateString = TimeUtils.getFormattedDate(date.getTimeInMillis(), "MMM, d") + " - ";
        int curMonth = date.get(Calendar.MONTH);

        // next 7 days
        date.add(Calendar.DAY_OF_MONTH, 6);
        if (curMonth == date.get(Calendar.MONTH)) {
            dateString += TimeUtils.getFormattedDate(date.getTimeInMillis(), "d");
        } else {
            dateString += TimeUtils.getFormattedDate(date.getTimeInMillis(), "MMM, d");
            isNewMonth = true;
        }

        // year
        if (date.get(Calendar.YEAR) != Calendar.getInstance().get(Calendar.YEAR)) {
            dateString += ", " + date.get(Calendar.YEAR);
        }

        // add it to entries
        Entry weekEntry = new Entry(date.getTimeInMillis(), dateString, null);
        entries.add(index, weekEntry);

        date.add(Calendar.DAY_OF_MONTH, 1);
        return isNewMonth || ((int)date.get(Calendar.DAY_OF_MONTH) == 8);
    }

    private void insertMonthEntry(Calendar date, ArrayList<Entry> entries, int index) {
        String month = TimeUtils.getFormattedDate(date.getTimeInMillis(), "MMMM yyyy");
        Entry monthEntry = new Entry(date.getTimeInMillis(), month, null);
        entries.add(index, monthEntry);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_schedule_view, container, false);

        // adapter
        mAdapter = new CalendarEntriesAdapter(view.getContext(), mEntriesList);

        // recycler view
        RecyclerView eventsRecyclerView = view.findViewById(R.id.rv_events_list);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        eventsRecyclerView.setLayoutManager(mLayoutManager);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        eventsRecyclerView.setAdapter(mAdapter);

        eventsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int position = mLayoutManager.findFirstVisibleItemPosition();
                if (dy < 0) {
                    if (position < 3) {
                        int newStartIndex = mStartWeekIndex - 52 >= 0 ? mStartWeekIndex - 52 : 0;
                        ArrayList<Entry> newEntries = DataUtils.getEntriesBetween(getContext(), mWeekPeriods.get(newStartIndex), mWeekPeriods.get(mStartWeekIndex));
                        insertMonthAndWeekEntries(newEntries, mWeekPeriods.get(newStartIndex), mWeekPeriods.get(mStartWeekIndex));
                        mEntriesList.addAll(0, newEntries);
                        mAdapter.notifyDataSetChanged();
                        mLayoutManager.scrollToPositionWithOffset(newEntries.size(), 0);
                        mStartWeekIndex = newStartIndex;
                    }
                } else {
                    if (mEntriesList.size() - position < 10) {
                        int newEndIndex = mEndWeekIndex + 52 < mWeekPeriods.size() ? mEndWeekIndex + 52 : mWeekPeriods.size() - 1;
                        ArrayList<Entry> newEntries = DataUtils.getEntriesBetween(getContext(), mWeekPeriods.get(mEndWeekIndex), mWeekPeriods.get(newEndIndex));
                        insertMonthAndWeekEntries(newEntries, mWeekPeriods.get(mEndWeekIndex), mWeekPeriods.get(newEndIndex));
                        mEntriesList.addAll(newEntries);
                        mAdapter.notifyDataSetChanged();
                        mEndWeekIndex = newEndIndex;
                    }
                }
            }
        });

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
