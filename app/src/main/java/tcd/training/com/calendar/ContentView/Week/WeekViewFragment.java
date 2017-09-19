package tcd.training.com.calendar.ContentView.Week;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Utils.TimeUtils;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class WeekViewFragment extends Fragment implements ContentViewBehaviors {

    private static final String TAG = WeekViewFragment.class.getSimpleName();
    private static final String ARG_SPECIFIED_DATE = "specificDate";

    private ArrayList<Calendar> mWeeks;
    private Context mContext;

    private ViewPager mWeekViewPager;
    private WeekPagerAdapter mAdapter;

    public WeekViewFragment() {
    }

    public static WeekViewFragment newInstance() {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, DataUtils.getAllEntries());
        WeekViewFragment fragment = new WeekViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static WeekViewFragment newInstance(long millis) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, DataUtils.getAllEntries());
        args.putLong(ARG_SPECIFIED_DATE, millis);
        WeekViewFragment fragment = new WeekViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            Bundle args = getArguments();
//            mEntriesList = (ArrayList<Entry>) args.getSerializable(ARG_ENTRIES_LIST);
//            mSpecifiedTime = args.getLong(ARG_SPECIFIED_DATE, -1);
            getArguments().remove(ARG_ENTRIES_LIST);
        } else {
//            mEntriesList = new ArrayList<>();
        }

        mContext = getContext();
        mWeeks = new ArrayList<>();
        addOneMoreYear(Calendar.getInstance().get(Calendar.YEAR), false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_day_view, container, false);

        initializeUiComponents(view);

        scrollToToday();

        return view;
    }

    private void initializeUiComponents(View view) {
        mWeekViewPager = view.findViewById(R.id.vp_day_view);
        mAdapter = new WeekPagerAdapter(getChildFragmentManager(), mWeeks);
        mWeekViewPager.setAdapter(mAdapter);

        mWeekViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == mWeeks.size() - 1) {
                    addOneMoreYear(mWeeks.get(mWeeks.size() - 1).get(Calendar.YEAR) + 1, false);
                    mAdapter.notifyDataSetChanged();
                } else if (position == 0) {
                    addOneMoreYear(mWeeks.get(0).get(Calendar.YEAR) - 1, true);
                    mAdapter.notifyDataSetChanged();
                    mWeekViewPager.setCurrentItem(52, false);

                }

                // change month
                sendUpdateMonthAction(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addOneMoreYear(int year, boolean toHead) {
        Calendar startDate = Calendar.getInstance();
        startDate.set(year, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(year, 11, 31);

        if (toHead) {
            while (endDate.compareTo(startDate) > 0) {
                mWeeks.add(0, (Calendar) endDate.clone());
                endDate.add(Calendar.DAY_OF_MONTH, -7);
            }

        } else {
            while (startDate.compareTo(endDate) < 0) {
                mWeeks.add((Calendar) startDate.clone());
                startDate.add(Calendar.DAY_OF_MONTH, 7);
            }
        }
    }

    private void sendUpdateMonthAction(int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_CALENDAR, mWeeks.get(position));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void scrollTo(long millis) {
        int low = 0;
        int high = mWeeks.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = TimeUtils.compareWeek(millis, mWeeks.get(mid).getTimeInMillis());
            if (comparison < 0) {
                high = mid - 1;
            } else if (comparison > 0) {
                low = mid + 1;
            } else {
                mWeekViewPager.setCurrentItem(mid);
                sendUpdateMonthAction(mid);
                return;
            }
        }
    }

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
        mWeekViewPager.invalidate();
    }
}
