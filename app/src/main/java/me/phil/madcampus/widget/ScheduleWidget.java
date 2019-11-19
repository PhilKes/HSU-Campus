package me.phil.madcampus.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.Date;

import me.phil.madcampus.LoginActivity;
import me.phil.madcampus.R;

/** Home Screen Widget for Schedule **/
public class ScheduleWidget extends AppWidgetProvider {

    private static final int[] dayID={R.id.txt_day0,R.id.txt_day1,R.id.txt_day2,R.id.txt_day3,R.id.txt_day4};
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId:appWidgetIds){
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.view_grid);

            Intent intent = new Intent(context, RemoteScheduleService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            Intent intentClick = new Intent(context, LoginActivity.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context, 0, intentClick, 0);

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rv.setOnClickPendingIntent(R.id.widget_frame, pendingIntent);
            //rv.setBoolean(R.id.view_grid,"setVerticalScrollBarEnabled",false);
            Date now=new Date();
            Calendar c = Calendar.getInstance();
            c.setTime(now);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK)-2;
            //Monday-Friday
            if(dayOfWeek<5 && dayOfWeek>=0) {
               // layoutDays.getChildAt(dayOfWeek).setSelected(true);
                rv.setBoolean(dayID[dayOfWeek],"setEnabled",false);
            }
            rv.setRemoteAdapter(R.id.view_grid, intent);

            appWidgetManager.updateAppWidget(widgetId, rv);
        }
    }
}
