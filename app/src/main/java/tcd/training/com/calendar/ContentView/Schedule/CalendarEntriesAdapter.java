package tcd.training.com.calendar.ContentView.Schedule;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.AddEventTask.AddEventActivity;
import tcd.training.com.calendar.Utils.PreferenceUtils;
import tcd.training.com.calendar.Utils.TimeUtils;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.Utils.ViewUtils;

/**
 * Created by cpu10661-local on 8/30/17.
 */

public class CalendarEntriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = CalendarEntriesAdapter.class.getSimpleName();

    private static final int TYPE_EVENT = 1;
    private static final int TYPE_WEEK = 2;
    private static final int TYPE_TODAY = 3;
    private static final int TYPE_MONTH = 4;

    private ArrayList<Entry> mEntries;
    private Context mContext;
    private String mAlternateCalendar;
    private boolean mShowNumberOfWeek;

    public CalendarEntriesAdapter(Context context, ArrayList<Entry> mEntries) {
        this.mContext = context;
        this.mEntries = mEntries;

        mAlternateCalendar = PreferenceUtils.getAlternateCalendar(mContext);
        mShowNumberOfWeek = PreferenceUtils.isShowNumberOfWeekChecked(mContext);
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
        String description = mEntries.get(position).getDescription();
        if (description == null) {
            return TYPE_EVENT;
        }
        switch (description) {
            case "t": return TYPE_TODAY;
            case "w": return TYPE_WEEK;
            case "m": return TYPE_MONTH;
            default:
                return TYPE_EVENT;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mEntries.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {

        final Entry entry = mEntries.get(position);

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

                todayHolder.setIsRecyclable(false);
                break;

            case TYPE_WEEK:
                WeekViewHolder weekHolder = (WeekViewHolder) viewHolder;

                Calendar week = Calendar.getInstance();
                week.setTimeInMillis(entry.getTime());
                String dateString = "";

                // week number
                if (mShowNumberOfWeek) {
                    dateString = String.format(mContext.getString(R.string.week_x) + ", ", week.get(Calendar.WEEK_OF_YEAR));
                }

                dateString += DateUtils.formatDateRange(mContext,
                        week.getTimeInMillis(),
                        week.getTimeInMillis() + TimeUnit.DAYS.toMillis(6),
                        DateUtils.FORMAT_ABBREV_ALL);

                weekHolder.mWeekTextView.setText(dateString);
                break;

            case TYPE_MONTH:
//                MonthViewHolder monthHolder = (MonthViewHolder) viewHolder;
                ParallaxViewHolder monthHolder = (ParallaxViewHolder) viewHolder;
                monthHolder.mMonthImageView.reuse();

//                int resId = ViewUtils.getMonthImageResourceId(entry.getDescription().substring(0, entry.getDescription().indexOf(" ")));
                int resId = ViewUtils.getMonthImageResourceId(entry.getTime());
                monthHolder.mMonthImageView.setImageResource(resId);

                String month = DateUtils.formatDateTime(mContext, entry.getTime(), DateUtils.FORMAT_NO_MONTH_DAY);
                monthHolder.mMonthTextView.setText(month);

                break;
        }
    }

    private void createDateTextViews(EntryViewHolder holder, final Entry entry) {

        // day and month
        holder.mDayOfMonthTextView.setText(String.valueOf(TimeUtils.getField(entry.getTime(), Calendar.DAY_OF_MONTH)));
        holder.mDayOfWeekTextView.setText(
                DateUtils.formatDateTime(mContext, entry.getTime(), DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_ABBREV_WEEKDAY));
        int color = ViewUtils.getDateColor(entry.getTime(), mContext);
        holder.mDayOfMonthTextView.setTextColor(color);
        holder.mDayOfWeekTextView.setTextColor(color);

        // lunar day
        if (mAlternateCalendar != null) {
            holder.mLunarDayTextView.setText(PreferenceUtils.getAlternateDate(entry.getTime(), mAlternateCalendar));
            holder.mLunarDayTextView.setVisibility(View.VISIBLE);
            holder.mLunarDayTextView.setTextColor(color);
        }

        holder.mDayOfMonthTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // prepare for shared element transition

                // send command to the activity
                Intent intent = new Intent(MainActivity.UPDATE_CONTENT_VIEW_ACTION);
                intent.putExtra(MainActivity.ARG_CONTENT_VIEW_TYPE, R.id.nav_day);
                intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, entry.getTime());
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }
        });

    }

    class EntryViewHolder extends RecyclerView.ViewHolder {

        TextView mDayOfMonthTextView;
        TextView mDayOfWeekTextView;
        TextView mLunarDayTextView;
        LinearLayout mEventsLinearLayout;

        EntryViewHolder(View itemView) {
            super(itemView);
            mDayOfMonthTextView = itemView.findViewById(R.id.tv_day_of_month);
            mDayOfWeekTextView = itemView.findViewById(R.id.tv_day_of_week);
            mLunarDayTextView = itemView.findViewById(R.id.tv_alternate_date);
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
