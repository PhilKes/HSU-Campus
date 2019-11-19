package me.phil.madcampus.shared;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.phil.madcampus.MainActivity;
import me.phil.madcampus.model.Event;
import me.phil.madcampus.model.Exercise;
import me.phil.madcampus.model.Lesson;
import me.phil.madcampus.model.Schedule;
import me.phil.madcampus.model.User;
import me.phil.madcampus.model.adapter.LessonAdapter;

import static me.phil.madcampus.fragments.ScheduleFragment.SIGNATURE;
import static me.phil.madcampus.shared.DummyDB.DAY_COL_SCHEDULE;
import static me.phil.madcampus.shared.DummyDB.DAY_TABLE;
import static me.phil.madcampus.shared.DummyDB.EXERCISE_COL_DUE;
import static me.phil.madcampus.shared.DummyDB.EXERCISE_COL_LESSON;
import static me.phil.madcampus.shared.DummyDB.EXERCISE_COL_NR;
import static me.phil.madcampus.shared.DummyDB.EXERCISE_TABLE;
import static me.phil.madcampus.shared.DummyDB.LESSON_COL_DAY;
import static me.phil.madcampus.shared.DummyDB.LESSON_COL_NAME;
import static me.phil.madcampus.shared.DummyDB.LESSON_COL_NAME_LONG;
import static me.phil.madcampus.shared.DummyDB.LESSON_COL_START;
import static me.phil.madcampus.shared.DummyDB.LESSON_TABLE;
import static me.phil.madcampus.shared.DummyDB.SCHEDULE_DAY;
import static me.phil.madcampus.shared.DummyDB.SCHEDULE_TABLE;
import static me.phil.madcampus.shared.DummyDB.USER_COL_SCHEDULE;

/**DUMMY API FOR LOGIN USERS, SCHEDULES,...
    SIMULATES MOODLE/LSF API **/

public class DummyApi {

    public static final String TAG="DummyAPI";
    public DummyDB database;
    private Context context;
    private static final String[] DAY_NAMES={"Mo","Tue","Wed","Thu","Fri"};

    /** EXAMPLE SCHEDULES FOR DATABASE **/
    //[HOUR][DAY]
    public static final Lesson[][] SCHEDULE_INF3={
            {new Lesson("PROG","Programmieren 3","C08","Bayerl"),new Lesson("FENGL","Fachenglisch *hust* bullshit *hust*","A304","McLaughlin"),null,new Lesson("ANLY","Analysis 2","A109","Lunde"),null},
            {new Lesson("PROG","Programmieren 3","C08","Bayerl"),new Lesson("FENGL","Fachenglisch *hust* bullshit *hust*","A304","McLaughlin"),null,new Lesson("ALGO","Algorithmen und Datenstrukturen","A109","Schied"),new Lesson("EMSYS","Embedded Systems","C22","Strahnen")},
            {new Lesson("ALGO","Algorithmen und Datenstrukturen","C08","Schied"),null,new Lesson("ANLY","Analysis 2","A304","Lunde"),new Lesson("ALGO","Algorithmen und Datenstrukturen","C08","Tut"),new Lesson("EMSYS","Embedded Systems","C22","Strahnen")},
            {new Lesson("GMARK","Grundlagen des Marketing","M108","Schallmo"),new Lesson("SPAN","Spanisch Grundstufe I","S07","Chico"),null,new Lesson("MOAD","Mobile Application Development","C09","Graf"),null},
            {new Lesson("GMARK","Grundlagen des Marketing","M108","Schallmo"),new Lesson("SPAN","Spanisch Grundstufe I","S07","Chico"),null,new Lesson("MOAD","Mobile Application Development","C09","Graf"),null}
    };
    //[HOUR][DAY]
    public static final Lesson[][] SCHEDULE_INF2={
            {new Lesson("PROG","Programmieren 2","C08","Lunde"),new Lesson("ANA1","Analysis 1","A104","Lunde"),null,null,null},
            {new Lesson("PROG","Programmieren 2","C08","Lunde"),null,null,new Lesson("EINF","Einführung in die Informatik","A109","Baer"),new Lesson("EPRO","Einführendes Projekt","C22","Strahnen")},
            {new Lesson("TINF","Theorethische Informatik","C08","Frey"),null,null,new Lesson("EINF","Einfphrung in die Informatik","A109","Baer"),new Lesson("EPRO","Einführendes Projekt","C22","Strahnen")},
            {null,new Lesson("RNET","Rechnernetze","A104","Steiper"),new Lesson("ANA1","Analysis 1","A109","Lunde"),null,null},
            {null,new Lesson("RNET","Rechnernetze","A104","Steiper"),null,null,null}
    };
    //[HOUR][DAY]
    public static final Lesson[][] SCHEDULE_INF4={
            {new Lesson("SOPR","Software Projekt","A204","Graf")            ,null,new Lesson("SEM","Seminar","A207","Lunde"),new Lesson("VSYS","Verteilte u. Webbasierte Systeme","Q015","Traub"),null},
            {new Lesson("SOPR","Software Projekt","C07","Graf")             ,null,new Lesson("SEM","Seminar","A207","Lunde"),new Lesson("VSYS","Verteilte u. Webbasierte Systeme","Q015","Traub"),new Lesson("ASN","Ad hoc Sensor Networks","A206","Steiper")},
            {null                                                                                         ,new Lesson("DIGT","Digital Systems ","A206\nC04","Frey")           ,null,null,null},
            {new Lesson("AUTMS","Autonomous Systems","B108\nC12","Schlegel"),new Lesson("ASN","Ad hoc Sensor Networks","A108\nC12","Steiper")   ,null,null,null},
            {new Lesson("AUTMS","Autonomous Systems","B108\nC12","Schlegel"),new Lesson("ASN","Ad hoc Sensor Networks","C12","Steiper")   ,null,null,null}
    };

    public DummyApi(Context context) {
        this.context=context;
        database=new DummyDB(context);
    }

    public boolean resetAll(){
        SQLiteDatabase db=database.getWritableDatabase();
        database.onUpgrade(db,0,0);
        return true;
    }

    /** USER API **/
    public long createUser(String name,String passwd,String studies){
        /**Do not create user if name already exists**/
        if(queryUser(name,"%","%").size()>0) {
            Log.d(TAG, "createUser: COULD NOT CREATE USER\tName: "+name+" already exists");
            return -1;
        }
        long scheduleID=queryScheduleID(studies);
        SQLiteDatabase db=database.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DummyDB.USER_COL_NAME,name);
        values.put(DummyDB.USER_COL_PASSWD,passwd);
        values.put(DummyDB.USER_COL_STUDIES,studies);
        values.put(USER_COL_SCHEDULE,scheduleID);
        long newRowId=db.insert(DummyDB.USER_TABLE,null,values);
        db.close();
        Log.d(TAG, "createUser: CREATED USER\tName: "+name+" Passwd: "+passwd);
        return newRowId;
    }
    public ArrayList<User> getAllUsers(){
        return queryUser("%","%","%");
    }
    public ArrayList<User> getAllFromStudies(String studies){
        return queryUser("%","%",studies);
    }
    public ArrayList<User> queryUser(String name, String passwd,String studies){
        SQLiteDatabase db=database.getReadableDatabase();
        String[] projection={
                BaseColumns._ID,
                DummyDB.USER_COL_NAME,
                DummyDB.USER_COL_PASSWD,
                DummyDB.USER_COL_STUDIES,
                DummyDB.USER_COL_AVATAR
        };
        String selection= DummyDB.USER_COL_NAME+" LIKE ? AND "+DummyDB.USER_COL_PASSWD
                +" LIKE ? AND "+DummyDB.USER_COL_STUDIES+" LIKE ?";
        String[] args={name,passwd,studies};
        Cursor c= db.query(DummyDB.USER_TABLE,projection,selection,args,null,null,null);
        ArrayList<User> users=new ArrayList<>();
        while(c.moveToNext()){
            users.add(new User(c.getString(c.getColumnIndexOrThrow(DummyDB.USER_COL_NAME))
                    ,c.getString(c.getColumnIndexOrThrow(DummyDB.USER_COL_PASSWD)),
                    c.getString(c.getColumnIndexOrThrow(DummyDB.USER_COL_STUDIES)),
                    c.getString(c.getColumnIndexOrThrow(DummyDB.USER_COL_AVATAR))));
        }
        c.close();
        db.close();
        return users;
    }
    public User validateLogin(String name, String passwd){
        ArrayList<User> result=queryUser(name,passwd,"%");
        if(result.size()==0)
            return null;
        if(result.size()>1)
            Log.d("SQLite", "validateLogin: Database ERROR: Multiple users for credentials found!");
        return result.get(0);
    }
    public boolean changePassword(String name, String passwd){
        SQLiteDatabase db=database.getWritableDatabase();
        ContentValues args = new ContentValues();
        //args.put(DummyDB.USER_COL_NAME, name);
        args.put(DummyDB.USER_COL_PASSWD, passwd);
        int rows=db.update(DummyDB.USER_TABLE,args,DummyDB.USER_COL_NAME+" = ?",new String[]{name});
        db.close();
        return rows==1;
    }
    public boolean changeAvatar(String user,String imagePath){
        SQLiteDatabase db=database.getWritableDatabase();
        ContentValues args = new ContentValues();
        args.put(DummyDB.USER_COL_AVATAR, imagePath);
        int rows=db.update(DummyDB.USER_TABLE,args,DummyDB.USER_COL_NAME+" = ?",new String[]{user});
        db.close();
        return rows==1;
    }

    /** Insert dummy Assignments for lessons showcase (Moodle mockup) **/
    public long createExercise(String name, long lessonId, Date dueDate, int number){
        SQLiteDatabase db=database.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DummyDB.EXERCISE_COL_NAME,name);
        values.put(DummyDB.EXERCISE_COL_LESSON,lessonId);
        values.put(DummyDB.EXERCISE_COL_DUE,getDateTime(dueDate));
        values.put(EXERCISE_COL_NR,number);
        long newRowId=db.insert(DummyDB.EXERCISE_TABLE,null,values);
        db.close();
        Log.d(TAG, "createExercise: CREATED Exercise\tName: "+name+" Lesson: "+lessonId);
        return newRowId;
    }
    public void createExercisesINF3(){
        Schedule scheduleINF3=getSchedule("INF3");
        Calendar tomorrow=Calendar.getInstance();
        tomorrow.setTime(new Date());
        Calendar date=(Calendar)tomorrow.clone();
        date.add(Calendar.DATE,1);

        createExercise("3.Integrale und Stuff",scheduleINF3.lessons[2][2]._id,dateFromString(getDateTime(date.getTime())),3);
        date=(Calendar)date.clone();
        date.add(Calendar.DATE,-14);
        createExercise("2.Konvergenzreihen",scheduleINF3.lessons[2][2]._id,dateFromString(getDateTime(date.getTime())),2);
        //date=(Calendar)date.clone();
        date.add(Calendar.DATE,-14);
        createExercise("1.Taylorreihen",scheduleINF3.lessons[2][2]._id,dateFromString(getDateTime(date.getTime())),1);

        date=(Calendar)tomorrow.clone();
        date.add(Calendar.DATE,3);
        createExercise("uebung11",scheduleINF3.lessons[0][0]._id,dateFromString(getDateTime(date.getTime())),11);
        //date=(Calendar)date.clone();
        date.add(Calendar.DATE,-7);
        createExercise("uebung10",scheduleINF3.lessons[0][0]._id,dateFromString(getDateTime(date.getTime())),10);
        //date=(Calendar)date.clone();
        date.add(Calendar.DATE,-7);
        createExercise("uebung09",scheduleINF3.lessons[0][0]._id,dateFromString(getDateTime(date.getTime())),9);

        date=(Calendar)tomorrow.clone();
        date.add(Calendar.DATE,7);
        createExercise("5.Graphalgorithmen",scheduleINF3.lessons[2][0]._id,dateFromString(getDateTime(date.getTime())),5);
        date.add(Calendar.DATE,-14);
        createExercise("4.AVL-Bäume",scheduleINF3.lessons[2][0]._id,dateFromString(getDateTime(date.getTime())),4);
        date.add(Calendar.DATE,-14);
        createExercise("3.Algorithmenanalyse",scheduleINF3.lessons[2][0]._id,dateFromString(getDateTime(date.getTime())),3);

        createExercise("MAD Final project",scheduleINF3.lessons[3][3]._id,dateFromString(getDateTime(date.getTime())),4);

    }
    public void createExercisesINF2(){
        Schedule scheduleINF2=getSchedule("INF2");
        createExercise("1.Dreiecke und Illuminaten",scheduleINF2.lessons[0][1]._id,dateFromString("2019-01-22 08:00:00"),1);
        createExercise("JavaFx GridView",scheduleINF2.lessons[0][0]._id,dateFromString("2019-01-07 09:50:00"),8);
        createExercise("Assembler",scheduleINF2.lessons[1][3]._id,dateFromString("2019-01-11 09:50:00"),3);
        createExercise("Linux",scheduleINF2.lessons[3][1]._id,dateFromString("2019-01-12 10:50:00"),3);
    }
    public void createAllExercises() {
        createExercisesINF2();
        createExercisesINF3();
    }
    public ArrayList<Exercise> queryExercises(String studies){
        long scheduleId=queryScheduleID(studies);
        /**Create new querybuilder**/
        SQLiteQueryBuilder _QB = new SQLiteQueryBuilder();

        /** Exercise<->Lesson<->Day<->Schedule **/
        _QB.setTables(EXERCISE_TABLE+
                " INNER JOIN " + LESSON_TABLE + " ON " +
                EXERCISE_TABLE+"."+EXERCISE_COL_LESSON + " = " + LESSON_TABLE+"."+BaseColumns._ID+
                " INNER JOIN " + DAY_TABLE + " ON " +
                LESSON_TABLE+"."+LESSON_COL_DAY + " = " + DAY_TABLE+"."+BaseColumns._ID+
                " INNER JOIN " + SCHEDULE_TABLE + " ON " +
                DAY_TABLE+"."+DAY_COL_SCHEDULE + " = " + SCHEDULE_TABLE+"."+BaseColumns._ID);

        String _OrderBy = EXERCISE_COL_DUE + " ASC";

        SQLiteDatabase db = database.getReadableDatabase();
        String[] projection={
                EXERCISE_TABLE+"."+BaseColumns._ID,
                EXERCISE_TABLE+"."+DummyDB.EXERCISE_COL_NAME,
                EXERCISE_TABLE+"."+DummyDB.EXERCISE_COL_LESSON,
                EXERCISE_TABLE+"."+DummyDB.EXERCISE_COL_DUE,
                EXERCISE_TABLE+"."+DummyDB.EXERCISE_COL_NR,
                LESSON_TABLE+"."+LESSON_COL_NAME_LONG,
                LESSON_TABLE+"."+LESSON_COL_NAME
        };
        String selection= SCHEDULE_TABLE+"."+BaseColumns._ID+"= ?";
        String[] args={""+scheduleId};
        Cursor c= _QB.query(db,projection,selection,args,null,null,_OrderBy);
        ArrayList<Exercise> exercises=new ArrayList<>();
        while(c.moveToNext()){
            exercises.add(new Exercise(c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID))
                    ,c.getString(c.getColumnIndexOrThrow(DummyDB.EXERCISE_COL_NAME)),
                    dateFromString(c.getString(c.getColumnIndexOrThrow(EXERCISE_COL_DUE))),
                    c.getInt(c.getColumnIndexOrThrow(DummyDB.EXERCISE_COL_NR)),
                    c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_NAME_LONG)),
                    c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_NAME))));
        }
        c.close();
        db.close();
        return exercises;
    }

    /** SCHEDULE API **/
    public long createSchedule(String studies,Lesson[][] lessons){
        SQLiteDatabase db=database.getWritableDatabase();
        ContentValues values=new ContentValues();
        values.put(DummyDB.SCHEDULE_COL_STUDIES,studies);
        long scheduleID=db.insert(DummyDB.SCHEDULE_TABLE,null,values);
        /**Insert all Days**/
        long[] dayID=new long[lessons[0].length];
        for (int days = 0; days < lessons[0].length; days++) {
            values=new ContentValues();
            values.put(DummyDB.DAY_COL_NAME,DAY_NAMES[days]);
            values.put(DummyDB.DAY_COL_SCHEDULE,scheduleID);
            dayID[days]=db.insert(DummyDB.DAY_TABLE,null,values);
            /**Inserts lessons of day **/
            long[] lessonID=new long[lessons.length];
            for (int hour = 0; hour < lessons.length; hour++) {
                values=new ContentValues();
                Lesson lesson=lessons[hour][days];
                if(lesson==null) {
                    values.put(DummyDB.LESSON_COL_NAME, "FREE");
                    values.put(DummyDB.LESSON_COL_START, hour);
                }
                else {
                    values.put(DummyDB.LESSON_COL_NAME, lesson.courseShort);
                    values.put(DummyDB.LESSON_COL_NAME_LONG, lesson.courseLong);
                    values.put(DummyDB.LESSON_COL_ROOM, lesson.room);
                    values.put(DummyDB.LESSON_COL_PROF, lesson.prof);
                    values.put(DummyDB.LESSON_COL_START, hour);
                    //values.put(DummyDB.LESSON_COL_END,Schedule.TIMEND[hour]);
                    values.put(DummyDB.LESSON_COL_DAY, dayID[days]);
                }
                lessonID[hour]=db.insert(DummyDB.LESSON_TABLE,null,values);
                if(lesson!=null)
                    lesson.setId(lessonID[hour]);
            }
            /** SET DAY IDs IN NEW SCHEDULE **/
            values=new ContentValues();
            values.put(SCHEDULE_DAY[days], dayID[days]);
            int rows=db.update(DummyDB.SCHEDULE_TABLE,values,BaseColumns._ID+" = ?",new String[]{""+scheduleID});
        }
        db.close();
        Log.d(TAG, "create: CREATED "+studies+" Schedule");
        return 1;
    }
    public void createAllSchedules() {
        createSchedule("INF3",SCHEDULE_INF3);
        createSchedule("INF2",SCHEDULE_INF2);
        createSchedule("INF4",SCHEDULE_INF4);
        Log.d(TAG, "CREATED all Schedules");
    }
    public Schedule getSchedule(String studies){
        SQLiteDatabase db=database.getReadableDatabase();
        String[] projection={
                BaseColumns._ID,
                DummyDB.SCHEDULE_COL_STUDIES,
                SCHEDULE_DAY[0],
                SCHEDULE_DAY[1],
                SCHEDULE_DAY[2],
                SCHEDULE_DAY[3],
                SCHEDULE_DAY[4],
        };
        String selection= DummyDB.SCHEDULE_COL_STUDIES+" LIKE ?";
        String[] args={studies};
        Cursor c= db.query(DummyDB.SCHEDULE_TABLE,projection,selection,args,null,null,null);
        if(!c.moveToFirst())
            return null;
        long scheduleID=c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
        long dayID[]={c.getLong(c.getColumnIndexOrThrow(DummyDB.SCHEDULE_DAY[0])),
                c.getLong(c.getColumnIndexOrThrow(DummyDB.SCHEDULE_DAY[1])),
                c.getLong(c.getColumnIndexOrThrow(DummyDB.SCHEDULE_DAY[2])),
                c.getLong(c.getColumnIndexOrThrow(DummyDB.SCHEDULE_DAY[3])),
                c.getLong(c.getColumnIndexOrThrow(DummyDB.SCHEDULE_DAY[4]))};
        c.close();
        Lesson[][] lessons=new Lesson[Schedule.TIMESTART.length][dayID.length];
        /** Get lessons of day **/
        for (int days = 0; days < dayID.length; days++) {
            projection=new String[]{
                    BaseColumns._ID,
                    DummyDB.LESSON_COL_DAY,
                    DummyDB.LESSON_COL_NAME,
                    DummyDB.LESSON_COL_NAME_LONG,
                    DummyDB.LESSON_COL_PROF,
                    DummyDB.LESSON_COL_ROOM,
                    DummyDB.LESSON_COL_START
            };
             selection= DummyDB.LESSON_COL_DAY+" = ?";
            args=new String[]{""+dayID[days]};
            c= db.query(DummyDB.LESSON_TABLE,projection,selection,args,null,null,LESSON_COL_START+" ASC");
           // ArrayList<Lesson> lessons=new ArrayList<>();
            while(c.moveToNext()){
                String name=c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_NAME));
                if(name.equals("FREE")){
                    lessons[c.getInt(c.getColumnIndexOrThrow(LESSON_COL_START))][days]=null;
                }
                else{
                    Lesson newLesson=new Lesson(name
                        ,c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_NAME_LONG)),
                        c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_ROOM)),
                        c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_PROF)));
                    newLesson.setId(c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID)));
                    lessons[c.getInt(c.getColumnIndexOrThrow(LESSON_COL_START))][days]=newLesson;

                }
            }
            c.close();
        }
        Schedule schedule=new Schedule(scheduleID,lessons);
        db.close();
        return schedule;
    }
    public long queryScheduleID(String studies){
        SQLiteDatabase db=database.getReadableDatabase();
        String[] projection={
                BaseColumns._ID,
                DummyDB.SCHEDULE_COL_STUDIES,
        };
        String selection= DummyDB.SCHEDULE_COL_STUDIES+" LIKE ?";
        String[] args={studies};
        Cursor c= db.query(DummyDB.SCHEDULE_TABLE,projection,selection,args,null,null,null);
        if(!c.moveToFirst())
            return -1;
        long scheduleID=c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
        return scheduleID;
    }

    /**Get distinct Lesson Names **/
    public ArrayList<String> queryLessonNames(String studies){
        long scheduleID=queryScheduleID(studies);
        /**Create new querybuilder**/
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        /** Lesson<->Day<->Schedule **/
        queryBuilder.setTables(LESSON_TABLE+
                " INNER JOIN " + DAY_TABLE + " ON " +
                LESSON_TABLE+"."+LESSON_COL_DAY + " = " + DAY_TABLE+"."+BaseColumns._ID+
                " INNER JOIN " + SCHEDULE_TABLE + " ON " +
                DAY_TABLE+"."+DAY_COL_SCHEDULE + " = " + SCHEDULE_TABLE+"."+BaseColumns._ID);
        queryBuilder.setDistinct(true);
        String orderBy = LESSON_TABLE+"."+LESSON_COL_NAME + " ASC";

        SQLiteDatabase db = database.getReadableDatabase();
        String[] projection={
                //LESSON_TABLE+"."+BaseColumns._ID,
                LESSON_TABLE+"."+DummyDB.LESSON_COL_NAME
                //LESSON_TABLE+"."+DummyDB.LESSON_COL_NAME_LONG
        };
        String selection= SCHEDULE_TABLE+"."+BaseColumns._ID+"= ?";
        String[] args={""+scheduleID};
        Cursor c= queryBuilder.query(db,projection,selection,args,null,null,orderBy);
        ArrayList<String> lessons=new ArrayList<>();
        while(c.moveToNext()){
                lessons.add(c.getString(c.getColumnIndexOrThrow(DummyDB.LESSON_COL_NAME)));
        }
        c.close();
        db.close();
        return lessons;
    }
    /**CALENDAR EVENTS API**/
    /**Get all Events sorted by Date**/
    public  ArrayList<Event> listCalendarEvents(String eventtitle) {
        Uri eventUri;
        if (android.os.Build.VERSION.SDK_INT <= 7)
            eventUri = Uri.parse("content://calendar/events");
         else
            eventUri = Uri.parse("content://com.android.calendar/events");

        ArrayList<Event> result = new ArrayList<>();
        String projection[] = { CalendarContract.Events._ID, CalendarContract.Events.TITLE,CalendarContract.Events.DESCRIPTION,CalendarContract.Events.DTEND,CalendarContract.Events.DTSTART };
        Cursor cursor = MainActivity.MAIN_ACTIVITY.getContentResolver().query(eventUri, null,
                CalendarContract.Events.DESCRIPTION+" LIKE ? AND "+CalendarContract.Events.TITLE+" LIKE ?",
                new String[]{"%"+SIGNATURE,"%"+eventtitle+"%"},
                CalendarContract.Events.DTSTART+" ASC");

        if (cursor.moveToFirst()) {

            String calName,calDesc;
            int calID;
            long calStart,calEnd;

            int nameCol = cursor.getColumnIndex(projection[1]);
            int idCol = cursor.getColumnIndex(projection[0]);
            int orgCol = cursor.getColumnIndex(projection[2]);
            int endCol = cursor.getColumnIndex(projection[3]);
            int startCol = cursor.getColumnIndex(projection[4]);
            do {
                calName = cursor.getString(nameCol);
                calID = cursor.getInt(idCol);
                calDesc=cursor.getString(orgCol);
                calStart=cursor.getLong(startCol);
                calEnd=cursor.getLong(endCol);
                //(int _id, String description, String title,Long start,Long end)
                    result.add(new Event(calID,calDesc,calName,calStart,calEnd));

            } while (cursor.moveToNext());
            cursor.close();
        }
        return result;

    }
    public  ArrayList<Event> listAllEvents() {
       return listCalendarEvents("");

    }
    public int deleteCalendarEvent(long entryID) {
        int iNumRowsDeleted = 0;
        Uri eventUri = ContentUris
                .withAppendedId(getCalendarUriBase(), entryID);
        iNumRowsDeleted = MainActivity.MAIN_ACTIVITY.getContentResolver().delete(eventUri, null, null);

        return iNumRowsDeleted;
    }
    public static Uri getCalendarUriBase() {
        Uri eventUri;
        if (android.os.Build.VERSION.SDK_INT <= 7)
            eventUri = Uri.parse("content://calendar/events");
        else
            eventUri = Uri.parse("content://com.android.calendar/events");

        return eventUri;
    }

    public static String getDateTime(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        String datestr= dateFormat.format(date);
        return datestr;
    }
    public static String getDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd", Locale.getDefault());
        String datestr= dateFormat.format(date);
        return datestr;
    }
    public static Date dateFromString(String string){
        //String string = "January 2, 2010";
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date= format.parse(string);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}

