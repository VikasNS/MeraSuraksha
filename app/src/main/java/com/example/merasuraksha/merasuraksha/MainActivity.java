package com.example.merasuraksha.merasuraksha;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.TotalCaptureResult;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.Permissions;
import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.provider.UserDictionary.Words.APP_ID;

import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;


public class MainActivity extends AppCompatActivity {


    FirebaseDatabase database;

    DatabaseReference update_location_reference;
    DatabaseReference Ambulance_reference;
    DatabaseReference Police_reference;

    DatabaseReference Medical_Emergency_reference;
    DatabaseReference Police_Emergency_reference;

    ChildEventListener childEventListener;

    String availability_status = "yes";
    String android_id;

    ImageButton phone_call_button;
    ImageButton open_google_maps_button;
    ImageButton finish_button;

    LinearLayout emergency_linear_layout;

    TextView emergency_user_name;

    RadioButton service_radio_button_police;
    RadioButton service_radio_button_ambulance;
    RadioButton availability_radio_button_yes;
    RadioButton availability_radio_button_no;
    RadioButton availability_radio_button_in_service;

    CardView hospital_card_view;

    User user;

    TextView hospital_name;

    public double agent_latitude;
    public double agent_longitude;

    final int PERMISSION_ACCESS_COARSE_LOCATION = 2;

    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 100;
    private static final float LOCATION_DISTANCE = 10f;

    LocationListener locationListener;

    double hospital_lat;
    double hospital_long;

    ImageButton hospital_direction_button;

    ImageButton finish_hospital_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);


        //startService(new Intent(this, MyService.class));
        //GPSTracker gpsTracker=new GPSTracker(getApplicationContext());
        // Toast.makeText(getApplicationContext(),gpsTracker.location.getLatitude()+" "+gpsTracker.location.getLongitude(),Toast.LENGTH_SHORT).show();


        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        database = FirebaseDatabase.getInstance();

        Ambulance_reference = database.getReference("Ambulance").child(android_id);
        Police_reference = database.getReference("Police").child(android_id);

        Medical_Emergency_reference = database.getReference("Medical_Emergency");
        Police_Emergency_reference = database.getReference("Police_Emergency");


        phone_call_button = findViewById(R.id.phone_call_button);
        open_google_maps_button = findViewById(R.id.open_google_maps_button);
        finish_button = findViewById(R.id.finish_button);


        emergency_linear_layout = findViewById(R.id.emergency_linear_layout);
        emergency_user_name = findViewById(R.id.emergency_user_name);

        service_radio_button_police = findViewById(R.id.service_radio_button_police);
        service_radio_button_ambulance = findViewById(R.id.service_radio_button_ambulance);

        availability_radio_button_yes = findViewById(R.id.availability_radio_button_yes);
        availability_radio_button_no = findViewById(R.id.availability_radio_button_no);
        availability_radio_button_in_service = findViewById(R.id.availability_radio_button_in_service);

        hospital_card_view=findViewById(R.id.hospital_card_view);
        hospital_name=findViewById(R.id.hospital_name);
        hospital_direction_button=findViewById(R.id.get_hospital_direction_button);

        finish_hospital_button=findViewById(R.id.finish_hospital);

        finish_hospital_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update_location_reference.child("hospital").removeValue();
                hospital_card_view.setVisibility(View.GONE);
            }
        });

        hospital_direction_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent map_intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?daddr=" + hospital_lat + "," + hospital_long));
                startActivity(map_intent);
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                agent_latitude = location.getLatitude();
                agent_longitude = location.getLongitude();
                if (update_location_reference != null)
                    update_location();
                //Toast.makeText(getApplicationContext(),location.getLatitude()+" "+location.getLongitude(),Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }else
        {
            initializeLocationManager();
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0,
                    locationListener);
        }



        View.OnClickListener img_btn_click_listner = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.phone_call_button:
                        Intent dialer_intent = new Intent(Intent.ACTION_DIAL);
                        dialer_intent.setData(Uri.parse("tel:" + user.phone_no));
                        startActivity(dialer_intent);
                        break;
                    case R.id.open_google_maps_button:
                        Intent map_intent = new Intent(android.content.Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?daddr=" + user.latitude + "," + user.longitude));
                        startActivity(map_intent);
                        break;
                    case R.id.finish_button:
                        update_location_reference.child("emergency").removeValue();
                        emergency_linear_layout.setVisibility(View.GONE);
                        radio_button_yes_clicked(v);
                        break;
                }
            }
        };

        phone_call_button.setOnClickListener(img_btn_click_listner);
        open_google_maps_button.setOnClickListener(img_btn_click_listner);
        finish_button.setOnClickListener(img_btn_click_listner);



        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.getKey().equals("emergency")) {
                    emergency_linear_layout.setVisibility(View.VISIBLE);
                    user = dataSnapshot.getValue(User.class);
                }

                if (dataSnapshot.getKey().equals("hospital"))
                {
                    hospital_card_view.setVisibility(View.VISIBLE);
                    hospital_name.setText("Sharawathy Hospital");
                    hospital_lat=12.9623;
                    hospital_long=77.5342;
                }

            }

            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    initializeLocationManager();
                    mLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 0, 0,
                            locationListener);
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }

    public void radio_button_yes_clicked(View view)
    {
        availability_radio_button_yes.setChecked(true);
        availability_radio_button_no.setChecked(false);
        availability_radio_button_in_service.setChecked(false);
        availability_status="yes";
        update_location();
    }

    public void radio_button_no_clicked(View view)
    {
        availability_radio_button_no.setChecked(true);
        availability_radio_button_yes.setChecked(false);
        availability_radio_button_in_service.setChecked(false);
        availability_status="no";
        update_location();
    }

    public void radio_button_inservice_clicked(View view)
    {
        availability_radio_button_in_service.setChecked(true);
        availability_radio_button_yes.setChecked(false);
        availability_radio_button_no.setChecked(false);
        availability_status="in_service";
        update_location();
    }

    public void radio_click_ambulance_clicked(View view)
    {
        // remove_event_listner();
        Ambulance_reference.addChildEventListener(childEventListener);
        Police_reference.removeValue();
        update_location_reference=Ambulance_reference;
        service_radio_button_police.setChecked(false);
        radio_button_yes_clicked(view);
    }

    public void radio_click_police_clicked(View view)
    {
        //remove_event_listner();
        Police_reference.addChildEventListener(childEventListener);
        Ambulance_reference.removeValue();
        update_location_reference=Police_reference;
        service_radio_button_ambulance.setChecked(false);
        radio_button_yes_clicked(view);
    }

    public void remove_event_listner()
    {
        Police_reference.removeEventListener(childEventListener);
        Ambulance_reference.removeEventListener(childEventListener);
    }
    public void update_location()
    {
        Agent agent=new Agent(agent_latitude, agent_longitude,availability_status);
        update_location_reference.child("details").setValue(agent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

}


class User {
    public User() {}
    public double latitude;
    public double longitude;
    public String phone_no;
    public User(double latitude,double longitude,String phone_no)
    {
        this.latitude=latitude;
        this.longitude=longitude;
        this.phone_no=phone_no;
    }

}

class Agent {
    public double latitude;
    public double longitude;
    public String isavailable;

    public Agent(double latitude,double longitude,String availability_status)
    {
        this.latitude=latitude;
        this.longitude=longitude;
        this.isavailable=availability_status;
    }
}