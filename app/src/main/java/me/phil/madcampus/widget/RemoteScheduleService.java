package me.phil.madcampus.widget;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.phil.madcampus.LoginActivity;
import me.phil.madcampus.R;
import me.phil.madcampus.model.Lesson;
import me.phil.madcampus.model.Schedule;
import me.phil.madcampus.model.User;
import me.phil.madcampus.shared.DummyApi;
import me.phil.madcampus.shared.Preferences;

/** Remote Views Provider for GridView of Schedule **/
public class RemoteScheduleService extends RemoteViewsService {
    private Schedule schedule;
    private int currLesson=0;
    private boolean notifiyLesson=false;
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            Date now;
            int dayOfWeek;

            @Override
            public void onCreate() {
                DummyApi api=new DummyApi(getBaseContext());
                SharedPreferences prefs=getBaseContext().getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
                notifiyLesson=prefs.getBoolean("notifyLesson",false);
                //prefs.getBoolean(Preferences.SAVE_CREDENTIALS,false);
                /** Default values for debug purpose **/
                String login=prefs.getString(Preferences.USER_NAME, Preferences.DEFAULT_NAME);
                String pwd=prefs.getString(Preferences.USER_PW, Preferences.DEFAULT_PW);
                User user=api.validateLogin(login,pwd);
                if(user==null)
                    user=api.validateLogin(Preferences.DEFAULT_NAME,Preferences.DEFAULT_PW);
                schedule=api.getSchedule(user.studies);
                now=new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(now);
                dayOfWeek = c.get(Calendar.DAY_OF_WEEK)-2;
            }
            @Override
            public void onDataSetChanged() {
                now=new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(now);
                dayOfWeek = c.get(Calendar.DAY_OF_WEEK)-2;
            }

            @Override
            public void onDestroy() {
                schedule=null;
            }

            @Override
            public int getCount() {
                return schedule.hours*schedule.days;
            }

            @Override
            public RemoteViews getViewAt(int i) {
                Lesson lesson=schedule.getLesson(i);
                RemoteViews rv = new RemoteViews(getBaseContext().getPackageName(),
                        R.layout.lesson_layout);
                /** BUG: Random item displayed when scrolling the widget ?**/
                rv.removeAllViews(R.id.view_grid);
                if(lesson==null) {
                    rv.setInt(R.id.lesson_layout,"setBackgroundColor",
                            getBaseContext().getResources().getColor(R.color.lessonFree));
                    return rv;
                }
                rv.setInt(R.id.lesson_layout,"setBackgroundResource",
                        R.drawable.grid_lesson_background);
                rv.setBoolean(R.id.lesson_layout,"setEnabled",true);
                rv.setTextViewText(R.id.txt_course,lesson.courseShort);
                rv.setTextViewText(R.id.txt_room,lesson.room);
                if(!lesson.room.contains("\n"))
                    rv.setTextViewTextSize(R.id.txt_room, TypedValue.COMPLEX_UNIT_SP,16);
                rv.setTextViewText(R.id.txt_prof,lesson.prof);
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
                            rv.setBoolean(R.id.lesson_layout,"setEnabled",false);
                            currLesson=i;

                            if(!notifiyLesson)
                                return rv;
                            /** NOTIFICATION FOR NEXT LESSON **/
                            Calendar c = Calendar.getInstance();
                            c.setTime(now);
                            int nextLesson = currLesson ;//+ schedule.days;
                            Lesson nxtLesson = schedule.getLesson(nextLesson);
                            if(nxtLesson==null)
                                return rv;
                            long minuteNow = c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE);
                            String start=schedule.getStartOfLesson(nextLesson);
                            long minuteNext = Integer.parseInt(start
                                    .substring(0, 2)) * 60
                                    + Integer.parseInt(start
                                    .substring(3, 5));
                            /** Notify if next lesson starts in under 20 mins.**/
                            /** Gets checked every ~15mins **/
                            if (Math.abs(minuteNext - minuteNow) < 20) {
                                NotificationManager notificationManager;
                                notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                Notification status = null;
                                Intent notificationIntent = new Intent(getBaseContext(), LoginActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(getBaseContext(), 0, notificationIntent, 0);

                                status = new Notification.Builder(getBaseContext())
                                        .setSmallIcon(R.drawable.ic_schedule)
                                        .setContentTitle(nxtLesson.courseShort)
                                        //.setContentText(nxtLesson.courseLong + "\nstarts at: "+start)
                                        .setContentIntent(pendingIntent)
                                        .setStyle(new Notification.BigTextStyle().bigText(nxtLesson.courseLong + "\nstarts at: "+start))
                                        .setAutoCancel(true)
                                        .setVibrate(new long[]{0L})
                                        .setOngoing(false)
                                        .build();
                                status.defaults |= Notification.DEFAULT_SOUND;
                                status.defaults |= Notification.DEFAULT_VIBRATE;
                                status.defaults |= Notification.DEFAULT_LIGHTS;
                                status.flags |= Notification.FLAG_AUTO_CANCEL;
                                notificationManager.notify(1, status);

                                //startForeground(21345, status);
                            }
                            //layoutHours.getChildAt(hour).setEnabled(false);
                        }
                    }catch (ParseException pe){
                        pe.printStackTrace();
                    }
                }
                return rv;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }
            @Override
            public int getViewTypeCount() {
                return 1;
            }
            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }
        };
    }
}
