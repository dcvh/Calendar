package tcd.training.com.calendar.ContentView.Shortcut;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Utils.PreferenceUtils;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.Utils.ViewUtils;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ShortcutFragment extends Fragment {

    private static final String TAG = ShortcutFragment.class.getSimpleName();
    private static final int DEFAULT_TEXT_SIZE = 12;

    public final static String ARG_DISPLAY_MONTH = "ARG_DISPLAY_MONTH";

    private LinearLayout mLayout;

    private Context mContext;
    private int mFirstDayOfWeek;
    private Calendar mCurMonth;
    private String[] mDayOrder;
    private Calendar mStartDate, mEndDate;
    private ArrayList<Entry> mEntries;

    public static ShortcutFragment newInstance(Calendar date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISPLAY_MONTH, date);
        ShortcutFragment fragment = new ShortcutFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurMonth = (Calendar) getArguments().getSerializable(ARG_DISPLAY_MONTH);
        }

        mContext = getContext();

        generateDisplayDays();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mEntries = DataUtils.getEntriesBetween(mContext, mStartDate.getTimeInMillis(), mEndDate.getTimeInMillis());
                return null;
            }
        }.execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_shortcut, container, false);

        mLayout = view.findViewById(R.id.ll_month_shortcut);

        createHeader();
        createCalendar();

        return view;
    }

    private void generateDisplayDays() {

        mDayOrder = getDayOfWeekOrder();

        // get the number of days in previous month
        mStartDate = (Calendar) mCurMonth.clone();
        mStartDate.set(Calendar.DAY_OF_MONTH, 1);
        int previousMonthDay = mStartDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
        if (previousMonthDay < 0) {
            previousMonthDay += 7;
        }
        mStartDate.add(Calendar.DAY_OF_MONTH, -previousMonthDay);
        mStartDate.set(Calendar.HOUR_OF_DAY, 0);
        mStartDate.set(Calendar.MINUTE, 0);

        // get the number of days in next month
        mEndDate = (Calendar) mCurMonth.clone();
        mEndDate.set(Calendar.DAY_OF_MONTH, mEndDate.getActualMaximum(Calendar.DAY_OF_MONTH));
        int nextMonthDay = 7 - (mEndDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek + 1);
        if (nextMonthDay >= 7) {
            nextMonthDay -= 7;
        }
        mEndDate.add(Calendar.DAY_OF_MONTH, nextMonthDay);
        mEndDate.set(Calendar.HOUR_OF_DAY, 23);
        mEndDate.set(Calendar.MINUTE, 59);
    }

    private String[] getDayOfWeekOrder() {
        mFirstDayOfWeek = PreferenceUtils.getFirstDayOfWeek(mContext);
        return PreferenceUtils.getDayOfWeekOrder(mFirstDayOfWeek, mContext);
    }

    private void createHeader() {
        LinearLayout header = getRow();
        for (String dayOfWeek : mDayOrder) {
            header.addView(getTextView(dayOfWeek, DEFAULT_TEXT_SIZE, Color.GRAY, Typeface.NORMAL));
        }
        mLayout.addView(header);
    }

    private LinearLayout getRow() {
        LinearLayout row = new LinearLayout(mContext);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, ViewUtils.dpToPixel(16));
        row.setLayoutParams(layoutParams);
        return row;
    }

    private void createCalendar() {

        Calendar curDate = (Calendar) mStartDate.clone();
        int curMonth = mCurMonth.get(Calendar.MONTH);

        LinearLayout row = getRow();
        for (int index = 1; curDate.compareTo(mEndDate) <= 0; index++) {
            if (curDate.get(Calendar.MONTH) != curMonth) {
                row.addView(getTextView("", DEFAULT_TEXT_SIZE, Color.TRANSPARENT, Typeface.NORMAL));
            } else {
                row.addView(createDateView(curDate, Color.BLACK));
            }
            curDate.add(Calendar.DAY_OF_MONTH, 1);
            if (index % 7 == 0) {
                mLayout.addView(row);
                row = getRow();
            }
        }
    }

    private View createDateView(final Calendar calendar, int dateColor) {

        // prepare date and lunar date
        String dateString = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        final TextView dateTextView = getTextView(dateString, DEFAULT_TEXT_SIZE, dateColor, Typeface.NORMAL);

        // tint today
        if (DateUtils.isToday(calendar.getTimeInMillis())) {
            dateTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        Entry entry = DataUtils.findEntryWithDate(mEntries, calendar.getTimeInMillis());
        if (entry != null) {
        }
        if (entry != null && entry.getEvents().size() > 0) {
            dateTextView.setTextColor(entry.getEvents().get(0).getDisplayColor());
        }

        final long timeInMillis = calendar.getTimeInMillis();
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.SCROLL_TO_ACTION);
                intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, timeInMillis);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

        return dateTextView;
    }

    private TextView getTextView(String content, int size, int color, int style) {
        TextView textView = new TextView(mContext);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textView.setLayoutParams(params);
        textView.setGravity(Gravity.CENTER);

        textView.setText(content);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        textView.setTextColor(color);
        textView.setTypeface(null, style);

        return textView;
    }

    public void refresh() {
        if (mLayout != null) {
            mLayout.removeAllViews();
            createHeader();
            createCalendar();
        }
    }
}
