package me.phil.madcampus.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import me.phil.madcampus.R;
import me.phil.madcampus.model.Event;
import me.phil.madcampus.model.User;
import me.phil.madcampus.model.adapter.EventAdapter;
import me.phil.madcampus.shared.DummyApi;
import me.phil.madcampus.R;

/** List of all events (Local from device) **/
@SuppressLint("ValidFragment")
public class EventsFragment extends Fragment {
    DummyApi api;
    User user;
    ListView listEvents,todayEvents;
    private String selectedLesson;
    private Context context;
    @SuppressLint("ValidFragment")
    public EventsFragment(Context context,DummyApi api) {
        this.api=api;
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_events, container, false);
        todayEvents=view.findViewById(R.id.list_all);
        listEvents=view.findViewById(R.id.list_events);
        //user=getArguments().getParcelable("user");
        if(getArguments()!=null)
            selectedLesson=getArguments().getString("lesson");
        return view;
    }
    @SuppressLint("NewApi")
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.notifications));
        Calendar today=Calendar.getInstance();
        today.setTime(new Date());
        ArrayList<Event> allEvents=api.listAllEvents();
        if(allEvents.isEmpty()) {
            view.findViewById(R.id.txt_message).setVisibility(View.VISIBLE);
            return;
        }
        /** Show only events from selected Lesson (click on schedule) **/
        if(selectedLesson!=null)
            allEvents= new ArrayList<>(allEvents.stream().filter(ev-> ev.title.equals(selectedLesson)).collect(Collectors.toList()));
        /** today's notifications on the top **/
        ArrayList<Event> eventsToday = new ArrayList<>(allEvents.stream().filter(ev ->{
                if(ev.start.get(Calendar.DAY_OF_WEEK)==today.get(Calendar.DAY_OF_WEEK)){
                    ev.today=true;
                    return true;
                }
                return false;
        }).collect(Collectors.toList()));
        /** all other notifications **/
        ArrayList<Event> eventsOther = new ArrayList<>(allEvents.stream().filter(ev ->ev.start.get(Calendar.DAY_OF_WEEK)!=today.get(Calendar.DAY_OF_WEEK)).collect(Collectors.toList()));

        TextView txtMessage=view.findViewById(R.id.txt_message);
        txtMessage.setVisibility(View.VISIBLE);
        if(!eventsToday.isEmpty()){
            txtMessage.setText(R.string.today);
            todayEvents.setAdapter(new EventAdapter(context,api,eventsToday));
            todayEvents.setVisibility(View.VISIBLE);
        }
        else
            txtMessage.setText(R.string.noevents);
        if(!eventsOther.isEmpty()){
            TextView txtOther=view.findViewById(R.id.txt_other);
            //txtMessage.setText(Preferences.PREF_OTHER);
            txtOther.setVisibility(View.VISIBLE);
            listEvents.setAdapter(new EventAdapter(context,api,eventsOther));
            listEvents.setVisibility(View.VISIBLE);
        }
    }
}
