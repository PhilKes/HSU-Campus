package me.phil.madcampus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.util.Locale;

import me.phil.madcampus.model.User;
import me.phil.madcampus.shared.DummyApi;
import me.phil.madcampus.shared.Preferences;

public class LoginActivity extends AppCompatActivity implements View.OnFocusChangeListener {
    public static final String TAG="LoginActivity";
    public static String LANGUAGE="en";

    private boolean changedLanguage=false;
    private DummyApi api;
    private EditText editTxt_Login,editTxt_Passwd;
    private Switch togglePref;
    private Button btnLang;
    ProgressDialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**CHECK PERMISSIONS **/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR,
                                Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},
                        101);
                while (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
            }
        }
        /** LOAD LANGUAGE **/
        SharedPreferences prefs=getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
        LANGUAGE=prefs.getString(Preferences.LANGUAGE,"en");
        changeLanguage(LANGUAGE);
        changedLanguage=false;
        api=new DummyApi(this);
        prefs=getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
        /**Reset Database if enabled **/
        if(prefs.getBoolean(Preferences.RESET_USERS,true)) {
            api.resetAll();
            api.createAllSchedules();
            api.createAllExercises();
            //ArrayList<Exercise> exercises=api.queryExercises("INF3");
            api.createUser(Preferences.DEFAULT_NAME,Preferences.DEFAULT_PW,"INF4");
            api.createUser("inf3",Preferences.DEFAULT_PW,"INF3");
            api.createUser("inf2",Preferences.DEFAULT_PW,"INF2");
            api.createUser("inf4",Preferences.DEFAULT_PW,"INF4");
            SharedPreferences.Editor editor=prefs.edit();
            editor.putBoolean(Preferences.RESET_USERS,false);
            editor.apply();
        }

        Log.d(TAG, "onCreate: ---ALL USERS:---");
        for (User user: api.getAllUsers())
           Log.d("SQL DB", user.toString());
        Log.d(TAG, "onCreate: ---INF3 USERS:---");
        for (User user: api.getAllFromStudies("INF3"))
            Log.d("SQL DB", user.toString());
        Log.d(TAG, "onCreate: ---INF2 USERS:---");
        for (User user: api.getAllFromStudies("INF2"))
            Log.d("SQL DB", user.toString());
        loadLayout();

    }
    public void loadLayout(){
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editTxt_Login=findViewById(R.id.edit_login);
        editTxt_Passwd=findViewById(R.id.edit_newpwdconfirm);
        togglePref=findViewById(R.id.toggle_pref);
        btnLang=findViewById(R.id.btn_language);
        if(LANGUAGE.equals("de")) {
            btnLang.setBackground(getResources().getDrawable(R.drawable.german));
        }
        else {
            btnLang.setBackground(getResources().getDrawable(R.drawable.english));
        }
        findViewById(R.id.btn_confirm).requestFocus();
        findViewById(R.id.frame).startAnimation(
                AnimationUtils.loadAnimation(LoginActivity.this,R.anim.login_fade));
        /** Restore Name and Password if option was enabled **/
        editTxt_Login.setText(Preferences.DEFAULT_NAME);
        editTxt_Passwd.setText(Preferences.DEFAULT_PW);
        SharedPreferences prefs=getSharedPreferences(Preferences.PREF_LOGIN,Context.MODE_PRIVATE);
        togglePref.setChecked(prefs.getBoolean(Preferences.SAVE_CREDENTIALS,false));
        if(togglePref.isChecked()) {
            editTxt_Login.setText(prefs.getString(Preferences.USER_NAME, Preferences.DEFAULT_NAME));
            editTxt_Passwd.setText(prefs.getString(Preferences.USER_PW, Preferences.DEFAULT_PW));
        }

        /**Auto login if enabled**/
        prefs=getSharedPreferences(Preferences.PREF_LOGIN,Context.MODE_PRIVATE);
        if(prefs.getBoolean(Preferences.AUTO_LOGIN,false))
            onClickLogin(findViewById(R.id.btn_confirm));
    }
    public void onClickLogin(View view) {
        /**Show loading dialog**/
        dialog=new ProgressDialog(LoginActivity.this);
        dialog.setMessage(getString(R.string.loginin));
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.requestFocus();
        new LoadingDialogTask().execute();
    }

    /** Hide virtual Keyboard **/
    @Override
    public void onFocusChange(View view, boolean b) {
        InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(dialog!=null)
            dialog.dismiss();
        /** Dont mess up the back stack **/
        if(changedLanguage)
            finish();
    }
/*    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // refresh your views here
        loadLayout();
        super.onConfigurationChanged(newConfig);
    }*/
    /** Change configuration to new language, save in preferences,restart activity **/
    public void onLanguageChange(View view) {
        if(LANGUAGE.equals("en")) {
            LANGUAGE = "de";
            btnLang.setBackground(getResources().getDrawable(R.drawable.german));
        }
        else {
            LANGUAGE = "en";
            btnLang.setBackground(getResources().getDrawable(R.drawable.english));
        }
        SharedPreferences prefs=getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=prefs.edit();
        editor.putString(Preferences.LANGUAGE, LANGUAGE);
        editor.apply();
        changeLanguage(LANGUAGE);
        // TODO RELOAD WITH onConfigurationChanged()
        //finish();
        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
        overridePendingTransition(R.anim.from_middle,R.anim.to_middle);
        finish();
    }
    private void changeLanguage(String lang){
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = new Locale(LANGUAGE);
        res.updateConfiguration(conf, dm);
        changedLanguage=true;
    }

    /** Loading Dialog while logging in **/
    class LoadingDialogTask extends
            AsyncTask<String, Integer, Boolean> {
        User user;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            /**Save preference if valid login  and start MainActivity**/
            if(user!=null)
            {
                SharedPreferences prefs=getSharedPreferences(Preferences.PREF_LOGIN,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=prefs.edit();
                editor.putString(Preferences.USER_NAME, editTxt_Login.getText().toString());
                editor.putString(Preferences.USER_PW, editTxt_Passwd.getText().toString());
                editor.putBoolean(Preferences.SAVE_CREDENTIALS,togglePref.isChecked());
                editor.apply();
                Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
                mainIntent.putExtra("user",user);
                mainIntent.putExtra("fragment",MainActivity.ID_LOGIN);
                startActivity(mainIntent);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
            }
            else{
                Toast.makeText(LoginActivity.this, R.string.invalid,Toast.LENGTH_SHORT).show();
                LoginActivity.this.dialog.dismiss();
            }
            super.onPostExecute(result);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            user=api.validateLogin(editTxt_Login.getText().toString(),editTxt_Passwd.getText().toString());
            return true;

        }

    }
}
