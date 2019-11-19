package me.phil.madcampus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import me.phil.madcampus.fragments.EventsFragment;
import me.phil.madcampus.fragments.ExerciseFragment;
import me.phil.madcampus.fragments.OptionsFragment;
import me.phil.madcampus.fragments.ScheduleFragment;
import me.phil.madcampus.fragments.SecurityFragment;
import me.phil.madcampus.model.Exercise;
import me.phil.madcampus.model.Lesson;
import me.phil.madcampus.model.User;
import me.phil.madcampus.shared.DummyApi;
import me.phil.madcampus.shared.IDialogOptions;
import me.phil.madcampus.shared.Preferences;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    public static final double UNI_LONGITUDE=9.997489999999999;
    public static final double UNI_LATITIUDE=48.40885000000001;
    private static final String TAG = "MainActivity";
    public static final int ID_LOGIN = -1;
    public static MainActivity MAIN_ACTIVITY;
    public static final int HOME_FRAGMENT = R.id.nav_schedule;
    public static final int MAX_IMAGE_SIZE = 1000;

    private DummyApi api;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private User currUser;
    private int currFragment = HOME_FRAGMENT;
    private View headerLayout;
    private Lesson selectedLesson;
    private long clickedExercise = -1;

    private FloatingActionButton fab;
    private GoogleApiClient googleClient;
    private LocationRequest locationRequest;
    private Location location;
    private Location homeLocation;
    /** Go to Uni if atHome ||(!atHome&&!atUni), Go home if atUni**/
    private boolean atHome=false;
    private boolean atUni=false;

    @SuppressLint({"RestrictedApi", "MissingPermission", "NewApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MAIN_ACTIVITY = this;
        api = new DummyApi(this);
        setTheme(R.style.AppTheme_NoActionBar);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((menuItem) -> {
            //menuItem.setChecked(true);
            drawerLayout.closeDrawers();
            return switchFragment(menuItem.getItemId());
        });

        /**Listener for Drawer Events**/
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // Respond when the drawer's position changes
            }

            /** Load notfication count**/
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDrawerOpened(View drawerView) {

                MenuItem item = navigationView.getMenu().findItem(R.id.nav_notifications);
                TextView txtCount = item.getActionView().findViewById(R.id.txt_notify_count);
                Calendar today = Calendar.getInstance();
                today.setTime(new Date());
                //get number of events today
                long count =
                        api.listAllEvents().stream().filter(ev ->
                                ev.start.get(Calendar.DAY_OF_WEEK) == today.get(Calendar.DAY_OF_WEEK)).count();
                if (count > 0) {
                    txtCount.setText("" + count);
                    txtCount.setVisibility(View.VISIBLE);
                } else
                    txtCount.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Respond when the drawer is closed
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // Respond when the drawer motion state changes
            }
        });
        View mainContainer = findViewById(R.id.main_container);
        /**Set Toolbar as Actionbar, navigationView layout**/
        Toolbar toolbar = mainContainer.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        headerLayout = navigationView.inflateHeaderView(R.layout.activity_main_drawer_header);

        /**CHECK GOOGLE PLAY VERSION **/
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (available == ConnectionResult.SUCCESS) {
            Log.d("MAIN", "onCreate: Valid Google Play version found");
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, 9001);
            dialog.show();
        }
        /** Init. Google API Client ->onConnected() called if successful**/
        if (googleClient == null) {
            googleClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        /**FLOATING ACTION BUTTON FOR NAVIGATION HOME/ TO UNI -> LOCATION AWARE**/
        fab = mainContainer.findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            // LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, locationRequest, this);
            Uri uri;
            if (atHome || (!atHome && !atUni))
                uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+UNI_LATITIUDE+","+UNI_LONGITUDE+"&travelmode=transit");
            else
                uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination="+homeLocation.getLatitude()+","+homeLocation.getLongitude()+"&travelmode=transit");
            Intent intent = new Intent(Intent.ACTION_VIEW,uri);
            startActivity(intent);
        });
        locationRequest = new LocationRequest()
                .setNumUpdates(1)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (savedInstanceState == null) {
            Bundle bundle = getIntent().getExtras();
            int id = bundle.getInt("fragment");
            if (id == ID_LOGIN) {
                currUser = getIntent().getParcelableExtra("user");
                navigationView.setCheckedItem(R.id.nav_schedule);
                switchFragment(HOME_FRAGMENT);
            } else {
                SharedPreferences prefs = getSharedPreferences(Preferences.PREF_LOGIN, Context.MODE_PRIVATE);
                currUser = api.validateLogin(prefs.getString(Preferences.USER_NAME, Preferences.DEFAULT_NAME), prefs.getString(Preferences.USER_PW, Preferences.DEFAULT_PW));
                /** Notification Intent **/
                clickedExercise = bundle.getLong("option");
                navigationView.setCheckedItem(id);
                switchFragment(id);
            }
        }
        TextView name = headerLayout.findViewById(R.id.txt_user2);
        name.setText(currUser.name);
        updateAvatar();

        /**Assignment Notifications */
        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.setTime(today.getTime());
        tomorrow.add(Calendar.DATE, 2);

        /** Notify on assignments due tomorrow */
        ArrayList<Exercise> exercisesDue = api.queryExercises(currUser.studies);
        exercisesDue.stream().filter(ex -> {
            Calendar due = Calendar.getInstance();
            due.setTime(ex.dueDate);
            return (!due.before(today) && due.before(tomorrow));
        }).forEach(ex -> {
            NotificationManager notificationManager;
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification status = null;
            Intent notificationIntent = new Intent(getBaseContext(), MainActivity.class);
            notificationIntent.putExtra("fragment", R.id.nav_exercises);
            notificationIntent.putExtra("option", ex._id);

            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            status = new Notification.Builder(getBaseContext())
                    .setSmallIcon(R.drawable.ic_assignments)
                    .setContentTitle(ex.lessonName + " " + getString(R.string.exercises))
                    .setStyle(new Notification.BigTextStyle().bigText(ex.name + "\nis due on " + DummyApi.getDateTime(ex.dueDate)))
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setVibrate(new long[]{1000,500,1000,500})
                    .setOngoing(false)
                    .build();
            notificationManager.notify(new Random().nextInt(), status);

        });
    }

    /** Connect /Disconnect Google API Client **/
    @Override
    protected void onStart() {
        super.onStart();
        googleClient.connect();
        homeLocation=getHomeLocation(this);
        /** Ask User to set their home location **/
        if(homeLocation==null){
            switchFragment(R.id.nav_other);
            navigationView.setCheckedItem(R.id.nav_other);
            showConfirmDialog(this,R.string.home_location,R.string.pick_location, (dialog, which) -> {});
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleClient, this);
            googleClient.disconnect();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        googleClient.connect();
        //LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, locationRequest, this);
    }

    /** Replaces fragment or returns false(for correct item selection(->logout)) **/
    @SuppressLint("MissingPermission")
    public boolean switchFragment(int itemId) {
        Fragment fragment = null;
        Bundle bundle;
        fab.setVisibility(View.VISIBLE);
        /**initializing the selected fragment object**/
        switch (itemId) {
           /* case R.id.nav_menu1:
                fragment = new Menu1Fragment();
                break;*/
            case R.id.nav_notifications:
                fragment = new EventsFragment(this,api);
                fab.setVisibility(View.INVISIBLE);
                /**Tell Fragment to Show only Events of selectedLesson */
                if(selectedLesson!=null) {
                    bundle= new Bundle();
                    bundle.putString("lesson",selectedLesson.courseShort);
                    fragment.setArguments(bundle);
                    selectedLesson=null;
                    navigationView.setCheckedItem(itemId);
                }
                break;
            case R.id.nav_exercises:
                bundle = new Bundle();
                bundle.putParcelable("user", currUser);
                bundle.putLong("exercise",clickedExercise);
                fragment = new ExerciseFragment(this,api);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_schedule:
                bundle = new Bundle();
                bundle.putParcelable("user", currUser);
                fragment = new ScheduleFragment(this);
                fragment.setArguments(bundle);
                break;
            case R.id.nav_security:
                bundle = new Bundle();
                bundle.putParcelable("user", currUser);
                fragment = new SecurityFragment(this,api);
                fragment.setArguments(bundle);
                fab.setVisibility(View.INVISIBLE);
                break;
            case R.id.nav_other:
                bundle = new Bundle();
                bundle.putParcelable("user", currUser);
                fragment = new OptionsFragment(this,api);
                fragment.setArguments(bundle);
                fab.setVisibility(View.INVISIBLE);
                break;
            case R.id.nav_logout:
                showLogoutDialog();
                navigationView.setCheckedItem(currFragment);
                return false;
            default:
                bundle = new Bundle();
                bundle.putParcelable("user", currUser);
                fragment = new ScheduleFragment(this);
                fragment.setArguments(bundle);
                break;
        }
        /**replacing the fragment**/
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main_content, fragment).commit();

        }
        /**Update user if coming from security fragment**/
        if(currFragment==R.id.nav_security){
            Fragment curFragment = getSupportFragmentManager().findFragmentById(R.id.main_content);
            if(curFragment.getArguments().getBoolean("confirmed")) {
                currUser = curFragment.getArguments().getParcelable("user");
                updateAvatar();
            }
        }
        /** Update homeLocation if coming from other options **/
        else if(currFragment==R.id.nav_other) {
            homeLocation=getHomeLocation(this);
           // LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, locationRequest, this);
        }
        currFragment=itemId;
        return true;
    }

    /** Load Homelocation from Preferences */
    public static Location getHomeLocation(Activity activity) {
        SharedPreferences prefs =activity.getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
        double latitude=Double.parseDouble(prefs.getString(Preferences.HOME_LATITUDE,"0"));
        double longitude=Double.parseDouble(prefs.getString(Preferences.HOME_LONGITUDE,"0"));
        if(latitude==0 ||longitude==0)
            return null;
        Location location=new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        Log.d(TAG, "switchFragment: homeLocation : Latitude :"+latitude+" Longitude: "+longitude);
        return location;
    }

    private void updateAvatar(){
        if(currUser.avatar==null) {
            /** Set default avatar**/
            ImageView myImage =headerLayout.findViewById(R.id.img_avatar);
            myImage.setImageDrawable(getResources().getDrawable(R.drawable.round_image_view));
            return;
        }

        ImageView myImage =headerLayout.findViewById(R.id.img_avatar);
        Bitmap avatarBitmap=getScaledBitmap(currUser.avatar);
        myImage.setImageBitmap(avatarBitmap);
        /**Zoom image view on click**/
        myImage.setOnClickListener(view -> {
            Dialog dialog = new Dialog(this);
            /**Show Image zoom in dialog**/
            View dialogLayout = getLayoutInflater().inflate(R.layout.image_view, null);
            ImageView img = dialogLayout.findViewById(R.id.img_zoom);
            img.setImageBitmap(avatarBitmap);
            dialog.setContentView(dialogLayout);
            dialog.getWindow().setBackgroundDrawable(
                    new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            //dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            dialog.show();
        });
    }

    public void setSelectedLesson(Lesson lesson){
        selectedLesson=lesson;
    }
    @Override
    public void onBackPressed() {
        /** Return to Home Fragment(Schedule) or logout**/
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_content);
        if (!(f instanceof ScheduleFragment)) {
            switchFragment(HOME_FRAGMENT);
            navigationView.setCheckedItem(HOME_FRAGMENT);
            return;
        }
        showLogoutDialog();
    }
    private void showLogoutDialog(){
        showConfirmDialog(this,R.string.logout,R.string.logingout, (dialog, which) -> MainActivity.super.onBackPressed());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            /** Menu icon from toolbar is pressed**/
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** GOOGLE API **/
    @Override
    public void onConnected(@Nullable Bundle bundle) throws SecurityException{
        Log.d(TAG, "onConnected: Google Client connected");
        /**Update location **/
        LocationServices.FusedLocationApi.requestLocationUpdates(googleClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: Couldnt connect Google API Client!");
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location=location;
        logLocation(location);
        if(homeLocation==null)
            return;
        Location uniLocation = new Location("");
        uniLocation.setLongitude(UNI_LONGITUDE);
        uniLocation.setLatitude(UNI_LATITIUDE);
        logLocation(uniLocation);

        double distanceToHome=(location.distanceTo(homeLocation)/1000);
        Log.d(TAG, "onLocationChanged: Distance to Home: "+distanceToHome+" km");
        atHome=(distanceToHome<1.0);
        double distanceToUni=(location.distanceTo(uniLocation)/1000);
        atUni=distanceToUni<0.5;
        Log.d(TAG, "onLocationChanged: Distance to Uni: "+distanceToUni+" km");
        if(atHome||(!atHome&&!atUni)){
            fab.setImageResource(R.drawable.ic_maps_uni);
            Log.d(TAG, "onLocationChanged: At home or not at the Uni");
        }
        else if(atUni) {
            fab.setImageResource(R.drawable.ic_maps_home);
            Log.d(TAG, "onLocationChanged: At the Uni");
        }

    }
    private void logLocation(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> locationAdresses = null;
        try {
            locationAdresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);
            Address locationAddress;
            if (locationAdresses != null && locationAdresses.size() > 0) {
                locationAddress = locationAdresses.get(0);
                String address = locationAddress.getAddressLine(0);
                String city = locationAddress.getLocality();
                String state = locationAddress.getAdminArea();
                String country = locationAddress.getCountryName();
                String postalCode = locationAddress.getPostalCode();
                String knownName = locationAddress.getFeatureName();
                Log.d(TAG, "logLocation: " + "Latitude: " + locationAddress.getLatitude() + " Longitude: " + locationAddress.getLongitude() + "\n"
                        + country + " , " + city + " , " + address + " , " + knownName);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "logLocation: Could not find location");
        }
    }

    /**UTILITIES**/

    /** Confirm Dialog **/
    public static void showConfirmDialog(Context context,int title,int text,IDialogOptions options){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog))
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(R.string.yes, options::yesOption)
                .setNegativeButton(R.string.no, options::noOption);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static void showConfirmDialog(Context context,int title,String text,IDialogOptions options){
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.myDialog))
                .setCancelable(true)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton(R.string.yes, options::yesOption)
                .setNegativeButton(R.string.no, options::noOption);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /**Rescales Image if file is too big**/
    public static Bitmap getScaledBitmap(String path){
        File imgFile = new File(path);
        if (!imgFile.exists())
            return null;
        Bitmap myBitmap =null;
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        try {
            FileInputStream fis = new FileInputStream(imgFile);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
            int scale = 1;
            if (o.outHeight > MAX_IMAGE_SIZE || o.outWidth > MAX_IMAGE_SIZE) {
                scale = (int) Math.pow(2, (int) Math.ceil(Math.log(MAX_IMAGE_SIZE / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(imgFile);
            myBitmap = BitmapFactory.decodeStream(fis, null, o2);
            //myImage.setImageBitmap(myBitmap);
            final Bitmap bit = myBitmap;
            fis.close();
        }catch (IOException e){e.printStackTrace();}
        return myBitmap;
    }
}
