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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Utils.PreferenceUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.ViewUtils;

import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthFragment extends Fragment {

    private static final String TAG = MonthFragment.class.getSimpleName();
    private static int MONTH_NUMBER_TEXT_SIZE;
    private static int NUMBER_OF_DISPLAY_EVENTS = 3;

    public final static String ARG_DISPLAY_MONTH = "ARG_DISPLAY_MONTH";

    private Context mContext;
    private int mFirstDayOfWeek;
    private Calendar mCurMonth;
    private String[] mDayOrder;
    private Calendar mStartDate, mEndDate;
    private ArrayList<Entry> mEntries;
    private String mAlternateCalendar;
    private boolean mShowNumberOfWeek;

    private LinearLayout mLayoutHeader;
    private LinearLayout mLayoutRow1;
    private LinearLayout mLayoutRow2;
    private LinearLayout mLayoutRow3;
    private LinearLayout mLayoutRow4;
    private LinearLayout mLayoutRow5;
    private LinearLayout mLayoutRow6;

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
        mShowNumberOfWeek = PreferenceUtils.isShowNumberOfWeekChecked(mContext);

        NUMBER_OF_DISPLAY_EVENTS = getResources().getConfiguration().orientation == ORIENTATION_PORTRAIT ? 3 : 0;
        MONTH_NUMBER_TEXT_SIZE = ViewUtils.pixelToSp(getResources().getDimension(R.dimen.month_number_text_size));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month, container, false);

        generateDisplayDays();
        mEntries = DataUtils.getEntriesBetween(mContext, mStartDate.getTimeInMillis(), mEndDate.getTimeInMillis());

        initializeUiComponents(view);
        createWeekNumber();
        createCalendarHeader();
        createCalendarDates();

        return view;
    }

    private void initializeUiComponents(View view) {
        mLayoutHeader = view.findViewById(R.id.ll_row_header);
        mLayoutRow1 = view.findViewById(R.id.ll_row_1);
        mLayoutRow2 = view.findViewById(R.id.ll_row_2);
        mLayoutRow3 = view.findViewById(R.id.ll_row_3);
        mLayoutRow4 = view.findViewById(R.id.ll_row_4);
        mLayoutRow5 = view.findViewById(R.id.ll_row_5);
        mLayoutRow6 = view.findViewById(R.id.ll_row_6);
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

        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        for (String dayOfWeek : mDayOrder) {
            TextView dayTextView = ViewUtils.getTextView(dayOfWeek, MONTH_NUMBER_TEXT_SIZE + 2, Color.GRAY, Typeface.BOLD, true, mContext);
            dayTextView.setLayoutParams(layoutParams);

            mLayoutHeader.addView(dayTextView);
        }
    }

    private String[] getDayOfWeekOrder() {
        mFirstDayOfWeek = PreferenceUtils.getFirstDayOfWeek(mContext);
        return PreferenceUtils.getDayOfWeekOrder(mFirstDayOfWeek, mContext);
    }

    private void createWeekNumber() {
        if (mShowNumberOfWeek) {

            LinearLayout.LayoutParams params =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, ViewUtils.dpToPixel(8), 0);
            TextView weekNumberTextView;

            weekNumberTextView = ViewUtils.getTextView("00", MONTH_NUMBER_TEXT_SIZE, Color.TRANSPARENT, Typeface.NORMAL, true, mContext);
            weekNumberTextView.setLayoutParams(params);
            mLayoutHeader.addView(weekNumberTextView);

            Calendar lastWeekDay = (Calendar) mStartDate.clone();
            lastWeekDay.add(Calendar.DAY_OF_MONTH, 6);
            lastWeekDay.setFirstDayOfWeek(mFirstDayOfWeek);
            for (int i = 0; i < 6; i++) {
                String weekNumber = String.valueOf(lastWeekDay.get(Calendar.WEEK_OF_YEAR));
                weekNumberTextView =ViewUtils.getTextView(weekNumber, MONTH_NUMBER_TEXT_SIZE, Color.GRAY, Typeface.NORMAL, true, mContext);
                weekNumberTextView.setLayoutParams(params);
                getRow(i).addView(weekNumberTextView);

                lastWeekDay.add(Calendar.DAY_OF_MONTH, 7);
            }
        }
    }

    private void createCalendarDates() {

        Calendar curDate = (Calendar) mStartDate.clone();
        int index = 0;

        // days of previous month
        Calendar prevMonthCal = (Calendar) mCurMonth.clone();
        prevMonthCal.add(Calendar.MONTH, -1);
        int prevMonth = prevMonthCal.get(Calendar.MONTH);
        while (curDate.get(Calendar.MONTH) == prevMonth) {
            getRow(0).addView(createDateView(curDate, Color.GRAY));
            curDate.add(Calendar.DAY_OF_MONTH, 1);
            index++;
        }

        // days of current month
        int curMonth = mCurMonth.get(Calendar.MONTH);
        while (curDate.get(Calendar.MONTH) == curMonth) {
            LinearLayout row = getRow(index++ / 7);
            row.addView(createDateView(curDate, Color.BLACK));
            curDate.add(Calendar.DAY_OF_MONTH, 1);
        }

        // days of the next month
        while (index < 42) {
            LinearLayout row = getRow(index++ / 7);
            row.addView(createDateView(curDate, Color.GRAY));
            curDate.add(Calendar.DAY_OF_MONTH, 1);
        }

//        TextView textView = new TextView(mContext);
//        textView.setTextColor(Color.BLACK);
//        textView.setText(curMonth.getField(Calendar.MONTH) + "/" + curMonth.getField(Calendar.YEAR));
//        mCalendarTable.addView(textView);
    }

    private View createDateView(final Calendar calendar, int dateColor) {

        LinearLayout dateLayout = new LinearLayout(mContext);
        dateLayout.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        dateLayout.setOrientation(LinearLayout.VERTICAL);

        int color = DateUtils.isToday(calendar.getTimeInMillis()) ? ContextCompat.getColor(mContext, R.color.colorAccent) : dateColor;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // original date
        String dateString = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        TextView dateTextView = ViewUtils.getTextView(dateString, MONTH_NUMBER_TEXT_SIZE, color, Typeface.NORMAL, true, mContext);
        dateTextView.setLayoutParams(params);
        dateLayout.addView(dateTextView);
        // alternate date
        if (mAlternateCalendar != null) {
            dateString = PreferenceUtils.getAlternateDate(calendar.getTimeInMillis(), mAlternateCalendar);
            TextView alternateTextView = ViewUtils.getTextView(dateString, MONTH_NUMBER_TEXT_SIZE, color, Typeface.NORMAL, true, mContext);
            alternateTextView.setLayoutParams(params);
            dateLayout.addView(alternateTextView);
        }

        Entry entry = DataUtils.findEntryWithDate(mEntries, calendar.getTimeInMillis());
        if (entry != null) {
            for (int i = 0; i < entry.getEvents().size(); i++) {
                Event event = entry.getEvents().get(i);

                if (i >= NUMBER_OF_DISPLAY_EVENTS) {
                    String x_more = String.format(getString(R.string.x_more), entry.getEvents().size() - NUMBER_OF_DISPLAY_EVENTS);
                    dateLayout.addView(ViewUtils.getTextView(x_more, MONTH_NUMBER_TEXT_SIZE, Color.BLACK, Typeface.BOLD, false, mContext));
                    break;
                }

                dateLayout.addView(ViewUtils.getSimpleTileView(event, mContext));
            }

        }

        final long timeInMillis = calendar.getTimeInMillis();
        dateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.UPDATE_CONTENT_VIEW_ACTION);
                intent.putExtra(MainActivity.ARG_CONTENT_VIEW_TYPE, R.id.nav_day);
                intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, timeInMillis);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

        return dateLayout;
    }

    private LinearLayout getRow(int index) {
        switch (index) {
            case 0: return mLayoutRow1;
            case 1: return mLayoutRow2;
            case 2: return mLayoutRow3;
            case 3: return mLayoutRow4;
            case 4: return mLayoutRow5;
            case 5: return mLayoutRow6;
            default:
                throw new UnsupportedOperationException("Unknown index");
        }
    }
}
