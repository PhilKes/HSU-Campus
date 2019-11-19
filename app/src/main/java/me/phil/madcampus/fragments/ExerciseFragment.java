package me.phil.madcampus.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import me.phil.madcampus.R;
import me.phil.madcampus.model.Exercise;
import me.phil.madcampus.model.User;
import me.phil.madcampus.model.adapter.ExerciseAdapter;
import me.phil.madcampus.shared.DummyApi;

/** List of all Exercise that are due **/
@SuppressLint("ValidFragment")
public class ExerciseFragment extends Fragment {
    DummyApi api;
    User user;
    ListView listExercises/*,todayEvents*/;
    private long selectedExercise;
    private Context context;
    private Spinner spinSort;
    private ArrayList<Exercise> allExercises;
    private ArrayList<Exercise> selectedExercises;
    private ExerciseAdapter exerciseAdapter;
    private TextView txtMsg;
    @SuppressLint("ValidFragment")
    public ExerciseFragment(Context context, DummyApi api) {
        this.api=api;
        this.context=context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_exercises, container, false);
        //todayEvents=view.findViewById(R.id.list_today);
        listExercises =view.findViewById(R.id.list_all);
        spinSort=view.findViewById(R.id.spin_sort);
        txtMsg=view.findViewById(R.id.txt_message);
        user=getArguments().getParcelable("user");
        if(getArguments()!=null){
            selectedExercise =getArguments().getLong("exercise");
        }

        return view;
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle(getString(R.string.exercises));
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        allExercises = api.queryExercises(user.studies);
        selectedExercises=new ArrayList<>();
        selectedExercises.addAll(allExercises);
        ArrayList<String> lessons=api.queryLessonNames(user.studies);
        lessons.add(0,"All");
        ArrayAdapter<String> lessonAdapter=new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,lessons);
        final Switch showOld=view.findViewById(R.id.toggle_old);
        showOld.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                updateExerciseList((String)spinSort.getSelectedItem(),b);
            }
        });
        spinSort.setAdapter(lessonAdapter);
        /** Show exercies of selected Lesson **/
        spinSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String sort=(String)spinSort.getItemAtPosition(i);
                updateExerciseList(sort,showOld.isChecked());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedExercises.clear();
                selectedExercises.addAll(allExercises);
                exerciseAdapter.notifyDataSetChanged();
            }
        });
        exerciseAdapter=new ExerciseAdapter(context, api, selectedExercises);
        listExercises.setAdapter(exerciseAdapter);
        /** Highlight selected Exercise from notification **/
        if(selectedExercise!=-1) {
            int selIdx = 0;//allExercises.indexOf(allExercises.stream().filter(ex -> ex._id == selectedExercise).findFirst());
            for (int i = 0; i < allExercises.size(); i++) {
                if (allExercises.get(i)._id == selectedExercise) {
                    selIdx = i;
                    break;
                }
            }
            listExercises.setItemChecked(selIdx, true);
            listExercises.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    view.setSelected(true);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            listExercises.setSelection(selIdx);
        }
    }
    /** Updates ListView to selection and if to show old assignments or not **/
    private void updateExerciseList(String sort,boolean showOld){
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        selectedExercises.clear();
        if(!sort.equals("All"))
            selectedExercises.addAll(allExercises.stream().filter(ex ->
                    ex.lessonShort.equals(sort)).collect(Collectors.toList()));
        else
            selectedExercises.addAll(allExercises);
        if(!showOld){
            ArrayList<Exercise> all=new ArrayList<>();
            all.addAll(selectedExercises);
            selectedExercises.clear();
            selectedExercises.addAll(all.stream().filter(ex->
                    ex.dueDate.after(now.getTime())).collect(Collectors.toList()));
        }
        exerciseAdapter.notifyDataSetChanged();
        if(selectedExercises.isEmpty()) {
            txtMsg.setText(R.string.no_assignments);
            txtMsg.setVisibility(View.VISIBLE);
        }else
            txtMsg.setVisibility(View.INVISIBLE);

    }
}
