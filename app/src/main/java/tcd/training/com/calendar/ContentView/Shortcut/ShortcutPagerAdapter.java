package tcd.training.com.calendar.ContentView.Shortcut;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ShortcutPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Calendar> mMonths;
    private ShortcutFragment mCurrentFragment;
    private int mCurrentPosition = -1;

    public ShortcutPagerAdapter(FragmentManager fm, ArrayList<Calendar> months) {
        super(fm);
        mMonths = months;
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            WrapContentViewPager pager = (WrapContentViewPager) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                pager.measureCurrentView(fragment.getView());
            }
        }

        mCurrentFragment = (ShortcutFragment) object;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        return ShortcutFragment.newInstance(mMonths.get(position));
    }

    @Override
    public int getCount() {
        return mMonths.size();
    }

    public ShortcutFragment getCurrentFragment() {
        return mCurrentFragment;
    }
}
