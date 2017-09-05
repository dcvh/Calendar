package tcd.training.com.calendar.ViewType;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import tcd.training.com.calendar.Calendar.CalendarEvent;
import tcd.training.com.calendar.Calendar.CalendarUtils;
import tcd.training.com.calendar.EventDetailsActivity;
import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 9/5/17.
 */

public class ViewUtils {

    private static final float SCALE = Resources.getSystem().getDisplayMetrics().density;
    private static final int DP_AS_PX_8 = (int) (8 * SCALE + 0.5f);

    public static View getEventTileView(final CalendarEvent event, final Context context) {

        // prepare a linear layout for wrapping title and duration
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, DP_AS_PX_8);
        LinearLayout eventLayout = new LinearLayout(context);
        eventLayout.setOrientation(LinearLayout.VERTICAL);
        eventLayout.setLayoutParams(layoutParams);
        eventLayout.setBackgroundResource(R.drawable.layout_round_corner);
        eventLayout.setBackgroundColor(CalendarUtils.getAccountColor(event.getCalendarId()));

        // title
        TextView titleTextView = getStandardTextView(event.getTitle(), context);
        eventLayout.addView(titleTextView);

        // duration
        if (!event.isAllDay()) {
            String duration = CalendarUtils.getDate(event.getStartDate(), "hh:mm a") + " - " + CalendarUtils.getDate(event.getEndDate(), "hh:mm a");
            TextView descriptionTextView = getStandardTextView(duration, context);
            descriptionTextView.setPadding(0, DP_AS_PX_8 / 2, 0, 0);
            eventLayout.addView(descriptionTextView);
        }

        // onClick listener
        eventLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent showEventDetails = new Intent(context, EventDetailsActivity.class);
                showEventDetails.putExtra(EventDetailsActivity.ARG_CALENDAR_ENTRY, event);
                context.startActivity(showEventDetails);
            }
        });

        eventLayout.setPadding(DP_AS_PX_8 * 2, DP_AS_PX_8, DP_AS_PX_8 * 2, DP_AS_PX_8);
        return eventLayout;
    }

    private static TextView getStandardTextView(String content, Context context) {
        TextView textView = new TextView(context);

        textView.setText(content);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null, Typeface.BOLD);

        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        return textView;
    }
}
