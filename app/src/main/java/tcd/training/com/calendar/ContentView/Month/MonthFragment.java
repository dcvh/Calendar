package tcd.training.com.calendar.ContentView.Month;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.format.DateUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Utils.PreferenceUtils;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.ViewUtils;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthFragment extends Fragment {

    private static final String TAG = MonthFragment.class.getSimpleName();
    private static final int DEFAULT_TEXT_SIZE = 10;
    private static final int NUMBER_OF_DISPLAY_EVENTS = 3;

    public final static String ARG_DISPLAY_MONTH = "ARG_DISPLAY_MONTH";

    private Context mContext;
    private int mFirstDayOfWeek;
    private Calendar mCurMonth;
    private String[] mDayOrder;
    private Calendar mStartDate, mEndDate;
    private ArrayList<Entry> mEntries;
    private String mAlternateCalendar;

    private TableRow mTableHeader;
    private TableRow mTableRow1;
    private TableRow mTableRow2;
    private TableRow mTableRow3;
    private TableRow mTableRow4;
    private TableRow mTableRow5;
    private TableRow mTableRow6;

    public static MonthFragment newInstance(Calendar date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISPLAY_MONTH, date);
        MonthFragment fragment = new MonthFragment();
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
        mAlternateCalendar = PreferenceUtils.getAlternateCalendar(mContext);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month, container, false);

        generateDisplayDays();
        mEntries = DataUtils.getEntriesBetween(mContext, mStartDate.getTimeInMillis(), mEndDate.getTimeInMillis());

        initializeUiComponents(view);
        createCalendarHeader();
        createCalendarDates();

        return view;
    }

    private void initializeUiComponents(View view) {
        mTableHeader = view.findViewById(R.id.tr_header);
        mTableRow1 = view.findViewById(R.id.tr_1);
        mTableRow2 = view.findViewById(R.id.tr_2);
        mTableRow3 = view.findViewById(R.id.tr_3);
        mTableRow4 = view.findViewById(R.id.tr_4);
        mTableRow5 = view.findViewById(R.id.tr_5);
        mTableRow6 = view.findViewById(R.id.tr_6);
    }

    private void generateDisplayDays() {

        mDayOrder = getDayOfWeekOrder();

        // getField the number of days in previous month
        mStartDate = (Calendar) mCurMonth.clone();
        mStartDate.set(Calendar.DAY_OF_MONTH, 1);
        int previousMonthDay = mStartDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
        if (previousMonthDay < 0) {
            previousMonthDay += 7;
        }
        mStartDate.add(Calendar.DAY_OF_MONTH, -previousMonthDay);
        mStartDate.set(Calendar.HOUR_OF_DAY, 0);
        mStartDate.set(Calendar.MINUTE, 0);

        // getField the number of days in next month
        int daysInMonth = mCurMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        mEndDate = (Calendar) mCurMonth.clone();
        mEndDate.add(Calendar.MONTH, 1);
        mEndDate.set(Calendar.DAY_OF_MONTH, 1);
        for (int i = 1; ; i++) {
            int index = (i + daysInMonth + previousMonthDay - 1) / 7;
            if (index >= 6) {
                break;
            }
            mEndDate.add(Calendar.DAY_OF_MONTH, 1);
        }
        mStartDate.set(Calendar.HOUR_OF_DAY, 23);
        mStartDate.set(Calendar.MINUTE, 59);
    }

    private void createCalendarHeader() {

        TableRow.LayoutParams layoutParams =
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        for (String dayOfWeek : mDayOrder) {
            TextView dayTextView = new TextView(mContext);
            dayTextView.setText(dayOfWeek);
            dayTextView.setLayoutParams(layoutParams);
            dayTextView.setTextColor(Color.BLACK);
            dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

            mTableHeader.addView(dayTextView);
        }
    }

    private String[] getDayOfWeekOrder() {
        mFirstDayOfWeek = PreferenceUtils.getFirstDayOfWeek(mContext);
        return PreferenceUtils.getDayOfWeekOrder(mFirstDayOfWeek, mContext);
    }

    private void createCalendarDates() {

        Calendar curDate = (Calendar) mStartDate.clone();
        int index = 0;

        // days of previous month
        int previousMonth = mCurMonth.get(Calendar.MONTH) - 1;
        while ((int)curDate.get(Calendar.MONTH) == previousMonth) {
            getRow(0).addView(createDateView(curDate, Color.GRAY));
            curDate.add(Calendar.DAY_OF_MONTH, 1);
            index++;
        }


        // days of current month
        int curMonth = mCurMonth.get(Calendar.MONTH);
        while (curDate.get(Calendar.MONTH) == curMonth) {
            TableRow row = getRow(index++ / 7);
            row.addView(createDateView(curDate, Color.BLACK));
            curDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        // days of the next month
        while (index < 42) {
            TableRow row = getRow(index++ / 7);
            row.addView(createDateView(curDate, Color.GRAY));
            curDate.add(Calendar.DAY_OF_MONTH, 1);
        }

//        TextView textView = new TextView(mContext);
//        textView.setTextColor(Color.BLACK);
//        textView.setText(curMonth.getField(Calendar.MONTH) + "/" + curMonth.getField(Calendar.YEAR));
//        mCalendarTable.addView(textView);
    }

    private View createDateView(final Calendar calendar, int dateColor) {

        // prepare date and lunar date
        String dateString = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (mAlternateCalendar != null) {
            dateString += "\n" + PreferenceUtils.getAlternateDate(calendar.getTimeInMillis(), mAlternateCalendar);
        }
        final TextView dateTextView = getTextView(dateString, DEFAULT_TEXT_SIZE, dateColor, Typeface.NORMAL);

        // tint today
        if (DateUtils.isToday(calendar.getTimeInMillis())) {
            dateTextView.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        }

        Entry entry = DataUtils.findEntryWithDate(mEntries, calendar.getTimeInMillis());

        View resultView;
        if (entry == null) {
            dateTextView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            resultView = dateTextView;
        } else {
            dateTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout layout = new LinearLayout(mContext);
            layout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f));
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.addView(dateTextView);

            for (int i = 0; i < entry.getEvents().size(); i++) {

                Event event = entry.getEvents().get(i);

                if (i >= NUMBER_OF_DISPLAY_EVENTS) {
                    String x_more = String.format(getString(R.string.x_more), entry.getEvents().size() - NUMBER_OF_DISPLAY_EVENTS);
                    layout.addView(getTextView(x_more, DEFAULT_TEXT_SIZE, Color.BLACK, Typeface.BOLD));
                    break;
                }

                layout.addView(ViewUtils.getSimpleTileView(event, mContext));
            }

            resultView = layout;
        }

        final long timeInMillis = calendar.getTimeInMillis();
        resultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.UPDATE_CONTENT_VIEW_ACTION);
                intent.putExtra(MainActivity.ARG_CONTENT_VIEW_TYPE, R.id.nav_day);
                intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, timeInMillis);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

        return resultView;
    }

    private TextView getTextView(String content, int size, int color, int style) {
        TextView textView = new TextView(mContext);

        textView.setText(content);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
        textView.setTextColor(color);
        textView.setTypeface(null, style);

        return textView;
    }

    private TableRow getRow(int index) {
        switch (index) {
            case 0: return mTableRow1;
            case 1: return mTableRow2;
            case 2: return mTableRow3;
            case 3: return mTableRow4;
            case 4: return mTableRow5;
            case 5: return mTableRow6;
            default:
                throw new UnsupportedOperationException("Unknown index");
        }
    }
}
