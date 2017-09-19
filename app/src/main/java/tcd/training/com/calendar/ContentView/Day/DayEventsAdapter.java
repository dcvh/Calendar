package tcd.training.com.calendar.ContentView.Day;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.ViewUtils;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayEventsAdapter extends RecyclerView.Adapter<DayEventsAdapter.DayEventViewHolder> {

    private static final String TAG = DayEventsAdapter.class.getSimpleName();

    private final Context mContext;
    private Entry mEntry;

    public DayEventsAdapter(Entry entry, Context context) {
        this.mEntry = entry;
        this.mContext = context;
    }

    @Override
    public DayEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_day_event, parent, false);
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
            for (Event event : mEntry.getEvents()) {
                if (!event.isAllDay() && Integer.valueOf(TimeUtils.getFormattedDate(event.getStartDate(), "H")) == position) {
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
