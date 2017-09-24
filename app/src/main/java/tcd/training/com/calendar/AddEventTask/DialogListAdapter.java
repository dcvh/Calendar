package tcd.training.com.calendar.AddEventTask;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.LayoutRes;
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

public class DialogListAdapter extends ArrayAdapter<String> {

    private static final String TAG = DialogListAdapter.class.getSimpleName();

    private Context mContext;
    private ArrayList<String> mStatuses;
    private int mChosenIndex;

    DialogListAdapter(@NonNull Context context, @LayoutRes int resource, ArrayList<String> statuses, int chosenIndex) {
        super(context, resource);
        this.mContext = context;
        this.mStatuses = statuses;
        this.mChosenIndex = chosenIndex;
    }

    @Override
    public int getCount() {
        return mStatuses.size();
    }

    void setSelectedItemIndex(int index) {
        mChosenIndex = index;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_dialog, parent, false);
        }

        TextView content = convertView.findViewById(R.id.tv_primary_content);
        content.setText(mStatuses.get(position));
        content.setTextColor(Color.BLACK);

        ImageView checkIcon = convertView.findViewById(R.id.iv_check_status);
        checkIcon.setVisibility(View.INVISIBLE);

        if (mChosenIndex == position) {
            content.setTextColor(ContextCompat.getColor(mContext, R.color.colorAccent));
            checkIcon.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
