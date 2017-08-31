package tcd.training.com.calendar.ViewType.Month;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ViewType.Schedule.ScheduleViewFragment;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthViewFragment extends Fragment {

    private static final String TAG = MonthViewFragment.class.getSimpleName();

    private ArrayList<CalendarEntry> mEntriesList;
    private ArrayList<Calendar> mMonths;

    private ViewPager mMonthViewPager;

    public MonthViewFragment() {
    }

    public static MonthViewFragment newInstance(ArrayList<CalendarEntry> entries) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, entries);
        MonthViewFragment fragment = new MonthViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mEntriesList = (ArrayList<CalendarEntry>) getArguments().getSerializable(ARG_ENTRIES_LIST);
            getArguments().remove(ARG_ENTRIES_LIST);
        } else {
            mEntriesList = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month_view, container, false);

        mMonths = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
        try {
            Calendar startDate = Calendar.getInstance();
            startDate.setTime(format.parse("2000/01/01"));
            Calendar endDate = Calendar.getInstance();
            endDate.setTime(format.parse("2030/12/31"));
            while (startDate.compareTo(endDate) < 0) {
                mMonths.add((Calendar) startDate.clone());
                startDate.add(Calendar.MONTH, 1);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mMonthViewPager = view.findViewById(R.id.vp_month_view);
        MonthPagerAdapter adapter = new MonthPagerAdapter(getFragmentManager(), mMonths);
        mMonthViewPager.setAdapter(adapter);

        return view;
    }

    public void scrollToToday() {
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        int curMonth = Calendar.getInstance().get(Calendar.MONTH);
        for (int i = 0; i < mMonths.size(); i++) {
            if (mMonths.get(i).get(Calendar.YEAR) == curYear && mMonths.get(i).get(Calendar.MONTH) == curMonth) {
                mMonthViewPager.setCurrentItem(i);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
