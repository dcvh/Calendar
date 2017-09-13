package tcd.training.com.calendar.ContentView.Day;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ViewUtils;

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
        mDayOfWeekTextView.setText(TimeUtils.getFormattedDate(mCurDay.getTimeInMillis(), "EEE"));
        Entry entry = DataUtils.findEntryWithDate(mCurDay.getTimeInMillis());
        if (entry != null) {
            for (Event event : entry.getEvents()) {
                if (event.isAllDay()) {
                    mAllDayEventsLayout.addView(ViewUtils.getEventTileView(event, mContext));
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    
}
