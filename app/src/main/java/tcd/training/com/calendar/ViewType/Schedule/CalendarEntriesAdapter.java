package tcd.training.com.calendar.ViewType.Schedule;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tcd.training.com.calendar.EventDetailsActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.ViewType.Day.DayViewFragment;
import tcd.training.com.calendar.ViewType.ViewUtils;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEntriesAdapter extends RecyclerView.Adapter<CalendarEntriesAdapter.CalendarViewHolder> {

    private static final String TAG = CalendarEntriesAdapter.class.getSimpleName();

    private ArrayList<CalendarEntry> mEntriesList;
    private Context mContext;

    public CalendarEntriesAdapter(Context context, ArrayList<CalendarEntry> mEntriesList) {
        this.mContext = context;
        this.mEntriesList = mEntriesList;
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.calendar_entry_list_item, parent, false);
        CalendarViewHolder viewHolder = new CalendarViewHolder(view);
        return viewHolder;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mEntriesList.size();
    }

    @Override
    public void onBindViewHolder(final CalendarViewHolder holder, int position) {

        final CalendarEntry entry = mEntriesList.get(position);

        // day and month
        holder.mDayOfMonthTextView.setText(CalendarUtils.getDate(entry.getDate(), "d"));
        holder.mDayOfWeekTextView.setText(CalendarUtils.getDate(entry.getDate(), "EEE"));

        holder.mDayOfMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DayViewFragment newFragment = DayViewFragment.newInstance(entry.getDate());
                FragmentTransaction transaction = ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction();
                transaction
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fl_content, newFragment)
                        .commit();
            }
        });

        Log.e(TAG, "onBindViewHolder: " + entry.getDate());
        Log.e(TAG, "onBindViewHolder: " + entry.getEvents().size());

        // events
        if (entry.getEvents().size() > 0) {
            for (CalendarEvent event : entry.getEvents()) {
                holder.mEventsLinearLayout.addView(ViewUtils.getEventTileView(event, mContext));
            }
        }

        holder.setIsRecyclable(false);
    }

    class CalendarViewHolder extends RecyclerView.ViewHolder {

        private TextView mDayOfMonthTextView;
        private TextView mDayOfWeekTextView;
        private LinearLayout mEventsLinearLayout;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            mDayOfMonthTextView = itemView.findViewById(R.id.tv_day_of_month);
            mDayOfWeekTextView = itemView.findViewById(R.id.tv_day_of_week);
            mEventsLinearLayout= itemView.findViewById(R.id.ll_events);
        }
    }
}
