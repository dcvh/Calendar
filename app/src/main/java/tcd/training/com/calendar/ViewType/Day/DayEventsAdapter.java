package tcd.training.com.calendar.ViewType.Day;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.EventDetailsActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.ViewType.Schedule.CalendarEntriesAdapter;
import tcd.training.com.calendar.ViewType.ViewUtils;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayEventsAdapter extends RecyclerView.Adapter<DayEventsAdapter.DayEventViewHolder> {

    private final Context mContext;
    private CalendarEntry mEntry;

    public DayEventsAdapter(CalendarEntry entry, Context context) {
        this.mEntry = entry;
        this.mContext = context;
    }

    @Override
    public DayEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_event_list_item, parent, false);
        return new DayEventViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 24;      // 24 hours a day
    }

    @Override
    public void onBindViewHolder(DayEventViewHolder holder, int position) {
        String time = String.valueOf(position % 12 + 1) + (position < 12 ? " AM" : " PM");
        holder.mTimeTextView.setText(time);
        if (mEntry != null) {
            for (CalendarEvent event : mEntry.getEvents()) {
                if (!event.isAllDay() && Integer.valueOf(CalendarUtils.getDate(event.getStartDate(), "HH")) == position) {
                    holder.mEventsLinearLayout.addView(ViewUtils.getEventTileView(event, mContext));
                    holder.mEventsLinearLayout.setVisibility(View.VISIBLE);
                    holder.setIsRecyclable(false);
                }
            }
        }
    }

    class DayEventViewHolder extends RecyclerView.ViewHolder {

        TextView mTimeTextView;
        LinearLayout mEventsLinearLayout;

        public DayEventViewHolder(View itemView) {
            super(itemView);
            mTimeTextView = itemView.findViewById(R.id.tv_time);
            mEventsLinearLayout = itemView.findViewById(R.id.ll_events_in_hour);
        }
    }
}
