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

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayViewFragment extends Fragment{

    private static final String TAG = DayViewFragment.class.getSimpleName();
    private static final String ARG_SPECIFIED_DATE = "specificDate";

    private ArrayList<Entry> mEntriesList;
    private ArrayList<Calendar> mDays;
    private long mSpecifiedTime;
    private Context mContext;

    private ViewPager mDayViewPager;

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

        mDays = new ArrayList<>();
        addOneMoreYear(Calendar.getInstance().get(Calendar.YEAR), false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_day_view, container, false);
        mContext = view.getContext();

        initializeUiComponents(view);

        // scroll to specified day (if any), otherwise scroll to today
        if (mSpecifiedTime != -1) {
            scrollTo(mSpecifiedTime);
        } else {
            scrollToToday();
        }

        return view;
    }

    private void initializeUiComponents(View view) {
        mDayViewPager = view.findViewById(R.id.vp_day_view);
        DayPagerAdapter adapter = new DayPagerAdapter(getChildFragmentManager(), mDays);
        mDayViewPager.setAdapter(adapter);

        mDayViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == mDays.size() - 1) {
                    addOneMoreYear(mDays.get(0).get(Calendar.YEAR) + 1, false);
                } else if (position == 0) {
                    addOneMoreYear(mDays.get(0).get(Calendar.YEAR) - 1, true);
                }
                sendUpdateMonthAction(mContext, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void addOneMoreYear(int year, boolean toHead) {
        Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.YEAR, year);
        startDate.set(Calendar.MONTH, 0);
        startDate.set(Calendar.DAY_OF_MONTH, 1);
        Calendar endDate = (Calendar) startDate.clone();
        endDate.set(Calendar.MONTH, 11);
        endDate.set(Calendar.DAY_OF_MONTH, 31);

        if (toHead) {
            while (endDate.compareTo(startDate) > 0) {
                mDays.add(0, (Calendar) startDate.clone());
                endDate.add(Calendar.DAY_OF_MONTH, -1);
            }

        } else {
            while (startDate.compareTo(endDate) < 0) {
                mDays.add((Calendar) startDate.clone());
                startDate.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
    }

    private void sendUpdateMonthAction(Context context, int position) {
        int dayOfMonth = mDays.get(position).get(Calendar.DAY_OF_MONTH);
        if (dayOfMonth == 1 || dayOfMonth == mDays.get(position).getActualMaximum(Calendar.DAY_OF_MONTH)) {
            Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
            intent.putExtra(MainActivity.ARG_CALENDAR, mDays.get(position));
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private void scrollTo(long millis) {
        // TODO: 9/1/17 this is temporary, must be fixed in the future for better performance (consider switching to binary search)
        for (int i = 0; i < mDays.size(); i++) {
            if (TimeUtils.isSameDay(mDays.get(i).getTimeInMillis(), millis)) {
                mDayViewPager.setCurrentItem(i);
                sendUpdateMonthAction(mContext, i);
            }
        }
    }

    public void scrollToToday() {
        scrollTo(Calendar.getInstance().getTimeInMillis());
    }
}