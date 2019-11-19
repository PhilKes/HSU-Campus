package me.phil.madcampus.model.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.phil.madcampus.R;
import me.phil.madcampus.model.Event;
import me.phil.madcampus.model.Lesson;
import me.phil.madcampus.model.Schedule;
import me.phil.madcampus.shared.DummyApi;

public class LessonAdapter extends BaseAdapter {
    private final Schedule schedule;
    private final Context context;
    private LinearLayout layoutDays,layoutHours;
    private Date now;
    private int dayOfWeek;
    private DummyApi api;
    public LessonAdapter(Context context, Schedule schedule, LinearLayout layout_days,LinearLayout layout_hours,Date now,DummyApi api) {
        this.schedule = schedule;
        this.context = context;
        this.layoutDays=layout_days;
        this.layoutHours=layout_hours;
        this.now=now;
        this.api=api;
    }

    @Override
    public int getCount() {
        return schedule.hours*schedule.days;
    }

    @Override
    public Lesson getItem(int i) {
        return schedule.lessons[i/schedule.days][i%schedule.days];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null) {
            LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=layoutInflater.inflate(R.layout.lesson_layout,null);
        }
       Lesson lesson=getItem(i);
        /** Show free lesson **/
       if(lesson==null) {
           view.setBackgroundColor(context.getColor(R.color.lessonFree));
           return view;
       }
       view.setBackground(context.getDrawable(R.drawable.grid_lesson_background));
       view.setEnabled(true);
        /**Check if lesson is active**/
        DateFormat format = new SimpleDateFormat("HH:mm", Locale.GERMANY);
        int lessonDayofWeek=schedule.getDayOfLesson(i);
        int hourOfLesson=schedule.getHourOfLesson(i);
        if(lessonDayofWeek==dayOfWeek) {
            /**Determine end of last lesson!=null**/
            String lastLessonEnd="00:00";
            if(hourOfLesson>0) {
                lastLessonEnd = schedule.getEndOfLesson(schedule.getLastLesson(i));
                int lessonBefore=i;
                for (int j =1 ; j <=hourOfLesson; j++) {
                    lessonBefore=schedule.getLastLesson(lessonBefore);
                    lastLessonEnd = schedule.getEndOfLesson(lessonBefore);
                    /**Break if last lesson!=null found**/
                    if(schedule.getLesson(lessonBefore)!=null)
                        break;
                    /**Is current Lesson first of the day?**/
                    if(schedule.getHourOfLesson(lessonBefore)==0)
                        lastLessonEnd = "00:00";
                }
            }
            try {
                Date lastEnd = format.parse(lastLessonEnd);
                Date end = format.parse(Schedule.TIMEND[i / schedule.days]);
                int hour = schedule.getHourOfLesson(i);
                now = format.parse(format.format(now));
                /**Mark Lesson as active if now is between end of last lesson and end of current lesson**/
                if (now.before(end) && now.after(lastEnd)) {
                    view.setEnabled(false);
                    layoutHours.getChildAt(hour).setEnabled(false);

                }
            }catch (ParseException pe){
                pe.printStackTrace();
            }
        }
        TextView txtCourse= view.findViewById(R.id.txt_course);
        txtCourse.setText(lesson.courseShort);
        TextView txtRoom= view.findViewById(R.id.txt_room);
        txtRoom.setText(lesson.room);
        if(!lesson.room.contains("\n"))
            txtRoom.setTextSize(16);
        TextView txtProf= view.findViewById(R.id.txt_prof);
        txtProf.setText(lesson.prof);
        /**Check if lesson has active events and show notify icon**/
        ImageView icNotify=view.findViewById(R.id.ic_notify);
        for(Event event : api.listCalendarEvents(lesson.courseShort)){
            DateFormat sdf = new SimpleDateFormat("HH:mm");
            int eventDay = event.start.get(Calendar.DAY_OF_WEEK);
            if(eventDay-2==lessonDayofWeek){
                String eventTime=sdf.format(event.start.getTime());
                if(eventTime.equals(Schedule.TIMESTART[hourOfLesson]))
                    icNotify.setVisibility(View.VISIBLE);
            }
        }
        return view;
    }


    /**no @override due to overloading**/
    public void notifyDataSetChanged(Date now) {
        /**Deselect everything**/
        for (int i = 0; i < layoutDays.getChildCount(); i++)
            layoutDays.getChildAt(i).setEnabled(true);
        for (int i = 0; i < layoutHours.getChildCount(); i++)
            layoutHours.getChildAt(i).setEnabled(true);
        /**Get current Weekday and Time**/
        //now=new Date();
        this.now=now;
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        dayOfWeek = c.get(Calendar.DAY_OF_WEEK)-2;
        /**Monday-Friday**/
        if(dayOfWeek<5 && dayOfWeek>=0)
            layoutDays.getChildAt(dayOfWeek).setEnabled(false);
        /**automatically checks active lesson again with getView(i) calls**/
        super.notifyDataSetChanged();

    }
}
