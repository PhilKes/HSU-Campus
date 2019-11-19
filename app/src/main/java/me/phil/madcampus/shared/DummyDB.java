package me.phil.madcampus.shared;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/** Dummy database for Moodle Users **/
public class DummyDB extends SQLiteOpenHelper {

    public static final int DB_VERSION=1;
    public static final String DB_NAME="moodle.db";

    public static final String USER_TABLE="user",
                               USER_COL_NAME="name",
                               USER_COL_PASSWD="password",
                                USER_COL_STUDIES="studies",
                                USER_COL_AVATAR="avatar",
                                USER_COL_SCHEDULE="schedule";

    public static final String SCHEDULE_TABLE="schedule",
            SCHEDULE_COL_STUDIES="study";
    public static final String[] SCHEDULE_DAY={"monday", "tuesday", "wednesday", "thursday", "friday"};
    public static final String DAY_TABLE="day",
            DAY_COL_NAME="name",
            DAY_COL_SCHEDULE="schedule";
    public static final String LESSON_TABLE="lesson",
            LESSON_COL_NAME="lessonName",
            LESSON_COL_NAME_LONG="nameLong",
            LESSON_COL_ROOM="room",
            LESSON_COL_PROF="prof",
            LESSON_COL_START="start",
            LESSON_COL_END="ending",
            LESSON_COL_DAY="day";
    public static final String EXERCISE_TABLE="exercise",
            EXERCISE_COL_LESSON="lesson",
            EXERCISE_COL_NAME="name",
            EXERCISE_COL_NR="number",
            EXERCISE_COL_DUE="dueDate";

    private static final String SQL_CREATE_TABLE_USER ="CREATE TABLE "+USER_TABLE +" ("
            + BaseColumns._ID+" INTEGER PRIMARY KEY,"
            +USER_COL_NAME+" TEXT,"
            +USER_COL_PASSWD+" TEXT,"
            +USER_COL_STUDIES+" TEXT,"
            +USER_COL_AVATAR+" TEXT,"
            +USER_COL_SCHEDULE+" INTEGER,"
            +" FOREIGN KEY ("+USER_COL_SCHEDULE+") REFERENCES "+SCHEDULE_TABLE+"("+BaseColumns._ID+"));";

    private static final String SQL_CREATE_TABLE_SCHEDULE="CREATE TABLE "+SCHEDULE_TABLE +" ("
            + BaseColumns._ID+" INTEGER PRIMARY KEY,"
            +SCHEDULE_COL_STUDIES+" TEXT,"
            +SCHEDULE_DAY[0]+" INTEGER,"
            +SCHEDULE_DAY[1]+" INTEGER,"
            +SCHEDULE_DAY[2]+" INTEGER,"
            +SCHEDULE_DAY[3]+" INTEGER,"
            +SCHEDULE_DAY[4]+" INTEGER,"
            +" FOREIGN KEY ("+SCHEDULE_DAY[0]+") REFERENCES "+DAY_TABLE+"("+BaseColumns._ID+"),"
            +" FOREIGN KEY ("+SCHEDULE_DAY[1]+") REFERENCES "+DAY_TABLE+"("+BaseColumns._ID+"),"
            +" FOREIGN KEY ("+SCHEDULE_DAY[2]+") REFERENCES "+DAY_TABLE+"("+BaseColumns._ID+"),"
            +" FOREIGN KEY ("+SCHEDULE_DAY[3]+") REFERENCES "+DAY_TABLE+"("+BaseColumns._ID+"),"
            +" FOREIGN KEY ("+SCHEDULE_DAY[4]+") REFERENCES "+DAY_TABLE+"("+BaseColumns._ID+"));";

    private static final String SQL_CREATE_TABLE_DAY="CREATE TABLE "+DAY_TABLE +" ("
            + BaseColumns._ID+" INTEGER PRIMARY KEY,"
            +DAY_COL_NAME+" TEXT,"
            +DAY_COL_SCHEDULE+" INTEGER,"
            +" FOREIGN KEY ("+DAY_COL_SCHEDULE+") REFERENCES "+SCHEDULE_TABLE+"("+BaseColumns._ID+"));";

    private static final String SQL_CREATE_TABLE_LESSON="CREATE TABLE "+LESSON_TABLE +" ("
            + BaseColumns._ID+" INTEGER PRIMARY KEY,"
            +LESSON_COL_NAME+" TEXT,"
            +LESSON_COL_NAME_LONG+" TEXT,"
            +LESSON_COL_ROOM+" TEXT,"
            +LESSON_COL_PROF+" TEXT,"
            +LESSON_COL_START+" INTEGER,"
           // +LESSON_COL_END+" INTEGER,"
            +LESSON_COL_DAY+" INTEGER,"
            +" FOREIGN KEY ("+LESSON_COL_DAY+") REFERENCES "+DAY_TABLE+"("+BaseColumns._ID+"));";

    private static final String SQL_CREATE_TABLE_EXERCISE ="CREATE TABLE "+EXERCISE_TABLE +" ("
            + BaseColumns._ID+" INTEGER PRIMARY KEY,"
            +EXERCISE_COL_NAME+" TEXT,"
            +EXERCISE_COL_LESSON+" INTEGER,"
            +EXERCISE_COL_DUE+" DATE,"
            +EXERCISE_COL_NR+" INTEGER,"
            +" FOREIGN KEY ("+EXERCISE_COL_LESSON+") REFERENCES "+LESSON_TABLE+"("+BaseColumns._ID+"));";
    private static final String SQL_DELETE_TABLE="DROP TABLE IF EXISTS ";

    public DummyDB(Context context){
        super(context,DB_NAME,null,DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+USER_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+SCHEDULE_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+DAY_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+LESSON_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+EXERCISE_TABLE);

        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_USER);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_SCHEDULE);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_DAY);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_LESSON);
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE_EXERCISE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        /*sqLiteDatabase.execSQL(SQL_DELETE_TABLE+USER_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+SCHEDULE_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+DAY_TABLE);
        sqLiteDatabase.execSQL(SQL_DELETE_TABLE+LESSON_TABLE);*/
        onCreate(sqLiteDatabase);
    }
}
