package me.phil.madcampus.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import me.phil.madcampus.MainActivity;
import me.phil.madcampus.R;
import me.phil.madcampus.model.User;
import me.phil.madcampus.shared.DummyApi;
import me.phil.madcampus.shared.Preferences;

import static android.app.Activity.RESULT_OK;

/** Account settings for password,auto-login,avatar **/
public class OptionsFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener{
    private static final int PLACE_PICKER_REQUEST = 1;
    private User user;
    private Switch toggleAutoLogin,toggleNotifyLessonStart,toggleResetUsers;
    private DummyApi api;
    private TextView txtLocation;
    Context context;
    public OptionsFragment() {
        super();
    }

    @SuppressLint("ValidFragment")
    public OptionsFragment(Context context, DummyApi api) {
        this.context=context;
        this.api=api;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_options, container, false);
        user=getArguments().getParcelable("user");

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleAutoLogin=view.findViewById(R.id.toggle_autologin);
        toggleNotifyLessonStart=view.findViewById(R.id.toggle_notifylesson);
        toggleResetUsers=view.findViewById(R.id.toggle_reset);
        SharedPreferences prefs=getActivity().getSharedPreferences(Preferences.PREF_LOGIN,Context.MODE_PRIVATE);
        toggleAutoLogin.setChecked(prefs.getBoolean(Preferences.AUTO_LOGIN,false));
        toggleAutoLogin.setOnCheckedChangeListener((compoundButton, val) -> {
            SharedPreferences prefs1 =getActivity().getSharedPreferences(Preferences.PREF_LOGIN,Context.MODE_PRIVATE);
            SharedPreferences.Editor edit= prefs1.edit();
            edit.putBoolean(Preferences.AUTO_LOGIN,val);
            edit.apply();
        });

        prefs=getActivity().getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
        toggleNotifyLessonStart.setChecked(prefs.getBoolean("notifyLesson",false));
        toggleNotifyLessonStart.setOnCheckedChangeListener((compoundButton, val) -> {
            SharedPreferences prefs1 =getActivity().getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
            SharedPreferences.Editor edit= prefs1.edit();
            edit.putBoolean("notifyLesson",val);
            edit.apply();
        });

        toggleResetUsers.setChecked(prefs.getBoolean(Preferences.RESET_USERS,false));
        toggleResetUsers.setOnCheckedChangeListener((compoundButton, val) -> {
            SharedPreferences prefs1 =getActivity().getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
            SharedPreferences.Editor edit= prefs1.edit();
            edit.putBoolean(Preferences.RESET_USERS,val);
            edit.apply();
        });

        txtLocation=view.findViewById(R.id.txt_location);
        Location location=MainActivity.getHomeLocation(getActivity());
        if(location!=null) {
            txtLocation.setText(getLocationToString(location));
        }
        /** Start Google Place Picker on Click **/
        Button btnPicker= view.findViewById(R.id.btn_picker);
        btnPicker.setOnClickListener(view1 -> {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Location homeLocation=MainActivity.getHomeLocation(getActivity());
            if(homeLocation!=null) {
                double longitude = homeLocation.getLongitude();
                double latitude = homeLocation.getLatitude();
                //if(longitude!=0&&latitude!=0) {
                LatLngBounds locationB = new LatLngBounds(
                        new LatLng(latitude, longitude), new LatLng(latitude, longitude));
                builder.setLatLngBounds(locationB);
                //}
            }
            try {
                startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        });
        getActivity().setTitle(getString(R.string.options));
    }

    private String getLocationToString(Location location) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> locationAdresses = null;
        try {
            locationAdresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 2);
            Address locationAddress;
            if (locationAdresses != null && locationAdresses.size() > 0) {
                locationAddress = locationAdresses.get(0);
                String address = locationAddress.getAddressLine(0);;
                return address;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Option", "logLocation: Could not find location");
        }
        return null;
    }

    /** Result Handler for Home Location Place Picker **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(getActivity(), data);
                Log.d("Options", "onActivityResult: Home Location set to: "+place.getName());
                Toast.makeText(context, "Home Location set to: "+place.getAddress(), Toast.LENGTH_SHORT).show();
                SharedPreferences prefs =getActivity().getSharedPreferences(Preferences.PREF_OTHER,Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=prefs.edit();
                /** Save Location in prefs as String (no putDouble available) **/
                editor.putString(Preferences.HOME_LATITUDE,""+place.getLatLng().latitude);
                editor.putString(Preferences.HOME_LONGITUDE,""+place.getLatLng().longitude);
                editor.apply();
                Location location=new Location("");
                location.setLongitude(place.getLatLng().longitude);
                location.setLatitude(place.getLatLng().latitude);
                txtLocation.setText(getLocationToString(location));
            }
            else{
                Log.d("Options", "onActivityResult: Home Location could not be set");
                Toast.makeText(context, "Home Location could not be set", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
