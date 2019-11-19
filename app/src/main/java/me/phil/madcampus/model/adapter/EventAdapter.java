package me.phil.madcampus.model.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import me.phil.madcampus.MainActivity;
import me.phil.madcampus.R;
import me.phil.madcampus.model.Event;
import me.phil.madcampus.shared.DummyApi;

public class EventAdapter extends BaseAdapter {
    private ArrayList<Event> events;
    private Context context;
    private DummyApi api;
    public EventAdapter(Context context,DummyApi api,ArrayList<Event> allEvents) {
        events=allEvents;
        this.context=context;
        this.api=api;
    }

    @Override
    public int getCount() {
        return events.size();
    }

    @Override
    public Object getItem(int i) {
        return events.get(i);
    }

    @Override
    public long getItemId(int i) {
        return events.get(i)._id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context=viewGroup.getContext();
        if(view==null) {
            LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=layoutInflater.inflate(R.layout.event_item,null);
        }
        Event event=events.get(i);
        /**Highlight today's notifications (selector in drawable)**/
        if(event.today)
            view.setEnabled(false);
        else
            view.setEnabled(true);
        TextView txtName= view.findViewById(R.id.txt_title);
        txtName.setText(event.title);
        TextView txtStart=view.findViewById(R.id.txt_due);

        DateFormat sdf = new SimpleDateFormat("EEE,dd.MM HH:mm");
        txtStart.setText(sdf.format(event.start.getTime()));
        /**Show confirm dialog before deletion**/
        Button btnDel=view.findViewById(R.id.btn_delete_avatar);
        btnDel.setOnClickListener(v->{
            showConfirmDialog(event);
        });
        Button btnEdit=view.findViewById(R.id.btn_upload);
        btnEdit.setOnClickListener(v->{
            long calendarEventID = event._id;
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("content://com.android.calendar/events/" + String.valueOf(calendarEventID)));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        return view;
    }
    /**Confirm dialog for event deletion **/
    private void showConfirmDialog(Event event) {
        MainActivity.showConfirmDialog(context, R.string.delevent, context.getString(R.string.delete) + " "+event.title+" " + context.getString(R.string.eventask),
                (dialog, which) -> {
                /**Delete calendar event**/
                int rows=api.deleteCalendarEvent(event._id);
                if(rows>0){
                    Toast.makeText(context, "Deleted: " + event.title +" event", Toast.LENGTH_SHORT).show();
                    events.remove(event);
                    notifyDataSetChanged();
                }
                else
                    Toast.makeText(context, "Could not delete: " + event.title +" event", Toast.LENGTH_SHORT).show();
        });
    }
}
