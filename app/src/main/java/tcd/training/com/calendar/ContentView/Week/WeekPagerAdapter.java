package tcd.training.com.calendar.ContentView.Week;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class WeekPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<Long> mWeeks;

    public WeekPagerAdapter(FragmentManager fm, ArrayList<Long> days) {
        super(fm);
        mWeeks = days;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return WeekFragment.newInstance(mWeeks.get(position));
    }

    @Override
    public int getCount() {
        return mWeeks.size();
    }

}
