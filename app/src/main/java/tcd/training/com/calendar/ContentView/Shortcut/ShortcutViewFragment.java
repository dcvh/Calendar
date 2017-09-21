package tcd.training.com.calendar.ContentView.Shortcut;

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
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.TimeUtils;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ShortcutViewFragment extends Fragment {

    private static final String TAG = ShortcutViewFragment.class.getSimpleName();

    private ArrayList<Calendar> mMonths;
    private Context mContext;

    private WrapContentViewPager mShortcutViewPager;
    private ShortcutPagerAdapter mAdapter;

    public ShortcutViewFragment() {
    }

    public static ShortcutViewFragment newInstance() {
        return new ShortcutViewFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMonths = new ArrayList<>();
        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_shortcut_view, container, false);

        Calendar startDate = Calendar.getInstance();
        startDate.set(2010, 0, 1);
        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.MONTH, 11);
        endDate.set(Calendar.DAY_OF_MONTH, 31);
        while (startDate.compareTo(endDate) < 0) {
            mMonths.add((Calendar) startDate.clone());
            startDate.add(Calendar.MONTH, 1);
        }

        initializeViewPager(view);

        scrollToToday();

        return view;
    }

    private void initializeViewPager(View view) {
        mShortcutViewPager = view.findViewById(R.id.vp_month_view);

        mAdapter = new ShortcutPagerAdapter(getChildFragmentManager(), mMonths);
        mShortcutViewPager.setAdapter(mAdapter);

        mShortcutViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
                    mShortcutViewPager.setCurrentItem(12, false);
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
        intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, mMonths.get(position).getTimeInMillis());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void scrollToToday() {
        long curMonth = Calendar.getInstance().getTimeInMillis();
        for (int i = 0; i < mMonths.size(); i++) {
            if (TimeUtils.compareMonth(mMonths.get(i).getTimeInMillis(), curMonth) == 0) {
                mShortcutViewPager.setCurrentItem(i);
                sendUpdateMonthAction(i);
            }
        }
    }

    public void notifyDataSetChanged() {
        ShortcutFragment fragment = mAdapter.getCurrentFragment();
        fragment.refresh();
    }
}
