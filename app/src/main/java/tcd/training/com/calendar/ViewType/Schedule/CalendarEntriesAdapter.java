package tcd.training.com.calendar.ViewType.Schedule;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
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
import tcd.training.com.calendar.ViewType.ViewUtils;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEntriesAdapter extends RecyclerView.Adapter<CalendarEntriesAdapter.CalendarViewHolder> {

    private static final String TAG = CalendarEntriesAdapter.class.getSimpleName();

    private ArrayList<CalendarEntry> mDatesList;
    private Context mContext;

    public CalendarEntriesAdapter(Context context, ArrayList<CalendarEntry> mDatesList) {
        this.mContext = context;
        this.mDatesList = mDatesList;
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
        return mDatesList.size();
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {

        CalendarEntry calendarDate = mDatesList.get(position);
        long dateInMillis = calendarDate.getEvents().get(0).getStartDate();
        holder.mDayOfMonthTextView.setText(CalendarUtils.getDate(dateInMillis, "d"));
        holder.mDayOfWeekTextView.setText(CalendarUtils.getDate(dateInMillis, "EEE"));

        for (CalendarEvent event : calendarDate.getEvents()) {
            holder.mEventsLinearLayout.addView(ViewUtils.getEventTileView(event, mContext));
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
