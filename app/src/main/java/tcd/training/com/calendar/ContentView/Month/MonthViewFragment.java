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

import tcd.training.com.calendar.ContentView.ContentViewBehaviors;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthViewFragment extends Fragment implements ContentViewBehaviors{

    private static final String TAG = MonthViewFragment.class.getSimpleName();

    private ArrayList<Calendar> mMonths;
    private Context mContext;

    private ViewPager mMonthViewPager;
    private MonthPagerAdapter mAdapter;

    public MonthViewFragment() {
    }

    public static MonthViewFragment newInstance() {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, DataUtils.getAllEntries());
        MonthViewFragment fragment = new MonthViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mEntriesList = (ArrayList<Entry>) getArguments().getSerializable(ARG_ENTRIES_LIST);
//            getArguments().remove(ARG_ENTRIES_LIST);
//        } else {
//            mEntriesList = new ArrayList<>();
//        }
        mMonths = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month_view, container, false);
        mContext = view.getContext();

        Calendar startDate = Calendar.getInstance();
        startDate.set(2010, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.MONTH, 11);
        endDate.set(Calendar.DAY_OF_MONTH, 31);
        while (startDate.compareTo(endDate) < 0) {
            mMonths.add((Calendar) startDate.clone());
            startDate.add(Calendar.MONTH, 1);
        }

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
                    addOneMoreYear(mMonths.get(mMonths.size() - 1).get(Calendar.YEAR) + 1, false);
                    mAdapter.notifyDataSetChanged();
                } else if (position == 0) {
                    addOneMoreYear(mMonths.get(0).get(Calendar.YEAR) - 1, true);
                    mAdapter.notifyDataSetChanged();
                    mMonthViewPager.setCurrentItem(12, false);
                }
                sendUpdateMonthAction(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addOneMoreYear(int year, boolean toTop) {

        Calendar startDate = Calendar.getInstance();
        startDate.set(year, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(year, 11, 31);

        if (toTop) {
            while (endDate.compareTo(startDate) > 0) {
                mMonths.add(0, (Calendar)endDate.clone());
                endDate.add(Calendar.MONTH, -1);
            }

        } else {
            while (startDate.compareTo(endDate) < 0) {
                mMonths.add((Calendar) startDate.clone());
                startDate.add(Calendar.MONTH, 1);
            }
        }
    }

    private void sendUpdateMonthAction(int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_CALENDAR, mMonths.get(position));
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    @Override
    public void scrollToToday() {
        scrollTo(Calendar.getInstance().getTimeInMillis());
    }

    @Override
    public void scrollTo(long millis) {
        int low = 0;
        int high = mMonths.size() - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            int comparison = TimeUtils.compareMonth(millis, mMonths.get(mid).getTimeInMillis());
            if (comparison < 0) {
                high = mid - 1;
            } else if (comparison > 0) {
                low = mid + 1;
            } else {
                mMonthViewPager.setCurrentItem(mid);
                sendUpdateMonthAction(mid);
                return;
            }
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
