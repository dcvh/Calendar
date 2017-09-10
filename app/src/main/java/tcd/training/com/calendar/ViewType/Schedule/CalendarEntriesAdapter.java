package tcd.training.com.calendar.ViewType.Schedule;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.ViewType.Day.DayViewFragment;
import tcd.training.com.calendar.ViewType.ViewUtils;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = CalendarEntriesAdapter.class.getSimpleName();

    private static final int TYPE_EVENT = 1;
    private static final int TYPE_WEEK = 2;
    private static final int TYPE_TODAY = 3;
    private static final int TYPE_MONTH = 4;

    private ArrayList<CalendarEntry> mEntriesList;
    private Context mContext;

    public CalendarEntriesAdapter(Context context, ArrayList<CalendarEntry> mEntriesList) {
        this.mContext = context;
        this.mEntriesList = mEntriesList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case TYPE_EVENT:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_schedule_entry, parent, false);
                viewHolder = new EntryViewHolder(view);
                break;
            case TYPE_TODAY:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_schedule_today, parent, false);
                viewHolder = new TodayViewHolder(view);
                break;
            case TYPE_WEEK:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_schedule_week, parent, false);
                viewHolder = new WeekViewHolder(view);
                break;
            case TYPE_MONTH:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_schedule_month, parent, false);
                viewHolder = new MonthViewHolder(view);
                break;
            default:
                throw new UnsupportedOperationException("Unknown type");
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        CalendarEntry entry = mEntriesList.get(position);
        if (entry.getEvents() == null) {
            if (entry.getDate().contains("-")){
                return TYPE_WEEK;
            } else {
                return TYPE_MONTH;
            }
        } else {
            if (entry.getEvents().size() > 0) {
                return TYPE_EVENT;
            } else {
                return TYPE_TODAY;
            }
        }
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
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final CalendarEntry entry = mEntriesList.get(position);

        switch (viewHolder.getItemViewType()) {
            case TYPE_EVENT:
                EntryViewHolder entryHolder = (EntryViewHolder) viewHolder;

                createDateTextViews(entryHolder, entry);

                // events
                if (entry.getEvents().size() > 0) {
                    for (CalendarEvent event : entry.getEvents()) {
                        entryHolder.mEventsLinearLayout.addView(ViewUtils.getEventTileView(event, mContext));
                    }
                }

                entryHolder.setIsRecyclable(false);
                break;

            case TYPE_TODAY:
                TodayViewHolder todayHolder = (TodayViewHolder) viewHolder;

                createDateTextViews(todayHolder, entry);

                // events
                if (entry.getEvents().size() == 0) {
                    todayHolder.mNoEventTextView.setText(R.string.no_event);
                    todayHolder.mNoEventTextView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mContext.startActivity(new Intent(mContext, AddEventActivity.class));
                        }
                    });
                    todayHolder.mNoEventTextView.setVisibility(View.VISIBLE);
                } else {
                    for (CalendarEvent event : entry.getEvents()) {
                        todayHolder.mEventsLinearLayout.addView(ViewUtils.getEventTileView(event, mContext));
                    }
                }

                break;

            case TYPE_WEEK:
                WeekViewHolder weekHolder = (WeekViewHolder) viewHolder;
                weekHolder.mWeekTextView.setText(entry.getDate());
                break;

            case TYPE_MONTH:
                MonthViewHolder monthHolder = (MonthViewHolder) viewHolder;
                int resId = ViewUtils.getMonthImageResourceId(entry.getDate().substring(0, entry.getDate().indexOf(" ")));
                Glide.with(mContext).load(resId).into(monthHolder.mMonthImageView);
                monthHolder.mMonthTextView.setText(entry.getDate());
                break;
        }
    }

    private void createDateTextViews(EntryViewHolder holder, final CalendarEntry entry) {
        // day and month
        holder.mDayOfMonthTextView.setText(CalendarUtils.getDate(entry.getDate(), "d"));
        holder.mDayOfWeekTextView.setText(CalendarUtils.getDate(entry.getDate(), "EEE"));

        holder.mDayOfMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DayViewFragment newFragment = DayViewFragment.newInstance(entry.getDate());
                FragmentTransaction transaction = ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction();
                transaction
                        .addToBackStack(null)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .replace(R.id.fl_content, newFragment)
                        .commit();
            }
        });

    }

    class EntryViewHolder extends RecyclerView.ViewHolder {

        protected TextView mDayOfMonthTextView;
        protected TextView mDayOfWeekTextView;
        protected LinearLayout mEventsLinearLayout;

        EntryViewHolder(View itemView) {
            super(itemView);
            mDayOfMonthTextView = itemView.findViewById(R.id.tv_day_of_month);
            mDayOfWeekTextView = itemView.findViewById(R.id.tv_day_of_week);
            mEventsLinearLayout= itemView.findViewById(R.id.ll_events);
        }
    }

    private class TodayViewHolder extends EntryViewHolder {

        TextView mNoEventTextView;

        TodayViewHolder(View itemView) {
            super(itemView);
            mDayOfMonthTextView = itemView.findViewById(R.id.tv_day_of_month);
            mDayOfWeekTextView = itemView.findViewById(R.id.tv_day_of_week);
            mEventsLinearLayout= itemView.findViewById(R.id.ll_events);

            mNoEventTextView = itemView.findViewById(R.id.tv_no_event);
        }
    }

    private class WeekViewHolder extends RecyclerView.ViewHolder {

        private TextView mWeekTextView;

        WeekViewHolder(View itemView) {
            super(itemView);
            mWeekTextView = itemView.findViewById(R.id.tv_week);
        }
    }

    private class MonthViewHolder extends RecyclerView.ViewHolder {

        private ImageView mMonthImageView;
        private TextView mMonthTextView;

        public MonthViewHolder(View itemView) {
            super(itemView);
            mMonthImageView = itemView.findViewById(R.id.iv_month_image);
            mMonthTextView = itemView.findViewById(R.id.tv_month);
        }
    }
}
