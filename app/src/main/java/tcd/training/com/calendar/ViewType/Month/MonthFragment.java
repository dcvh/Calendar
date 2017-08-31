package tcd.training.com.calendar.ViewType.Month;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthFragment extends Fragment {

    private static final String TAG = MonthFragment.class.getSimpleName();

    public final static String ARG_DISPLAY_MONTH = "ARG_DISPLAY_MONTH";

    private Calendar mMonth;
    private Context mContext;

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
            mMonth = (Calendar) getArguments().getSerializable(ARG_DISPLAY_MONTH);
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

        // TODO: 8/31/17 this is temporary, must be fixed in the future for better localization
        String[] daysOfWeek = new String[] {"M", "T", "W", "T", "F", "S", "S"};
        for (String dayOfWeek : daysOfWeek) {
            TextView dayTextView = new TextView(mContext);
            dayTextView.setText(dayOfWeek);
            dayTextView.setLayoutParams(layoutParams);
            dayTextView.setTextColor(Color.BLACK);
            dayTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);

            mTableHeader.addView(dayTextView);
        }
    }

    private void createCalendarDates() {

        // TODO: 8/31/17 clean this mess

        // days of the previous month
        Calendar curMonth = (Calendar) mMonth.clone();
        curMonth.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeekInMonth = curMonth.get(Calendar.DAY_OF_WEEK);
        int previousMonthDay = firstDayOfWeekInMonth - curMonth.getFirstDayOfWeek();
        Calendar lastMonth = (Calendar) curMonth.clone();
        lastMonth.add(Calendar.MONTH, -1);
        for (int i = previousMonthDay; i > 0; i--) {
            TextView dateTextView = createDateTextView(Color.GRAY);
            dateTextView.setText(String.valueOf(lastMonth.getActualMaximum(Calendar.DAY_OF_MONTH) - i + 1));
            getRow(0).addView(dateTextView);
        }

        // days of current month
        int daysInMonth = curMonth.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            TableRow row = getRow((i + previousMonthDay - 1) / 7);
            TextView dateTextView = createDateTextView(Color.BLACK);
            dateTextView.setText(String.valueOf(i));
            row.addView(dateTextView);
        }

        // days of the next month
        for (int i = 1; ; i++) {
            int index = (i + daysInMonth + previousMonthDay - 1) / 7;
            if (index >= 6) {
                break;
            }
            TableRow row = getRow(index);
            TextView dateTextView = createDateTextView(Color.GRAY);
            dateTextView.setText(String.valueOf(i));
            row.addView(dateTextView);
        }
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
                new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f);

        // create the text view
        TextView dateTextView = new TextView(mContext);
        dateTextView.setLayoutParams(layoutParams);

        // customize it
        dateTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        dateTextView.setTextColor(color);

        return dateTextView;
    }

    private int dpToPixel(int dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
