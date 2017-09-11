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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthViewFragment extends Fragment {

    private static final String TAG = MonthViewFragment.class.getSimpleName();

    private ArrayList<Entry> mEntriesList;
    private ArrayList<Calendar> mMonths;
    private Context mContext;

    private ViewPager mMonthViewPager;

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
        if (getArguments() != null) {
            mEntriesList = (ArrayList<Entry>) getArguments().getSerializable(ARG_ENTRIES_LIST);
            getArguments().remove(ARG_ENTRIES_LIST);
        } else {
            mEntriesList = new ArrayList<>();
        }
        mMonths = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month_view, container, false);
        mContext = view.getContext();

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

        initializeUiComponents(view);

        scrollToToday();

        return view;
    }

    private void initializeUiComponents(View view) {
        mMonthViewPager = view.findViewById(R.id.vp_month_view);
        MonthPagerAdapter adapter = new MonthPagerAdapter(getChildFragmentManager(), mMonths);
        mMonthViewPager.setAdapter(adapter);

        mMonthViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                sendUpdateMonthAction(mContext, position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void sendUpdateMonthAction(Context context, int position) {
        Intent intent = new Intent(MainActivity.UPDATE_MONTH_ACTION);
        intent.putExtra(MainActivity.ARG_CALENDAR, mMonths.get(position));
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public void scrollToToday() {
        // TODO: 9/1/17 this is temporary, must be fixed in the future for better performance (consider switching to binary search)
        int curYear = Calendar.getInstance().get(Calendar.YEAR);
        int curMonth = Calendar.getInstance().get(Calendar.MONTH);
        for (int i = 0; i < mMonths.size(); i++) {
            if (mMonths.get(i).get(Calendar.YEAR) == curYear && mMonths.get(i).get(Calendar.MONTH) == curMonth) {
                mMonthViewPager.setCurrentItem(i);
                sendUpdateMonthAction(mContext, i);
            }
        }
    }
}
