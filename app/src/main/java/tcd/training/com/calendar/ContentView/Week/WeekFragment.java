package tcd.training.com.calendar.ContentView.Week;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.DataUtils;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.Utils.ViewUtils;

import static tcd.training.com.calendar.R.string.x_more;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class WeekFragment extends Fragment {

    private static final String TAG = WeekFragment.class.getSimpleName();

    public final static String ARG_DISPLAY_DAY = "ARG_DISPLAY_DAY";
    private static final int NUMBER_OF_DISPLAY_EVENTS = 2;

    private Calendar mFirstWeekDay;
    private Context mContext;
    private ArrayList<Entry> mEntries;

    private GridView mGridView;

    public static WeekFragment newInstance(Calendar date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DISPLAY_DAY, date);
        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFirstWeekDay = (Calendar) getArguments().getSerializable(ARG_DISPLAY_DAY);
        }

        mContext = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_week, container, false);

        getAllWeekEntries();

        createHeader(view);

        createWeekGridView(view);

        return view;
    }

    private void getAllWeekEntries() {

        mFirstWeekDay.set(Calendar.HOUR_OF_DAY, 0);
        mFirstWeekDay.set(Calendar.MINUTE, 0);
        mEntries = DataUtils.getEntriesBetween(mContext,
                mFirstWeekDay.getTimeInMillis(),
                mFirstWeekDay.getTimeInMillis() + TimeUnit.DAYS.toMillis(8) - TimeUnit.MINUTES.toMillis(1));
        Collections.sort(mEntries);

        for (int i = 0; i < 7; i++) {
            long millis = mFirstWeekDay.getTimeInMillis() + TimeUnit.DAYS.toMillis(i);
            int index = DataUtils.findEntryIndexWithDate(mEntries, millis);
            if (index < 0) {
                index = -index - 1;
                mEntries.add(index, new Entry(millis, null, null));
            }
        }
    }

    private void createHeader(View view) {
        LinearLayout header = view.findViewById(R.id.ll_week);
        for (int i = 0; i < 7; i++) {
            final Entry entry = mEntries.get(i);
            TextView dayOfMonth = ViewUtils.getTextView(TimeUtils.getFormattedDate(entry.getTime(), "dd"),
                    24, Color.GRAY, Typeface.NORMAL, true, mContext);
            TextView dayOfWeek = ViewUtils.getTextView(TimeUtils.getFormattedDate(entry.getTime(), "EEE"),
                    16, Color.GRAY, Typeface.NORMAL, true, mContext);

            LinearLayout dayLayout = (LinearLayout) header.getChildAt(i + 1);
            dayLayout.addView(dayOfMonth);
            dayLayout.addView(dayOfWeek);

            if (entry.getEvents() != null) {
                int allDayEventsNumber = 0;
                for (int j = 0; j < entry.getEvents().size(); j++) {
                    if (entry.getEvents().get(j).isAllDay()) {
                        if (allDayEventsNumber >= NUMBER_OF_DISPLAY_EVENTS) {
                            dayLayout.addView(ViewUtils.getTextView("+" + (entry.getEvents().size() - NUMBER_OF_DISPLAY_EVENTS),
                                    10, Color.GRAY, Typeface.BOLD, true, mContext));
                            break;
                        }
                        allDayEventsNumber++;
                        dayLayout.addView(ViewUtils.getSimpleTileView(entry.getEvents().get(j), mContext));
                    }
                }
            }

            dayLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.UPDATE_CONTENT_VIEW_ACTION);
                    intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, entry.getTime());
                    intent.putExtra(MainActivity.ARG_CONTENT_VIEW_TYPE, R.id.nav_day);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            });
        }
    }

    private void createWeekGridView(View view) {
        mGridView = view.findViewById(R.id.grid_view);
        DaysOfWeekAdapter adapter = new DaysOfWeekAdapter(mEntries, mContext);
        mGridView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
