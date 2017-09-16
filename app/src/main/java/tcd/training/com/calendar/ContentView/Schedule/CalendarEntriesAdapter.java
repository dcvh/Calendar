package tcd.training.com.calendar.ContentView.Schedule;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Data.TimeUtils;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Data.Entry;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.ContentView.Day.DayViewFragment;
import tcd.training.com.calendar.ViewUtils;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = CalendarEntriesAdapter.class.getSimpleName();

    private static final int TYPE_EVENT = 1;
    private static final int TYPE_WEEK = 2;
    private static final int TYPE_TODAY = 3;
    private static final int TYPE_MONTH = 4;

    private ArrayList<Entry> mEntriesList;
    private Context mContext;

    public CalendarEntriesAdapter(Context context, ArrayList<Entry> mEntriesList) {
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
//                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_schedule_month, parent, false);
//                viewHolder = new MonthViewHolder(view);
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_schedule_month_parallex, parent, false);
                viewHolder = new ParallaxViewHolder(view);
                break;
            default:
                throw new UnsupportedOperationException("Unknown type");
        }
        return viewHolder;
    }

    @Override
    public int getItemViewType(int position) {
        Entry entry = mEntriesList.get(position);
        if (entry.getEvents() == null) {
            if (entry.getDescription().contains("-")){
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

        final Entry entry = mEntriesList.get(position);

        switch (viewHolder.getItemViewType()) {
            case TYPE_EVENT:
                EntryViewHolder entryHolder = (EntryViewHolder) viewHolder;

                createDateTextViews(entryHolder, entry);

                // events
                if (entry.getEvents().size() > 0) {
                    for (Event event : entry.getEvents()) {
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
                    for (Event event : entry.getEvents()) {
                        todayHolder.mEventsLinearLayout.addView(ViewUtils.getEventTileView(event, mContext));
                    }
                }

                break;

            case TYPE_WEEK:
                WeekViewHolder weekHolder = (WeekViewHolder) viewHolder;
                weekHolder.mWeekTextView.setText(entry.getDescription());
                break;

            case TYPE_MONTH:
//                MonthViewHolder monthHolder = (MonthViewHolder) viewHolder;
                ParallaxViewHolder monthHolder = (ParallaxViewHolder) viewHolder;
                monthHolder.mMonthImageView.reuse();

                int resId = ViewUtils.getMonthImageResourceId(entry.getDescription().substring(0, entry.getDescription().indexOf(" ")));
                monthHolder.mMonthImageView.setImageResource(resId);

                monthHolder.mMonthTextView.setText(entry.getDescription());

                break;
        }
    }

    private void createDateTextViews(EntryViewHolder holder, final Entry entry) {
        // day and month
        holder.mDayOfMonthTextView.setText(TimeUtils.getFormattedDate(entry.getTime(), "d"));
        holder.mDayOfWeekTextView.setText(TimeUtils.getFormattedDate(entry.getTime(), "EEE"));
        if (TimeUtils.compareDay(entry.getTime(), Calendar.getInstance().getTimeInMillis()) > 0) {
            int blackColor = Color.rgb(40, 40, 40);
            holder.mDayOfMonthTextView.setTextColor(blackColor);
            holder.mDayOfWeekTextView.setTextColor(blackColor);
        }

        holder.mDayOfMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DayViewFragment newFragment = DayViewFragment.newInstance(entry.getTime());
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

    class ParallaxViewHolder extends RecyclerView.ViewHolder implements ParallaxImageView.ParallaxImageListener {

        private ParallaxImageView mMonthImageView;
        private TextView mMonthTextView;

        ParallaxViewHolder(View itemView) {
            super(itemView);

            mMonthImageView = itemView.findViewById(R.id.iv_month_image);
            mMonthImageView.setListener(this);

            mMonthTextView = itemView.findViewById(R.id.tv_month);
        }

        @Override
        public int[] requireValuesForTranslate() {
            if (itemView.getParent() == null) {
                // Not added to parent yet!
                return null;
            } else {
                int[] itemPosition = new int[2];
                itemView.getLocationOnScreen(itemPosition);

                int[] recyclerPosition = new int[2];
                ((RecyclerView) itemView.getParent()).getLocationOnScreen(recyclerPosition);

                return new int[]{itemPosition[1], ((RecyclerView) itemView.getParent()).getMeasuredHeight(), recyclerPosition[1]};
            }
        }

        void animateImage() {
            mMonthImageView.doTranslate();
        }
    }
}
