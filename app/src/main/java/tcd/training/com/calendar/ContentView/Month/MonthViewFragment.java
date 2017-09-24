package tcd.training.com.calendar.ContentView.Month;

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
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthViewFragment extends Fragment implements ContentViewBehaviors{

    private static final String TAG = MonthViewFragment.class.getSimpleName();

    private ArrayList<Calendar> mMonths;
    private Context mContext;

    private ViewPager mMonthViewPager;
    private MonthPagerAdapter mAdapter;

    public static MonthViewFragment newInstance() {
        Bundle args = new Bundle();
        MonthViewFragment fragment = new MonthViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMonths = new ArrayList<>();
        Calendar curYearMonths = Calendar.getInstance();
        curYearMonths.set(Calendar.getInstance().get(Calendar.YEAR), 0, 0, 0, 0);
        for (int i = 0; i < 12; i++) {
            mMonths.add((Calendar) curYearMonths.clone());
            curYearMonths.add(Calendar.MONTH, 1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month_view, container, false);
        mContext = view.getContext();

        initializeUiComponents(view);

        scrollToToday();

        return view;
    }

    private void initializeUiComponents(View view) {
        mMonthViewPager = view.findViewById(R.id.vp_month_view);
        mAdapter = new MonthPagerAdapter(getChildFragmentManager(), mMonths);
        mMonthViewPager.setAdapter(mAdapter);

        mMonthViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == mMonths.size() - 1) {
                    addOneMoreYear(false);
                } else if (position == 0) {
                    addOneMoreYear(true);
                    position = 12;
                    mMonthViewPager.setCurrentItem(position, false);
                }
                sendUpdateMonthAction(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addOneMoreYear(boolean toTop) {
        if (toTop) {
            Calendar prev = (Calendar) mMonths.get(0).clone();
            for (int i = 0; i < 12; i++) {
                prev.add(Calendar.MONTH, -1);
                mMonths.add(0, (Calendar) prev.clone());
            }
        } else {
            Calendar next = (Calendar) mMonths.get(mMonths.size() - 1).clone();
            for (int i = 0; i < 12; i++) {
                next.add(Calendar.MONTH, 1);
                mMonths.add((Calendar) next.clone());
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private void sendUpdateMonthAction(int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, mMonths.get(position).getTimeInMillis());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void scrollToToday() {
        scrollTo(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public void scrollTo(long millis) {

        // in case it's out of range, add 12 more months (1 year)
        Calendar destMonth = Calendar.getInstance();
        destMonth.setTimeInMillis(millis);
        if (destMonth.compareTo(mMonths.get(0)) < 0 || destMonth.compareTo(mMonths.get(mMonths.size() - 1)) > 0) {
            while (destMonth.compareTo(mMonths.get(0)) < 0) {
                addOneMoreYear(true);
            }
            while (destMonth.compareTo(mMonths.get(mMonths.size() - 1)) > 0) {
                addOneMoreYear(false);
            }
            mAdapter.notifyDataSetChanged();
        }

        // binary search
        Comparator<Calendar> comparator = new Comparator<Calendar>() {
            @Override
            public int compare(Calendar calendar, Calendar t1) {
                return TimeUtils.compareMonth(calendar.getTimeInMillis(), t1.getTimeInMillis());
            }
        };
        int index = Collections.binarySearch(mMonths, destMonth, comparator);
        if (index >= 0) {
            mMonthViewPager.setCurrentItem(index);
            sendUpdateMonthAction(index);
        }
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
        mMonthViewPager.invalidate();
    }
}
