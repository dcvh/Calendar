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

import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayViewFragment extends Fragment implements ContentViewBehaviors {

    private static final String TAG = DayViewFragment.class.getSimpleName();
    private static final String ARG_SPECIFIED_DATE = "specificDate";

    private ArrayList<Entry> mEntriesList;
    private ArrayList<Calendar> mDays;
    private long mSpecifiedTime;
    private Context mContext;

    private ViewPager mDayViewPager;
    private DayPagerAdapter mAdapter;

    public DayViewFragment() {
    }

    public static DayViewFragment newInstance() {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, DataUtils.getAllEntries());
        DayViewFragment fragment = new DayViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static DayViewFragment newInstance(long millis) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, DataUtils.getAllEntries());
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
            mEntriesList = (ArrayList<Entry>) args.getSerializable(ARG_ENTRIES_LIST);
            mSpecifiedTime = args.getLong(ARG_SPECIFIED_DATE, -1);
            getArguments().remove(ARG_ENTRIES_LIST);
        } else {
            mEntriesList = new ArrayList<>();
        }

        // determine the year to be showed
        mDays = new ArrayList<>();
        Calendar year = Calendar.getInstance();
        if (mSpecifiedTime > 0) {
            year.setTimeInMillis(mSpecifiedTime);
        }
        addOneMoreYear(year.get(Calendar.YEAR), false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_day_view, container, false);
        mContext = view.getContext();

        initializeUiComponents(view);

        // scroll to specified day (if any), otherwise scroll to today
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
                    addOneMoreYear(mDays.get(mDays.size() - 1).get(Calendar.YEAR) + 1, false);
                    mAdapter.notifyDataSetChanged();
                } else if (position == 0) {
                    int daysOfYear = addOneMoreYear(mDays.get(0).get(Calendar.YEAR) - 1, true);
                    mAdapter.notifyDataSetChanged();
                    mDayViewPager.setCurrentItem(daysOfYear - 1, false);
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

    private int addOneMoreYear(int year, boolean toHead) {
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(year, 11, 31);

        if (toHead) {
            while (endDate.compareTo(startDate) > 0) {
                mDays.add(0, (Calendar) endDate.clone());
                endDate.add(Calendar.DAY_OF_MONTH, -1);
            }
            return startDate.getActualMaximum(Calendar.DAY_OF_YEAR);

        } else {
            while (startDate.compareTo(endDate) < 0) {
                mDays.add((Calendar) startDate.clone());
                startDate.add(Calendar.DAY_OF_MONTH, 1);
            }
            return endDate.getActualMaximum(Calendar.DAY_OF_YEAR);
        }
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
        if (mDays != null) {
            int low = 0;
            int high = mDays.size() - 1;
            while (low <= high) {
                int mid = low + (high - low) / 2;
                int comparison = TimeUtils.compareDay(millis, mDays.get(mid).getTimeInMillis());
                if (comparison < 0) {
                    high = mid - 1;
                } else if (comparison > 0) {
                    low = mid + 1;
                } else {
                    mDayViewPager.setCurrentItem(mid, smoothScroll);
                    sendUpdateMonthAction(mid);
                    return;
                }
            }
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