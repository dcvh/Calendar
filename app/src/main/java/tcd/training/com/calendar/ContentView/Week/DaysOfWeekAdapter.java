package tcd.training.com.calendar.ContentView.Week;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import tcd.training.com.calendar.Entities.Entry;
import tcd.training.com.calendar.Entities.Event;
import tcd.training.com.calendar.MainActivity;
import tcd.training.com.calendar.R;
import tcd.training.com.calendar.Utils.ViewUtils;

/**
 * Created by ADMIN on 11/09/2017.
 */

public class DaysOfWeekAdapter extends BaseAdapter {

    private static final String TAG = DaysOfWeekAdapter.class.getSimpleName();

    private static int WEEK_NUMBER_TEXT_SIZE;

    private ArrayList<Entry> mEntries;
    private Context mContext;

    public DaysOfWeekAdapter(ArrayList<Entry> entries, Context context) {
        this.mEntries = entries;
        this.mContext = context;

        WEEK_NUMBER_TEXT_SIZE = ViewUtils.pixelToSp(context.getResources().getDimension(R.dimen.week_number_text_size));
    }

    @Override
    public int getCount() {
        return 192;             // 24 * 8
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewUtils.dpToPixel(40));

        if (position % 8 == 0) {

            int hour = position / 8;
            String hourString = hour <= 12 ? String.valueOf(hour) + " AM" : String.valueOf(hour % 12) + " PM";
            TextView timeTextView = ViewUtils.getTextView(hourString, WEEK_NUMBER_TEXT_SIZE, Color.GRAY, Typeface.NORMAL, true, mContext);
            timeTextView.setGravity(Gravity.CENTER_VERTICAL);
            timeTextView.setLayoutParams(params);

            view = timeTextView;

        } else {

            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setLayoutParams(params);

            LinearLayout.LayoutParams eventParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);

            final Entry entry = mEntries.get(position % 8 - 1);
            Calendar cal = Calendar.getInstance();
            if (entry.getEvents() != null) {
                for (Event event : entry.getEvents()) {
                    if (!event.isAllDay()) {
                        cal.setTimeInMillis(event.getStartDate());
                        if ((int) cal.get(Calendar.HOUR_OF_DAY) == (position / 8)) {

                            TextView eventTextView = ViewUtils.getSimpleTileView(event, mContext);
                            eventTextView.setLayoutParams(eventParams);

                            layout.addView(eventTextView);
                        }
                    }
                }
            }

            layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(MainActivity.UPDATE_CONTENT_VIEW_ACTION);
                    intent.putExtra(MainActivity.ARG_TIME_IN_MILLIS, entry.getTime());
                    intent.putExtra(MainActivity.ARG_CONTENT_VIEW_TYPE, R.id.nav_day);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                }
            });
            view = layout;
        }

        view.setBackgroundColor(Color.WHITE);
        return view;
    }
}
