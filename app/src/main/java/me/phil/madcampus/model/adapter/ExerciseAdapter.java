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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import me.phil.madcampus.MainActivity;
import me.phil.madcampus.R;
import me.phil.madcampus.model.Event;
import me.phil.madcampus.model.Exercise;
import me.phil.madcampus.shared.DummyApi;

public class ExerciseAdapter extends BaseAdapter {
    private ArrayList<Exercise> exercises;
    private Context context;
    private DummyApi api;
    public ExerciseAdapter(Context context, DummyApi api, ArrayList<Exercise> allExercises) {
        exercises=allExercises;
        this.context=context;
        this.api=api;
    }

    @Override
    public int getCount() {
        return exercises.size();
    }

    @Override
    public Object getItem(int i) {
        return exercises.get(i);
    }

    @Override
    public long getItemId(int i) {
        return exercises.get(i)._id;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context=viewGroup.getContext();
        if(view==null) {
            LayoutInflater layoutInflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view=layoutInflater.inflate(R.layout.exercise_item,null);
        }
        Exercise exercise=exercises.get(i);
        TextView txtName= view.findViewById(R.id.txt_title);
        txtName.setText(exercise.name);
        TextView txtDue= view.findViewById(R.id.txt_due);
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd.MMM, HH:mm", Locale.getDefault());
        txtDue.setText(dateFormat.format(exercise.dueDate));
        TextView txtLesson= view.findViewById(R.id.txt_lesson);
        txtLesson.setText(exercise.lessonName);

        Calendar today=Calendar.getInstance();
        today.setTime(new Date());
        Calendar due=Calendar.getInstance();
        due.setTime(exercise.dueDate);

        Button btnUpload=view.findViewById(R.id.btn_upload);
        /** MARK OLD EXERCISES **/
        if(due.getTime().before(today.getTime())) {
            /**(see state-selector in drawable exercise_item_highlight.xml**/
            view.setActivated(false);
            btnUpload=view.findViewById(R.id.btn_upload);
            btnUpload.setText(R.string.rating);
            btnUpload.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_rating, 0, 0, 0);
        }
        else{
            view.setActivated(true);
            /** Mark due exercises **/
            today.add(Calendar.DATE,2);
            if(due.before(today))
                view.setEnabled(false);
            else
                view.setEnabled(true);
            btnUpload.setText(R.string.upload);
            btnUpload.setCompoundDrawablesWithIntrinsicBounds( R.drawable.ic_upload, 0, 0, 0);
        }

        /**Moodle Link **/
        btnUpload.setOnClickListener(v->{
            Uri uri = Uri.parse("https://moodle-hs-ulm.de/login/index.php");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            context.startActivity(intent);
        });
        return view;
    }
}
