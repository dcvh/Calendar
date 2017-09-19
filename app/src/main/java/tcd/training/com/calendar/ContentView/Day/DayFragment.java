package tcd.training.com.calendar.ContentView.Day;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.ViewUtils;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayFragment extends Fragment {

    private static final String TAG = DayFragment.class.getSimpleName();

    public final static String ARG_DISPLAY_DAY = "ARG_DISPLAY_DAY";
    private static final int NUMBER_OF_SHOWN_EVENTS  = 2;

    private Calendar mCurDay;
    private Context mContext;
    private Entry mEntry;
    private boolean mShowLunarDate;

    private LinearLayout mHeaderLayout;
    private LinearLayout mAllDayEventsLayout;

    private LinearLayout mShowHideLayout;
    private TextView mShowHideTextView;

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

        mContext = getContext();
        mShowLunarDate = PreferenceManager.getDefaultSharedPreferences(mContext)
                .getBoolean(getString(R.string.pref_key_show_lunar_calendar), false);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_day, container, false);

        initializeUiComponents(view);
        createHeader();
        createShowHideLayout();
        initializeTimelineRecyclerView(view);

        return view;
    }

    private void initializeUiComponents(View view) {
        mShowHideLayout = view.findViewById(R.id.ll_show_hide);
        mShowHideTextView = view.findViewById(R.id.tv_show_hide);

        mHeaderLayout = view.findViewById(R.id.ll_entry);
        mAllDayEventsLayout = mHeaderLayout.findViewById(R.id.ll_events);
    }

    private void createHeader() {

        TextView dayOfMonthTextView = mHeaderLayout.findViewById(R.id.tv_day_of_month);
        TextView dayOfWeekTextView = mHeaderLayout.findViewById(R.id.tv_day_of_week);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, ViewUtils.dpToPixel(16), 0, 0);
        mHeaderLayout.setLayoutParams(params);

        dayOfMonthTextView.setText(String.valueOf(mCurDay.get(Calendar.DAY_OF_MONTH)));
        dayOfWeekTextView.setText(TimeUtils.getFormattedDate(mCurDay.getTimeInMillis(), "EEE"));

        if (mShowLunarDate) {
            TextView lunarDateTextView = mHeaderLayout.findViewById(R.id.tv_lunar_day);
            lunarDateTextView.setText(TimeUtils.getLunarString(mCurDay.getTimeInMillis()));
            lunarDateTextView.setVisibility(View.VISIBLE);
        }

        mEntry = DataUtils.getEntryIn(mCurDay.getTimeInMillis(), mContext);
        if (mEntry != null) {

            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, ViewUtils.dpToPixel(0));

            // create event tiles
            int allDayEventsNumber = 0;
            for (int i = 0; i < mEntry.getEvents().size(); i++) {
                if (mEntry.getEvents().get(i).isAllDay()) {
                    allDayEventsNumber++;
                    View eventTileView = ViewUtils.getEventTileView(mEntry.getEvents().get(i), mContext);
                    eventTileView.setLayoutParams(params);
                    if (i >= NUMBER_OF_SHOWN_EVENTS) {
                        eventTileView.setVisibility(View.GONE);
                    }
                    mAllDayEventsLayout.addView(eventTileView);
                }
            }

            // hide some if number of events exceeds limit
            if (allDayEventsNumber > NUMBER_OF_SHOWN_EVENTS) {
                mShowHideTextView.setText(String.format(getString(R.string.x_more), mEntry.getEvents().size() - NUMBER_OF_SHOWN_EVENTS));
                mShowHideLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void createShowHideLayout() {

        final ImageView arrowImageView = mShowHideLayout.findViewById(R.id.iv_arrow);

        final boolean[] isExpanded = {false};
        mShowHideLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int showState;
                if (isExpanded[0]) {
                    showState = View.GONE;
                    arrowImageView.setImageResource(R.drawable.ic_arrow_down_black_24dp);
                    mShowHideTextView.setText(String.format(getString(R.string.x_more), mEntry.getEvents().size() - 2));
                } else {
                    showState = View.VISIBLE;
                    arrowImageView.setImageResource(R.drawable.ic_arrow_up_black_24dp);
                    mShowHideTextView.setText(R.string.show_less);
                }
                isExpanded[0] = !isExpanded[0];
                for (int i = 2; i < mAllDayEventsLayout.getChildCount(); i++) {
                    mAllDayEventsLayout.getChildAt(i).setVisibility(showState);
                }
            }
        });
    }

    private void initializeTimelineRecyclerView(View view) {

        RecyclerView eventsListRecyclerView = view.findViewById(R.id.rv_events_list);

        // recycler view
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        eventsListRecyclerView.setLayoutManager(layoutManager);
        eventsListRecyclerView.setItemAnimator(new DefaultItemAnimator());
        DayEventsAdapter adapter = new DayEventsAdapter(mEntry, mContext);
        eventsListRecyclerView.setAdapter(adapter);
    }
    
}
