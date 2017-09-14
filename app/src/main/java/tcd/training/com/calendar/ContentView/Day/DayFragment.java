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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Entry;
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

        LinearLayout entryLayout = view.findViewById(R.id.ll_entry);
        mDayOfMonthTextView = entryLayout.findViewById(R.id.tv_day_of_month);
        mDayOfWeekTextView = entryLayout.findViewById(R.id.tv_day_of_week);
        mAllDayEventsLayout = entryLayout.findViewById(R.id.ll_events);
        LinearLayout showHideLayout = view.findViewById(R.id.ll_show_hide);
        final ImageView arrowImageView = view.findViewById(R.id.iv_arrow);
        final TextView showHideTextView = view.findViewById(R.id.tv_show_hide);
        RecyclerView eventsListRecyclerView = view.findViewById(R.id.rv_events_list);

        // header
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, ViewUtils.dpToPixel(16), 0, 0);
        entryLayout.setLayoutParams(params);
        mDayOfMonthTextView.setText(String.valueOf(mCurDay.get(Calendar.DAY_OF_MONTH)));
        mDayOfWeekTextView.setText(TimeUtils.getFormattedDate(mCurDay.getTimeInMillis(), "EEE"));
        final Entry entry = DataUtils.findEntryWithDate(mCurDay.getTimeInMillis());
        if (entry != null) {
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, ViewUtils.dpToPixel(0));
            for (int i = 0; i < entry.getEvents().size(); i++) {
                if (entry.getEvents().get(i).isAllDay()) {
                    View eventTileView = ViewUtils.getEventTileView(entry.getEvents().get(i), mContext);
                    eventTileView.setLayoutParams(params);
                    if (i >= 2) {
                        eventTileView.setVisibility(View.GONE);
                    }
                    mAllDayEventsLayout.addView(eventTileView);
                }
            }
            if (entry.getEvents().size() > 2) {
                showHideTextView.setText(String.format(getString(R.string.x_more), entry.getEvents().size() - 2));
                showHideLayout.setVisibility(View.VISIBLE);
            }
        }

        // show more or less events
        final boolean[] isExpanded = {false};
        showHideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int showState;
                if (isExpanded[0]) {
                    showState = View.GONE;
                    arrowImageView.setImageResource(R.drawable.ic_arrow_down_black_24dp);
                    showHideTextView.setText(String.format(getString(R.string.x_more), entry.getEvents().size() - 2));
                } else {
                    showState = View.VISIBLE;
                    arrowImageView.setImageResource(R.drawable.ic_arrow_up_black_24dp);
                    showHideTextView.setText(R.string.show_less);
                }
                isExpanded[0] = !isExpanded[0];
                for (int i = 2; i < mAllDayEventsLayout.getChildCount(); i++) {
                    mAllDayEventsLayout.getChildAt(i).setVisibility(showState);
                }
            }
        });

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
