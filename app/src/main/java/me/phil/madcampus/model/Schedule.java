package me.phil.madcampus.model;

public class Schedule {
    public static final String[] TIMESTART={"08:00","09:50","11:30","14:00","15:30"};
    public static final String[] TIMEND={"09:30","11:20","13:00","15:30","17:00"};

    //[LESSON][DAY]
   /* public static final Lesson[][] INF3_TEMPLATE={
            {new Lesson("PROG"),new Lesson("FENGL"),null,new Lesson("ANLY"),null},
            {new Lesson("PROG"),new Lesson("FENGL"),null,new Lesson("ALGO"),new Lesson("EMSYS")},
            {new Lesson("ALGO"),null,new Lesson("ANLY"),new Lesson("ALGO"),new Lesson("EMSYS")},
            {null,null,null,null,null}
    };*/
   public Lesson[][] lessons;
    public final int days;
    public final int hours;
    public long _ID=-1;
    public Schedule(Lesson[][] lessons) {
        days=lessons[0].length;
        hours=lessons.length;
        this.lessons=lessons;
    }
    public Schedule(long id,Lesson[][] lessons) {
        days=lessons[0].length;
        hours=lessons.length;
        this.lessons=lessons;
        _ID=id;
    }
    public Lesson getLesson(int i){

        return lessons[i/days][i%days];
    }
    public int getLastLesson(int i){
        if(i>0)
            return i-days;
        return i;
    }
    public int getDayOfLesson(int i){
       return (i)%lessons[0].length;
   }
    public int getHourOfLesson(int i){
        return (i)/lessons[0].length;
    }
    public String getStartOfLesson(int i){
        return TIMESTART[getHourOfLesson(i)];
    }
    public String getEndOfLesson(int i){
        return TIMEND[getHourOfLesson(i)];
    }
}

