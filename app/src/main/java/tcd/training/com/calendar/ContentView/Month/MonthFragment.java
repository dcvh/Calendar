package tcd.training.com.calendar.ContentView.Month;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ContentView.Day.DayViewFragment;
import tcd.training.com.calendar.ContentView.ViewUtils;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthFragment extends Fragment {

    private static final String TAG = MonthFragment.class.getSimpleName();

    public final static String ARG_DISPLAY_MONTH = "ARG_DISPLAY_MONTH";

    private Calendar mCurMonth;
    private Context mContext;
    private int mFirstDayOfWeek;

    private TableLayout mCalendarTable;
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_month, container, false);

        mContext = view.getContext();

        initializeUiComponents(view);

        createCalendarHeader();
        createCalendarDates();

        return view;
    }

    private void initializeUiComponents(View view) {
        mCalendarTable = view.findViewById(R.id.tl_month_view);
        mTableHeader = view.findViewById(R.id.tr_header);
        mTableRow1 = view.findViewById(R.id.tr_1);
        mTableRow2 = view.findViewById(R.id.tr_2);
        mTableRow3 = view.findViewById(R.id.tr_3);
        mTableRow4 = view.findViewById(R.id.tr_4);
        mTableRow5 = view.findViewById(R.id.tr_5);
        mTableRow6 = view.findViewById(R.id.tr_6);
    }

    private void createCalendarHeader() {

        TableRow.LayoutParams layoutParams =
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        String[] daysOfWeek = getDayOfWeekOrder();

        for (String dayOfWeek : daysOfWeek) {
            TextView dayTextView = new TextView(mContext);
            dayTextView.setText(dayOfWeek);
            dayTextView.setLayoutParams(layoutParams);
            dayTextView.setTextColor(Color.BLACK);
            dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

            mTableHeader.addView(dayTextView);
        }
    }

    private String[] getDayOfWeekOrder() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String firstDay = sharedPreferences.getString(getString(R.string.pref_key_start_of_the_week), "Monday");

        String[] daysOfWeek;
        switch (firstDay) {
            case "Saturday":
                daysOfWeek = getResources().getStringArray(R.array.first_day_saturday);
                mFirstDayOfWeek = Calendar.SATURDAY;
                break;
            case "Sunday":
                daysOfWeek = getResources().getStringArray(R.array.first_day_sunday);
                mFirstDayOfWeek = Calendar.SUNDAY;
                break;
            case "Monday":
                daysOfWeek = getResources().getStringArray(R.array.first_day_monday);
                mFirstDayOfWeek = Calendar.MONDAY;
                break;
            default:
                throw new UnsupportedOperationException("Unknown first day");
        }

        return daysOfWeek;
    }

    private void createCalendarDates() {

        // TODO: 8/31/17 clean this mess

        // determine number of days of the previous month will be shown
        Calendar curMonth = (Calendar) mCurMonth.clone();
        curMonth.set(Calendar.DAY_OF_MONTH, 1);
        int previousMonthDay = curMonth.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek;
        if (previousMonthDay < 0) {
            previousMonthDay += 7;
        }

        // days of previous month
        Calendar lastMonth = (Calendar) curMonth.clone();
        assert Integer.valueOf(lastMonth.get(Calendar.DAY_OF_MONTH)) == 1;
        lastMonth.add(Calendar.DAY_OF_MONTH, -previousMonthDay);
        for (int i = 0; i < previousMonthDay; i++) {
            getRow(0).addView(createDateView(lastMonth, Color.GRAY));
            lastMonth.add(Calendar.DAY_OF_MONTH, 1);
        }

        // days of current month
        int daysInMonth = curMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        curMonth.set(Calendar.DAY_OF_MONTH, 1);
        for (int i = 1; i <= daysInMonth; i++) {
            TableRow row = getRow((i + previousMonthDay - 1) / 7);

            curMonth.set(Calendar.DAY_OF_MONTH, i);
            row.addView(createDateView(curMonth, Color.BLACK));
        }

        // days of the next month
        Calendar nextMonth = (Calendar) mCurMonth.clone();
        nextMonth.add(Calendar.MONTH, 1);
        nextMonth.set(Calendar.DAY_OF_MONTH, 1);
        for (int i = 1; ; i++) {
            int index = (i + daysInMonth + previousMonthDay - 1) / 7;
            if (index >= 6) {
                break;
            }
            TableRow row = getRow(index);
            row.addView(createDateView(nextMonth, Color.GRAY));
            nextMonth.add(Calendar.DAY_OF_MONTH, 1);
        }

//        TextView textView = createDateTextView(Color.BLACK);
//        textView.setText(curMonth.get(Calendar.MONTH) + "/" + curMonth.get(Calendar.YEAR));
//        mCalendarTable.addView(textView);
    }

    private View createDateView(final Calendar calendar, int dateColor) {
        TextView dateTextView = createDateTextView(dateColor);
        dateTextView.setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));

        final Entry entry = DataUtils.findEntryWithDate(calendar.getTimeInMillis());

        View resultView;
        if (entry == null) {
            resultView = dateTextView;
        } else {
            LinearLayout layout = new LinearLayout(mContext);
            layout.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(dateTextView);
            for (Event event : entry.getEvents()) {
                TextView eventTextView = ViewUtils.getStandardTextView(event.getTitle(), mContext);
                eventTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

                eventTextView.setBackgroundResource(R.drawable.layout_round_corner);
                GradientDrawable drawable = (GradientDrawable) eventTextView.getBackground();
                drawable.setColor(DataUtils.getAccountColor(event.getCalendarId()));
                layout.addView(eventTextView);
            }
            resultView = layout;
        }

        resultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DayViewFragment newFragment = DayViewFragment.newInstance(calendar.getTimeInMillis());
                FragmentTransaction transaction = getParentFragment().getFragmentManager().beginTransaction();
                transaction
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fl_content, newFragment)
                        .commit();
            }
        });

        return resultView;
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

    private TextView createDateTextView(int color) {

        TableRow.LayoutParams layoutParams =
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT, 1f);

        // create the text view
        TextView dateTextView = new TextView(mContext);
        dateTextView.setLayoutParams(layoutParams);

        // customize it
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dateTextView.setTextColor(color);

        return dateTextView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
