package tcd.training.com.calendar.AddEventTask;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import tcd.training.com.calendar.R;

/**
 * Created by cpu10661-local on 9/8/17.
 */

public class ColorAdapter extends ArrayAdapter<String> {

    private static final String TAG = ColorAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<String> mColorNames;
    private ArrayList<Integer> mColorValues;
    private int mChosenIndex;

    private int mDpToPx_5;

    public ColorAdapter(@NonNull Context context, ArrayList<String> colorNames, ArrayList<Integer> colorValues, int chosenIndex) {
        super(context, R.layout.list_item_color_dialog);
        this.mContext = context;
        this.mColorNames = colorNames;
        this.mColorValues = colorValues;
        this.mChosenIndex = chosenIndex;

        float scale = Resources.getSystem().getDisplayMetrics().density;
        mDpToPx_5 = (int) (5 * scale + 0.5f);
    }

    @Override
    public int getCount() {
        return mColorNames.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_item_color_dialog, parent, false);

        TextView content = convertView.findViewById(R.id.tv_primary_content);

        content.setText(mColorNames.get(position));

        ImageView circleColor = convertView.findViewById(R.id.iv_circle_color);

        if (mChosenIndex == position) {
            content.setTextColor(ContextCompat.getColor(mContext, R.color.light_blue));
            convertView.findViewById(R.id.iv_check_status).setVisibility(View.VISIBLE);

            circleColor.setBackgroundResource(R.drawable.layout_circle);
            GradientDrawable drawable = (GradientDrawable) circleColor.getBackground();
            drawable.setColor(mColorValues.get(position));

        } else {
            GradientDrawable drawable = (GradientDrawable) circleColor.getBackground();
            drawable.setStroke(mDpToPx_5, mColorValues.get(position));
        }

        return convertView;
    }
}
