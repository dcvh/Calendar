package tcd.training.com.calendar.ContentView.Week;

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
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.PreferenceUtils;
import tcd.training.com.calendar.Utils.TimeUtils;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class WeekViewFragment extends Fragment implements ContentViewBehaviors {

    private static final String TAG = WeekViewFragment.class.getSimpleName();
    private static final String ARG_SPECIFIED_DATE = "specificDate";

    private ArrayList<Long> mWeeks;
    private Context mContext;

    private ViewPager mWeekViewPager;
    private WeekPagerAdapter mAdapter;

    public static WeekViewFragment newInstance() {
        Bundle args = new Bundle();
        WeekViewFragment fragment = new WeekViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
        mWeeks = new ArrayList<>();

        // initialize weeks
        int firstDayOfWeek = PreferenceUtils.getFirstDayOfWeek(mContext);
        Calendar cal = Calendar.getInstance();
        mWeeks.add(cal.getTimeInMillis() + TimeUnit.DAYS.toMillis(firstDayOfWeek + 7 - cal.get(Calendar.DAY_OF_WEEK)));
        addMoreWeeks(true);
        addMoreWeeks(false);
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
                    addMoreWeeks(false);
                } else if (position == 0) {
                    addMoreWeeks(true);
                    position = 52;
                    mWeekViewPager.setCurrentItem(position, false);

                }
                sendUpdateMonthAction(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addMoreWeeks(boolean toHead) {
        if (toHead) {
            long prevWeek = mWeeks.get(0);
            for (int i = 1; i <= 10; i++) {
                mWeeks.add(0, prevWeek - TimeUnit.DAYS.toMillis(i * 7));
            }
        } else {
            long nextWeek = mWeeks.get(mWeeks.size() - 1);
            for (int i = 1; i <= 10; i++) {
                mWeeks.add(nextWeek + TimeUnit.DAYS.toMillis(i * 7));
            }
        }
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private void sendUpdateMonthAction(int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, mWeeks.get(position));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void scrollTo(long millis) {

        // in case it's out of range, add more weeks
        if (millis < mWeeks.get(0) || millis > mWeeks.get(mWeeks.size() - 1)) {
            while (millis < mWeeks.get(0)) {
                addMoreWeeks(true);
            }
            while (millis > mWeeks.get(mWeeks.size() - 1)) {
                addMoreWeeks(false);
            }
            mAdapter.notifyDataSetChanged();
        }

        // binary search
        final int firstDayOfWeek = PreferenceUtils.getFirstDayOfWeek(mContext);
        Comparator<Long> comparator = new Comparator<Long>() {
            @Override
            public int compare(Long aLong, Long t1) {
                return TimeUtils.compareWeek(aLong, t1, firstDayOfWeek);
            }
        };
        int index = Collections.binarySearch(mWeeks, millis, comparator);
        if (index >= 0) {
            mWeekViewPager.setCurrentItem(index);
            sendUpdateMonthAction(index);
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
