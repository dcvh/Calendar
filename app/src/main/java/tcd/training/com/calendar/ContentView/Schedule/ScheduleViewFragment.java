package tcd.training.com.calendar.ContentView.Schedule;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.ContentView.Schedule.CalendarEntriesAdapter.ParallaxViewHolder;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Utils.PreferenceUtils;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ScheduleViewFragment extends Fragment implements ContentViewBehaviors {

    private static final String TAG = ScheduleViewFragment.class.getSimpleName();

    private static int DY_LIMIT_FOR_PARALLAX = 100;

    private ArrayList<Entry> mEntries;
    private ArrayList<Long> mWeekPeriods;
    private Context mContext;
    private int mCurMonth;
    private int mStartWeekIndex;
    private int mEndWeekIndex;
    private int mPosition;
    private int mPrevDy;

    private LinearLayoutManager mLayoutManager;
    private CalendarEntriesAdapter mAdapter;

    public static ScheduleViewFragment newInstance() {
        Bundle args = new Bundle();
        ScheduleViewFragment fragment = new ScheduleViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mCurMonth = Calendar.getInstance().get(Calendar.MONTH);

        breakUpIntoWeekPeriods();
        // determine the start and end periods of current year
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 0; i < mWeekPeriods.size(); i++) {
            if (TimeUtils.getField(mWeekPeriods.get(i), Calendar.YEAR) == curYear) {
                mStartWeekIndex = i;
                break;
            }
        }
        for (int i = mWeekPeriods.size() - 1; i >= 0; i--) {
            if (TimeUtils.getField(mWeekPeriods.get(i), Calendar.YEAR) == curYear) {
                mEndWeekIndex = i;
                break;
            }
        }

        // get all entries between start and end date
        mEntries = DataUtils.getEntriesBetween(mContext, mWeekPeriods.get(mStartWeekIndex), mWeekPeriods.get(mEndWeekIndex));
        if (mEntries != null) {
            // insert today (if it doesn't exist)
            mEntries = (ArrayList<Entry>) mEntries.clone();

            mEntries = insertMonthsAndWeeks(mEntries, mWeekPeriods.get(mStartWeekIndex), mWeekPeriods.get(mEndWeekIndex));
            insertToday(mEntries);
        } else {
            mEntries = new ArrayList<>();
        }
    }

    private void breakUpIntoWeekPeriods() {

        Calendar start = Calendar.getInstance();
        start.set(1970, 0, 8, 0, 0);
        Calendar end = Calendar.getInstance();
        end.set(2030, 11, 31, 23, 59);

        // determine the first date of week
        int previousWeekDays = start.get(Calendar.DAY_OF_WEEK) - PreferenceUtils.getFirstDayOfWeek(mContext);
        start.add(Calendar.DAY_OF_MONTH, -previousWeekDays);

        mWeekPeriods = new ArrayList<>();
        while (start.compareTo(end) < 0) {
            mWeekPeriods.add(start.getTimeInMillis());
            start.add(Calendar.DAY_OF_MONTH, 7);
        }
    }

    private ArrayList<Entry> insertMonthsAndWeeks(ArrayList<Entry> entries, long startTime, long endTime) {

        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startTime);

        ArrayList<Entry> weekMonths = new ArrayList<>();
        while (start.getTimeInMillis() < endTime) {

            if (start.get(Calendar.DAY_OF_MONTH) > 1 && start.get(Calendar.DAY_OF_MONTH) <= 8) {
                Calendar earlyMonth = (Calendar) start.clone();
                earlyMonth.set(Calendar.DAY_OF_MONTH, 1);
                weekMonths.add(new Entry(earlyMonth.getTimeInMillis(), "m", null));
            }

            Entry entry = new Entry(start.getTimeInMillis(), "w", null);
            weekMonths.add(entry);

            start.add(Calendar.DAY_OF_MONTH, 7);
        }

        weekMonths.addAll(entries);
        Collections.sort(weekMonths);

        return weekMonths;
    }

    private void insertToday(ArrayList<Entry> entries) {
        long today = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            int comparison = TimeUtils.compareDay(entry.getTime(), today);
            if (comparison == 0 && entry.getDescription() == null) {
                entry.setDescription("t");
                break;
            } else if (comparison > 0) {
                entries.add(i, new Entry(today, "t", new ArrayList<Event>()));
                break;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_schedule_view, container, false);

        // adapter
        mAdapter = new CalendarEntriesAdapter(mContext, mEntries);

        // recycler view
        RecyclerView eventsRecyclerView = view.findViewById(R.id.rv_events_list);
        mLayoutManager = new LinearLayoutManager(mContext);
        eventsRecyclerView.setLayoutManager(mLayoutManager);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());
        eventsRecyclerView.setAdapter(mAdapter);

        eventsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                mPosition = mLayoutManager.findFirstVisibleItemPosition();

                // add more events when scroll to top or bottom
                if (dy < 0) {
                    if (mPosition < 3) {
                        addMoreEventWhenScrollToTop();
                    }
                } else {
                    if (mEntries.size() - mPosition < 10) {
                        addMoreEventWhenScrollToBottom();
                    }
                }

                // update month title
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(mEntries.get(mPosition).getTime());
                if (cal.get(Calendar.MONTH) != mCurMonth) {
                    sendUpdateMonthAction(mPosition);
                    mCurMonth = cal.get(Calendar.MONTH);
                }

                // parallax
                if (Math.abs(dy) < DY_LIMIT_FOR_PARALLAX && mPrevDy < DY_LIMIT_FOR_PARALLAX) {
                    for (int i = 0; i < recyclerView.getChildCount(); i++) {
                        RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(recyclerView.getChildAt(i));
                        if (viewHolder instanceof ParallaxViewHolder) {
                            ((ParallaxViewHolder) viewHolder).animateImage();
                        }
                    }
                }
                mPrevDy = Math.abs(dy);
            }
        });

        scrollToToday();

        return view;
    }

    private void addMoreEventWhenScrollToTop() {
        int newStartIndex = mStartWeekIndex - 52 >= 0 ? mStartWeekIndex - 52 : 0;
        ArrayList<Entry> newEntries = insertMonthsAndWeeks(
                DataUtils.getEntriesBetween(mContext, mWeekPeriods.get(newStartIndex), mWeekPeriods.get(mStartWeekIndex)),
                mWeekPeriods.get(newStartIndex),
                mWeekPeriods.get(mStartWeekIndex)
        );
        mEntries.addAll(0, newEntries);
        mAdapter.notifyDataSetChanged();
        mLayoutManager.scrollToPositionWithOffset(newEntries.size() + mPosition, 0);
        mStartWeekIndex = newStartIndex;
    }

    private void addMoreEventWhenScrollToBottom() {
        int newEndIndex = mEndWeekIndex + 52 < mWeekPeriods.size() ? mEndWeekIndex + 52 : mWeekPeriods.size() - 1;
        ArrayList<Entry> newEntries = insertMonthsAndWeeks(
                DataUtils.getEntriesBetween(mContext, mWeekPeriods.get(mEndWeekIndex), mWeekPeriods.get(newEndIndex)),
                mWeekPeriods.get(mEndWeekIndex),
                mWeekPeriods.get(newEndIndex)
        );
        mEntries.addAll(newEntries);
        mAdapter.notifyDataSetChanged();
        mEndWeekIndex = newEndIndex;
    }

    private void sendUpdateMonthAction(int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, mEntries.get(position).getTime());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void scrollTo(long millis) {
        int mCurrentPosition = mLayoutManager.findFirstVisibleItemPosition();
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {
            @Override protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        if (millis < mEntries.get(0).getTime() || millis > mEntries.get(mEntries.size() - 1).getTime()) {
            while (millis < mEntries.get(0).getTime()) {
                addMoreEventWhenScrollToTop();
            }
            while (millis > mEntries.get(mEntries.size() - 1).getTime()) {
                addMoreEventWhenScrollToBottom();
            }
            mAdapter.notifyDataSetChanged();
        }

        // TODO: 9/19/17 this is temporary, must be fixed in the future for better performance
        for (int i = 0; i < mEntries.size(); i++) {
            int comparison = TimeUtils.compareDay(mEntries.get(i).getTime(), millis);
            if (comparison >= 0) {
                if (comparison > 0) {
                    i--;
                }
                if (i < 0 || i >= mEntries.size()) {
                    return;
                }
                if (Math.abs(mCurrentPosition - i) > 50) {
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                } else {
                    smoothScroller.setTargetPosition(i);
                    mLayoutManager.startSmoothScroll(smoothScroller);
                }
                sendUpdateMonthAction(i);
                break;
            }
        }
    }

    @Override
    public void scrollToToday() {
        scrollTo(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public void addEvent() {

//        final String selection = CalendarContract.Events.DIRTY + "=" + 1;
//        ArrayList<Event> events = DataUtils.readCalendarEvents(selection, mContext);
//
//        Event event = events.getField(events.size() - 1);
//        for (Entry entry : mEntries) {
//            if (entry.getDescription() == null || entry.getDescription().length() == 0 || entry.getDescription().equals("t")) {
//                if (TimeUtils.isSameDay(entry.getTime(), event.getStartDate())) {
//                    entry.getEvents().add(event);
//                }
//            }
//        }
//
//        mAdapter.notifyDataSetChanged();

        removeEvent();
    }

    @Override
    public void removeEvent() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                DataUtils.readCalendarEventsInfo(mContext);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                mEntries.clear();
                mEntries.addAll(insertMonthsAndWeeks(
                        DataUtils.getEntriesBetween(mContext, mWeekPeriods.get(mStartWeekIndex), mWeekPeriods.get(mEndWeekIndex)),
                        mWeekPeriods.get(mStartWeekIndex),
                        mWeekPeriods.get(mEndWeekIndex)
                ));
                insertToday(mEntries);
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    @Override
    public void invalidate() {

    }
}
