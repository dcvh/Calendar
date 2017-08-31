package tcd.training.com.calendar.ViewType.Schedule;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import tcd.training.com.calendar.Calendar.CalendarEntry;
import tcd.training.com.calendar.R;

import static tcd.training.com.calendar.MainActivity.ARG_ENTRIES_LIST;

/**
 * Created by cpu10661-local on 8/31/17.
 */

public class ScheduleViewFragment extends Fragment {

    private ArrayList<CalendarEntry> mEntriesList;

    private RecyclerView.LayoutManager mLayoutManager;
    private CalendarEntriesAdapter mAdapter;

    public ScheduleViewFragment() {
    }

    public static ScheduleViewFragment newInstance(ArrayList<CalendarEntry> entries) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ENTRIES_LIST, entries);
        ScheduleViewFragment fragment = new ScheduleViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEntriesList = getArguments() != null ?
                (ArrayList<CalendarEntry>) getArguments().getSerializable(ARG_ENTRIES_LIST) :
                new ArrayList<CalendarEntry>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_schedule_view, container, false);

        // recycler view
        RecyclerView eventsRecyclerView = (RecyclerView) view.findViewById(R.id.rv_events_list);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        eventsRecyclerView.setLayoutManager(mLayoutManager);
        eventsRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mAdapter = new CalendarEntriesAdapter(view.getContext(), mEntriesList);
        eventsRecyclerView.setAdapter(mAdapter);

        return view;
    }

    public void scrollToToday() {
//        String today = CalendarUtils.getDate(Calendar.getInstance().getTimeInMillis(), "yyyy/MM/dd");
//        // TODO: 8/31/17 this is temporary, must be fixed in the future for better performance (consider switching to binary search)
//        for (int i = 0; i < mCalendarEntriesList.size(); i++) {
//            String date = mCalendarEntriesList.get(i).getDate();
//            if (date.equals(today)) {
//                mLayoutManager.scrollToPosition(i);
//            } else if (date.compareTo(today) < 0) {
//                break;
//            }
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
