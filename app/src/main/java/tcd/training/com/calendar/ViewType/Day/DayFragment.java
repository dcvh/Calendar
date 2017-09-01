package tcd.training.com.calendar.ViewType.Day;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ViewType.Month.MonthFragment;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayFragment extends Fragment {

    private static final String TAG = DayFragment.class.getSimpleName();

    public final static String ARG_DISPLAY_DAY = "ARG_DISPLAY_DAY";

    private Calendar mCurDay;
    private Context mContext;

    private TextView mDayOfMonthTextView;
    private TextView mDayOfWeekTextView;
    private LinearLayout mAllDayEventsLayout;

    public static DayFragment newInstance(Calendar date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISPLAY_DAY, date);
        DayFragment fragment = new DayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mCurDay = (Calendar) getArguments().getSerializable(ARG_DISPLAY_DAY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_day, container, false);

        mContext = view.getContext();

        initializeUiComponents(view);

        return view;
    }

    private void initializeUiComponents(View view) {

        LinearLayout entryLinearLayout = view.findViewById(R.id.ll_entry);
        mDayOfMonthTextView = entryLinearLayout.findViewById(R.id.tv_day_of_month);
        mDayOfWeekTextView = entryLinearLayout.findViewById(R.id.tv_day_of_week);
        mAllDayEventsLayout = entryLinearLayout.findViewById(R.id.ll_events);
        RecyclerView eventsListRecyclerView = view.findViewById(R.id.rv_events_list);

        // header
        mDayOfMonthTextView.setText(String.valueOf(mCurDay.get(Calendar.DAY_OF_MONTH)));
        mDayOfWeekTextView.setText(CalendarUtils.getDate(mCurDay.getTimeInMillis(), "EEE"));
        CalendarEntry entry = CalendarUtils.findEntryWithDate(CalendarUtils.getDate(mCurDay.getTimeInMillis(), "yyyy/MM/dd"));
        if (entry != null) {
            for (CalendarEvent event : entry.getEvents()) {
                if (event.isAllDay()) {
                    addEventView(event, false, mAllDayEventsLayout);
                }
            }
        }

        // recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        eventsListRecyclerView.setLayoutManager(layoutManager);
        eventsListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DayEventsAdapter adapter = new DayEventsAdapter(entry, mContext);
        eventsListRecyclerView.setAdapter(adapter);
    }

    private void addEventView(CalendarEvent event, boolean containDuration, ViewGroup parent) {

        // dp to pixels
        final float scale = mContext.getResources().getDisplayMetrics().density;
        int dpAsPx_8 = (int) (8 * scale + 0.5f);

        // prepare a linear layout for wrapping title and duration
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, dpAsPx_8);
        LinearLayout eventLayout = new LinearLayout(mContext);
        eventLayout.setOrientation(LinearLayout.VERTICAL);
        eventLayout.setLayoutParams(layoutParams);
        eventLayout.setBackgroundResource(R.drawable.layout_round_corner);

        // title
        TextView titleTextView = new TextView(mContext);
        titleTextView.setText(event.getTitle());
        eventLayout.addView(titleTextView);

        // duration
        if (containDuration) {
            TextView descriptionTextView = new TextView(mContext);
            descriptionTextView.setPadding(0, dpAsPx_8 / 2, 0, 0);
            String duration = CalendarUtils.getDate(event.getStartDate(), "hh") + CalendarUtils.getDate(event.getEndDate(), "hh");
            descriptionTextView.setText(duration);
            eventLayout.addView(descriptionTextView);
        }

        // add to parent
        eventLayout.setPadding(dpAsPx_8 * 2, dpAsPx_8, dpAsPx_8 * 2, dpAsPx_8);
        parent.addView(eventLayout);
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
