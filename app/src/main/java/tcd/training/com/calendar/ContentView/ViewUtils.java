package tcd.training.com.calendar.ContentView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import tcd.training.com.calendar.Data.DataUtils;
import tcd.training.com.calendar.Data.Event;
import tcd.training.com.calendar.Data.TimeUtils;
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

    public static View getEventTileView(final Event event, final Context context) {

        // prepare a linear layout for wrapping title and duration
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, DP_AS_PX_8);
        LinearLayout eventLayout = new LinearLayout(context);
        eventLayout.setOrientation(LinearLayout.VERTICAL);
        eventLayout.setLayoutParams(layoutParams);

        // background color
        eventLayout.setBackgroundResource(R.drawable.layout_round_corner);
        GradientDrawable drawable = (GradientDrawable) eventLayout.getBackground();
        drawable.setColor(DataUtils.getAccountColor(event.getCalendarId()));

        // title
        TextView titleTextView = getStandardTextView(event.getTitle(), context);
        eventLayout.addView(titleTextView);

        // duration
        if (!event.isAllDay()) {
            String duration = TimeUtils.getFormattedDate(event.getStartDate(), TimeUtils.getStandardTimeFormat())
                    + " - " + TimeUtils.getFormattedDate(event.getEndDate(), TimeUtils.getStandardTimeFormat());
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

    public static String getAddEventDateFormat() {
        return "EEE, MMM d, yyyy";
    }

    public static String getNotificationTimeFormat(int minutes, Context context) {

        StringBuilder formattedTime = new StringBuilder();

        int _minutes = minutes;
        String format = "%d %s ";

        int weeks = (int) (TimeUnit.MINUTES.toDays(_minutes) / 7);
        if (weeks > 0) {
            formattedTime.append(String.format(format, weeks, weeks == 1 ? context.getString(R.string.week) : context.getString(R.string.weeks)));
            _minutes -= TimeUnit.DAYS.toMinutes(weeks) * 7;
        }

        int days = (int) (TimeUnit.MINUTES.toDays(_minutes));
        if (days > 0) {
            formattedTime.append(String.format(format, days, days == 1 ? context.getString(R.string.day) : context.getString(R.string.days)));
            _minutes -= TimeUnit.DAYS.toMinutes(days);
        }

        int hours = (int) (TimeUnit.MINUTES.toHours(_minutes));
        if (hours > 0) {
            formattedTime.append(String.format(format, hours, hours == 1 ? context.getString(R.string.hour) : context.getString(R.string.hours)));
            _minutes -= TimeUnit.HOURS.toMinutes(hours);
        }

        if (_minutes > 0) {
            formattedTime.append(String.format(format, _minutes, _minutes == 1 ? context.getString(R.string.minute) : context.getString(R.string.minutes)));
        }

        return formattedTime.toString();
    }
}
