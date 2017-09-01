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
                    addEventView(event, holder.mEventsLinearLayout);
                    holder.mEventsLinearLayout.setVisibility(View.VISIBLE);
                    holder.setIsRecyclable(false);
                }
            }
        }
    }

    private void addEventView(final CalendarEvent event, ViewGroup parent) {

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
        titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        titleTextView.setSingleLine(true);
        titleTextView.setEllipsize(TextUtils.TruncateAt.END);
        titleTextView.setTextColor(Color.WHITE);
        eventLayout.addView(titleTextView);

        // duration
        TextView descriptionTextView = new TextView(mContext);
        descriptionTextView.setPadding(0, dpAsPx_8 / 2, 0, 0);
        String duration = CalendarUtils.getDate(event.getStartDate(), "hh:mm a") + " - "+ CalendarUtils.getDate(event.getEndDate(), "hh:mm a");
        descriptionTextView.setText(duration);
        descriptionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        descriptionTextView.setTextColor(Color.WHITE);
        descriptionTextView.setSingleLine(true);
        descriptionTextView.setEllipsize(TextUtils.TruncateAt.END);
        eventLayout.addView(descriptionTextView);

        // onClick listener
        eventLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showEventDetails = new Intent(mContext, EventDetailsActivity.class);
                showEventDetails.putExtra(EventDetailsActivity.ARG_CALENDAR_ENTRY, event);
                mContext.startActivity(showEventDetails);
            }
        });

        // add to parent
        eventLayout.setPadding(dpAsPx_8 * 2, dpAsPx_8, dpAsPx_8 * 2, dpAsPx_8);
        parent.addView(eventLayout);
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
