package tcd.training.com.calendar.ViewType;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
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

    public static int dpToPixel(int dp) {
        return (int) (dp * SCALE + 0.5f);
    }

    public static View getEventTileView(final CalendarEvent event, final Context context) {

        // prepare a linear layout for wrapping title and duration
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, DP_AS_PX_8);
        LinearLayout eventLayout = new LinearLayout(context);
        eventLayout.setOrientation(LinearLayout.VERTICAL);
        eventLayout.setLayoutParams(layoutParams);

        // background color
        eventLayout.setBackgroundResource(R.drawable.layout_round_corner);
        GradientDrawable drawable = (GradientDrawable) eventLayout.getBackground();
        drawable.setColor(CalendarUtils.getAccountColor(event.getCalendarId()));

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

    public static TextView getStandardTextView(String content, Context context) {
        TextView textView = new TextView(context);

        textView.setText(content);
        textView.setTextColor(Color.WHITE);
        textView.setTypeface(null, Typeface.BOLD);

        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);

        return textView;
    }

    public static int getMonthImageResourceId(int month) {
        switch (month) {
            case 0: return R.drawable.bkg_01_january ;
            case 1: return R.drawable.bkg_02_february ;
            case 2: return R.drawable.bkg_03_march ;
            case 3: return R.drawable.bkg_04_april ;
            case 4: return R.drawable.bkg_05_may ;
            case 5: return R.drawable.bkg_06_june ;
            case 6: return R.drawable.bkg_07_july ;
            case 7: return R.drawable.bkg_08_august ;
            case 8: return R.drawable.bkg_09_september ;
            case 9: return R.drawable.bkg_10_october ;
            case 10: return R.drawable.bkg_11_november ;
            case 11: return R.drawable.bkg_12_december ;
            default:
                throw new UnsupportedOperationException("Unknown month");
        }
    }

    public static int getMonthImageResourceId(String month) {
        switch (month) {
            case "January": return R.drawable.bkg_01_january ;
            case "February": return R.drawable.bkg_02_february ;
            case "March": return R.drawable.bkg_03_march ;
            case "April": return R.drawable.bkg_04_april ;
            case "May": return R.drawable.bkg_05_may ;
            case "June": return R.drawable.bkg_06_june ;
            case "July": return R.drawable.bkg_07_july ;
            case "August": return R.drawable.bkg_08_august ;
            case "September": return R.drawable.bkg_09_september ;
            case "October": return R.drawable.bkg_10_october ;
            case "November": return R.drawable.bkg_11_november ;
            case "December": return R.drawable.bkg_12_december ;
            default:
                throw new UnsupportedOperationException("Unknown month");
        }
    }
}
