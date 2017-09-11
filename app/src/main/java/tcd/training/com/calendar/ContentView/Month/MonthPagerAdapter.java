package tcd.training.com.calendar.ContentView.Month;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class MonthPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Calendar> mMonths;

    public MonthPagerAdapter(FragmentManager fm, ArrayList<Calendar> months) {
        super(fm);
        mMonths = months;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return MonthFragment.newInstance(mMonths.get(position));
    }

    @Override
    public int getCount() {
        return mMonths.size();
    }
}
