package tcd.training.com.calendar.ContentView.Day;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayViewFragment extends Fragment implements ContentViewBehaviors {

    private static final String TAG = DayViewFragment.class.getSimpleName();
    private static final String ARG_SPECIFIED_DATE = "specificDate";

    private ArrayList<Calendar> mDays;
    private long mSpecifiedTime;
    private Context mContext;

    private ViewPager mDayViewPager;
    private DayPagerAdapter mAdapter;

    public DayViewFragment() {
    }

    public static DayViewFragment newInstance() {
        Bundle args = new Bundle();
        DayViewFragment fragment = new DayViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DayViewFragment newInstance(long millis) {
        Bundle args = new Bundle();
        args.putLong(ARG_SPECIFIED_DATE, millis);
        DayViewFragment fragment = new DayViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            Bundle args = getArguments();
            mSpecifiedTime = args.getLong(ARG_SPECIFIED_DATE, 0);
        }

        mContext = getContext();

        // determine the year to be showed
        mDays = new ArrayList<>();
        Calendar firstDate = Calendar.getInstance();
        if (mSpecifiedTime > 0) {
            firstDate.setTimeInMillis(mSpecifiedTime);
        }
        mDays.add(firstDate);
        addMoreDays(true);
        addMoreDays(false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_day_view, container, false);

        initializeUiComponents(view);

        if (mSpecifiedTime > 0) {
            scrollTo(mSpecifiedTime);
        } else {
            scrollToToday();
        }

        return view;
    }

    private void initializeUiComponents(View view) {
        mDayViewPager = view.findViewById(R.id.vp_day_view);
        mAdapter = new DayPagerAdapter(getChildFragmentManager(), mDays);
        mDayViewPager.setAdapter(mAdapter);

        mDayViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == mDays.size() - 1) {
                    addMoreDays(false);
                } else if (position == 0) {
                    position = addMoreDays(true);
                    mDayViewPager.setCurrentItem(position, false);
                }
                // change month
                int dayOfMonth = mDays.get(position).get(Calendar.DAY_OF_MONTH);
                if (dayOfMonth == 1 || dayOfMonth == mDays.get(position).getActualMaximum(Calendar.DAY_OF_MONTH)) {
                    sendUpdateMonthAction(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private int addMoreDays(boolean toHead) {
        final int numberOfAddedDays = 10;
        if (toHead) {
            Calendar prev = (Calendar) mDays.get(0).clone();
            for (int i = 0; i < numberOfAddedDays; i++) {
                prev.add(Calendar.DAY_OF_MONTH, -1);
                mDays.add(0, (Calendar) prev.clone());
            }
        } else {
            Calendar next = (Calendar) mDays.get(mDays.size() - 1).clone();
            for (int i = 0; i < numberOfAddedDays; i++) {
                next.add(Calendar.DAY_OF_MONTH, 1);
                mDays.add((Calendar) next.clone());
            }
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        return numberOfAddedDays;
    }

    private void sendUpdateMonthAction(int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, mDays.get(position).getTimeInMillis());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void scrollTo(long millis) {
        scrollTo(millis, true);
    }

    public void scrollTo(long millis, boolean smoothScroll) {

        // in case it's out of range, reinitialize entire days with the new one
        Calendar destDay = Calendar.getInstance();
        destDay.setTimeInMillis(millis);
        if (destDay.compareTo(mDays.get(0)) < 0 || destDay.compareTo(mDays.get(mDays.size() - 1)) > 0) {
            mDays.clear();
            mDays.add(destDay);
            addMoreDays(true);
            addMoreDays(false);
        }

        // binary search
        Comparator<Calendar> comparator = new Comparator<Calendar>() {
            @Override
            public int compare(Calendar calendar, Calendar t1) {
                return TimeUtils.compareDay(calendar.getTimeInMillis(), t1.getTimeInMillis());
            }
        };
        int index = Collections.binarySearch(mDays, destDay, comparator);
        if (index >= 0) {
            mDayViewPager.setCurrentItem(index, smoothScroll);
            sendUpdateMonthAction(index);
        }
    }

    @Override
    public void scrollToToday() {
        scrollTo(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public void addEvent() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeEvent() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void invalidate() {
        mDayViewPager.invalidate();
    }
}