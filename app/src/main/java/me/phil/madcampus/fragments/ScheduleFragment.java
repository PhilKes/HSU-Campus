package me.phil.madcampus.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import me.phil.madcampus.MainActivity;
import me.phil.madcampus.R;
import me.phil.madcampus.model.Lesson;
import me.phil.madcampus.model.Schedule;
import me.phil.madcampus.model.User;
import me.phil.madcampus.model.adapter.LessonAdapter;
import me.phil.madcampus.shared.DummyApi;
import me.phil.madcampus.shared.Preferences;

/**Schedule with notifications, trips for all lessons **/
public class ScheduleFragment extends Fragment {

    public static final String SIGNATURE ="HSUCampus";
    DummyApi api;
    User user;
    Schedule schedule;
    LessonAdapter lessonAdapter;
    Date now;
    Context context;

    public ScheduleFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public ScheduleFragment(Context context) {
        this.context=context;
    }

    /** Refresh when resumed **/
    @Override
    public void onResume() {
        super.onResume();
        refreshSchedule();
    }
    private void refreshSchedule() {
        now=new Date();
        lessonAdapter.notifyDataSetChanged(now);
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        TextView txtDate=getView().findViewById(R.id.txt_date);
        txtDate.setText(new SimpleDateFormat("MMM dd, yyyy").format(now));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_schedule, container, false);
        TextView txtUser=view.findViewById(R.id.txt_title);
        user=getArguments().getParcelable("user");
        txtUser.setText(user.name+"\n"+user.studies);
        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.schedule));
        api=new DummyApi(context);
        schedule=api.getSchedule(user.studies);
        LinearLayout layoutHours=view.findViewById(R.id.layout_hours);
        LinearLayout layoutDays=view.findViewById(R.id.layout_days);
        layoutHours.removeAllViews();
        /**Load schedule times**/
        for (int i = 0; i < schedule.hours; i++) {
            TextView hourDisplay=new TextView(context);
            hourDisplay.setText(schedule.TIMESTART[i]+"\n-\n"+schedule.TIMEND[i]);
            hourDisplay.setHeight(0);
            hourDisplay.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
            hourDisplay.setGravity(Gravity.CENTER);
            hourDisplay.setBackground(context.getDrawable(R.drawable.grid_hours_background));
            hourDisplay.setTextSize(12f);
            hourDisplay.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            hourDisplay.setTypeface(Typeface.MONOSPACE,Typeface.NORMAL);
            layoutHours.addView(hourDisplay);
        }
        now= new Date();
        /**Fill gridView with lessons**/
        lessonAdapter=new LessonAdapter(context,schedule
                ,layoutDays,layoutHours,now,api);
        GridView gridView = view.findViewById(R.id.view_grid);
        gridView.setAdapter(lessonAdapter);
        /**Show lesson details/actions on click**/
        gridView.setOnItemClickListener((adapterView, clickedView, i, l)->{
             Lesson selectedLesson=schedule.getLesson(i);
            if(selectedLesson==null)
                return;
            Dialog  dialog = new Dialog(getActivity());
            //Fill dialog view
            View dialogLayout = getLayoutInflater().inflate(R.layout.lesson_dialog, null);
            dialog.setContentView(dialogLayout);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            TextView txtCourse=dialogLayout.findViewById(R.id.txt_course);
            txtCourse.setText(selectedLesson.courseLong);
            TextView txtRoom=dialogLayout.findViewById(R.id.txt_room);
            txtRoom.setText(txtRoom.getText()+selectedLesson.room);
            TextView txtProf=dialogLayout.findViewById(R.id.txt_prof);
            txtProf.setText(txtProf.getText()+selectedLesson.prof);
            Button showEvent= dialogLayout.findViewById(R.id.del_event);
            Button addEvent= dialogLayout.findViewById(R.id.add_event);
            showEvent.setOnClickListener(v->{onShowEvent(selectedLesson);dialog.dismiss();});
            addEvent.setOnClickListener(v->{onAddEvent(i);dialog.dismiss();});
            Button btnTrip=dialogLayout.findViewById(R.id.btn_trip);
            btnTrip.setOnClickListener(v->onTripClicked(i));
            dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            dialog.show();
        });

    }

    /** LESSON ACTIONS **/
    /**Show bus route to lesson from Home Location in Google Maps**/
    private void onTripClicked(int i) {
        Date now=new Date();
        char room=schedule.getLesson(i).room.charAt(0);
        /** Choose right location **/
        String location="Hochschule+Ulm";
        switch (room){
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'H':
            case 'F':
            case 'L':
            case 'G':
            case 'K':
            case 'U':
                location+="+Prittwitzstrasse";
                break;
            case 'P':
            case 'O':
            case 'N':
            case 'M':
            case 'Z':
                location+="+Eberhard+Finckh+Strasse";
                break;
            case 'V':
            case 'T':
            case 'S':
            case 'R':
                location+="+Albert+Einstein+Allee";
                break;
        }
        int day=schedule.getDayOfLesson(i);
        int hour=schedule.getHourOfLesson(i);
        String startTime=Schedule.TIMESTART[hour];
        int hourOfDay=Integer.parseInt(startTime.substring(0,2));
        int minute=Integer.parseInt(startTime.substring(3,5));

        Calendar today= new GregorianCalendar();
        today.setTime(now);
        today.set(Calendar.DAY_OF_WEEK,day+2);
        today.set(Calendar.HOUR_OF_DAY,hourOfDay);
        today.set(Calendar.MINUTE,minute);

        SharedPreferences prefs =context.getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
        double latitude=Double.parseDouble(prefs.getString(Preferences.HOME_LATITUDE,"0"));
        double longitude=Double.parseDouble(prefs.getString(Preferences.HOME_LONGITUDE,"0"));

        Log.d("Trip ", "onTripClicked: Arrival Time: "+today.get(Calendar.DAY_OF_WEEK)+"d "+today.get(Calendar.HOUR_OF_DAY)+"h "+today.get(Calendar.MINUTE)+"m");
        Intent intent = new Intent(Intent.ACTION_VIEW,
               /* Uri.parse("https://www.google.com/maps/dir/?api=1&origin="+latitude+","+longitude+"&destination="+location
                        +"&travelmode=transit&dirflg=r&date=01/17/2019&time=11:00&arrival_time="+(today.getTimeInMillis()/1000)+"&key="+getString(R.string.google_maps_key)));
                */Uri.parse("https://www.google.com/maps/?saddr="+latitude+","+longitude+"&daddr="+location+"&date="+(today.get(Calendar.MONTH)+1)+"/"+today.get(Calendar.DAY_OF_MONTH)+"/"+today.get(Calendar.YEAR)+"&time="+today.get(Calendar.HOUR_OF_DAY)+":" +
                                                +today.get(Calendar.MINUTE)+"&ttype=arr&dirflg=r"));
        /*Uri.parse("https://maps.googleapis.com/maps/api/directions/json?origin="+latitude+","+longitude+"&destination="+location
                        +"&mode=transit&arrival_time="+(today.getTimeInMillis()/1000)+"&key="+getString(R.string.google_maps_key)));*/

        /*Log.d("Trip", "https://maps.googleapis.com/maps/api/directions/json?origin="+latitude+","+longitude+"&destination="+location
                +"&mode=transit&arrival_time="+today.getTimeInMillis()+"&key="+getString(R.string.google_maps_key));
                //TODO ARRIVAL<->DEPARTURE???
        */Log.d("Trip ", "https://www.google.com/maps/?saddr="+latitude+","+longitude+"&daddr="+location+"&date="+(today.get(Calendar.MONTH)+1)+"/"+today.get(Calendar.DAY_OF_MONTH)+"/"+today.get(Calendar.YEAR)+"&time="+today.get(Calendar.HOUR_OF_DAY)+":" +
                                                +today.get(Calendar.MINUTE)+"&ttype=arr&dirflg=r");
        //intent.setPackage("com.google.android.apps.maps");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);


    }
    private void onAddEvent(int i) {
        /**Make new event in calendar for lesson**/
        int dayOfWeek= schedule.getDayOfLesson(i);
        Lesson selectedLesson=schedule.getLesson(i);

        Date startDate=new Date(now.getTime());
        String start=schedule.getStartOfLesson(i);
        startDate.setHours(Integer.parseInt(start.substring(0,2)));
        startDate.setMinutes(Integer.parseInt(start.substring(3,5)));
        Calendar startCal = new GregorianCalendar();
        startCal.setTime(startDate);
        startCal.set(Calendar.DAY_OF_WEEK,dayOfWeek+2);

        Date endDate=new Date(now.getTime());
        String end=schedule.getEndOfLesson(i);
        endDate.setHours(Integer.parseInt(end.substring(0,2)));
        endDate.setMinutes(Integer.parseInt(end.substring(3,5)));
        Calendar endCal = new GregorianCalendar();
        endCal.setTime(endDate);
        endCal.set(Calendar.DAY_OF_WEEK,dayOfWeek+2);
        /**Start event next week if lesson already took place this week**/
        if(now.after(startCal.getTime())){
            startCal.add(Calendar.WEEK_OF_MONTH,1);
            endCal.add(Calendar.WEEK_OF_MONTH,1);
        }

        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startCal.getTimeInMillis());
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endCal.getTimeInMillis());
        intent.putExtra(CalendarContract.Events.ALL_DAY, false);
        //intent.putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY");
        intent.putExtra(CalendarContract.Events.TITLE, selectedLesson.courseShort);
        intent.putExtra(CalendarContract.Events.DESCRIPTION
                ,selectedLesson.courseLong+"\n"+selectedLesson.room+"\n"+selectedLesson.prof+"\n\n"+"by "+ SIGNATURE);
        startActivity(intent);
    }
    public void onShowEvent(Lesson selectedLesson){
        MainActivity activity=(MainActivity)getActivity();
        activity.setSelectedLesson(selectedLesson);
        activity.switchFragment(R.id.nav_notifications);
    }
}
