package tcd.training.com.calendar.ContentView.Day;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cpu10661-local on 9/1/17.
 */

public class DayPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Calendar> mDays;

    public DayPagerAdapter(FragmentManager fm, ArrayList<Calendar> days) {
        super(fm);
        mDays = days;
    }

    @Override
    public Fragment getItem(int position) {
        return DayFragment.newInstance(mDays.get(position));
    }

    @Override
    public int getCount() {
        return mDays.size();
    }

}
