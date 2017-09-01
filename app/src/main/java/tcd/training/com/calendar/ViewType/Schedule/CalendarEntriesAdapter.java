package tcd.training.com.calendar.ViewType.Schedule;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;

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
            addEventView(event.getTitle(), event.getDescription(), holder.mEventsLinearLayout);
        }

        holder.setIsRecyclable(false);
    }
    
    private void addEventView(String title, String description, ViewGroup parent) {

        // dp to pixels
        final float scale = mContext.getResources().getDisplayMetrics().density;
        int dpAsPx_8 = (int) (8 * scale + 0.5f);

        // title and description text views
        TextView titleTextView = new TextView(mContext);
        titleTextView.setPadding(0, dpAsPx_8, 0, dpAsPx_8 / 2);
        titleTextView.setText(title);
        TextView descriptionTextView = new TextView(mContext);
        descriptionTextView.setPadding(0, 0, 0, dpAsPx_8);
        descriptionTextView.setText(description);

        // prepare a linear layout for wrapping those two
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, dpAsPx_8);
        LinearLayout eventLayout = new LinearLayout(mContext);
        eventLayout.setOrientation(LinearLayout.VERTICAL);
        eventLayout.setLayoutParams(layoutParams);
        eventLayout.setBackgroundResource(R.drawable.layout_round_corner);

        // add to parent
        eventLayout.addView(titleTextView);
        eventLayout.addView(descriptionTextView);
        eventLayout.setPadding(dpAsPx_8 * 2, 0, dpAsPx_8 * 2, 0);

        parent.addView(eventLayout);
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
