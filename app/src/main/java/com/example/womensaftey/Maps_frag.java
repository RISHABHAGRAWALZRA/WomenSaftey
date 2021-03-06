package com.example.womensaftey;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.content.ContentValues.TAG;


public class Maps_frag extends Fragment implements OnMapReadyCallback {


    SupportMapFragment supportMapFragment;
    GoogleMap mMap;
    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;

    FloatingActionButton fab;

    String userID = null;
    public static final String MY_PREFS_FILENAME = "com.example.womensaftey.myfile";

    Maps_frag(Context context) {
        this.context = context;
    }

    Maps_frag(Context context, String Uid) {
        this.context = context;
        userID = Uid;
    }

    final LatLng[] userlocation = new LatLng[1];


    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: loc Changed");
            userlocation[0] = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
        }
    };


    @Override
    public void onStart() {
        super.onStart();

        if(userID==null){
            SharedPreferences preferences = context.getSharedPreferences(MY_PREFS_FILENAME, Context.MODE_PRIVATE);
            userID = preferences.getString("uid", null);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (userID != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference mref = database.getReference("message");
            mref.child(userID).child("canCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean canCheck = dataSnapshot.getValue(Boolean.class);
                    if (canCheck == false) {
                        userID = null;
                        SharedPreferences.Editor editor = context.
                                getSharedPreferences(MY_PREFS_FILENAME, Context.MODE_PRIVATE).edit();
                        editor.putString("uid", userID);
                        editor.commit();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    userID = null;
                }
            });
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.maps_frag_layout, container, false);

        fab = view.findViewById(R.id.fab);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {

                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                userlocation[0] = new LatLng(location.getLatitude(), location.getLongitude());

                                supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                                if (supportMapFragment == null) {
                                    FragmentManager fm = getFragmentManager();
                                    FragmentTransaction ft = fm.beginTransaction();
                                    supportMapFragment = SupportMapFragment.newInstance();
                                    ft.replace(R.id.map, supportMapFragment).commit();
                                }
                                supportMapFragment.getMapAsync(Maps_frag.this);
                            }
                        }
                    });

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }

        //end of mapfrag


        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = context.
                getSharedPreferences(MY_PREFS_FILENAME, Context.MODE_PRIVATE).edit();
        editor.putString("uid", userID);
        editor.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        //mMap.getUiSettings().setCompassEnabled(true);
        //mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);


        //mMap.addMarker(new MarkerOptions().position(userlocation[0]).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userlocation[0],15));


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userlocation[0], 10));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userlocation[0], 15));
            }
        });

        if (userID != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference mref = database.getReference("message");


            mref.child(userID).child("canCheck").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Boolean canCheck = dataSnapshot.getValue(Boolean.class);
                    if (canCheck == false) {
                        userID = null;
                        mMap.clear();
                        SharedPreferences.Editor editor = context.
                                getSharedPreferences(MY_PREFS_FILENAME, Context.MODE_PRIVATE).edit();
                        editor.putString("uid", userID);
                        editor.commit();

                    } else {

                        mref.child(userID).child("latLng").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null) {
                                    LatLng destination = new LatLng(dataSnapshot.child("latitude").getValue(Double.class), dataSnapshot.child("longitude").getValue(Double.class));
                                    mMap.clear();
                                    mMap.addMarker(new MarkerOptions().position(destination).title("Your Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

//                                    new GetPathFromLocation(userlocation[0], destination, new DirectionPointListener() {
//                                        @Override
//                                        public void onPath(PolylineOptions polyLine) {
//                                            mMap.addPolyline(polyLine);
//                                        }
//                                    }).execute();

                                    Log.d(TAG, "onDataChange: UserID:" + userID + "  \n" + destination.latitude + "  " + destination.longitude);
                                } else {
                                    Log.d(TAG, "onDataChange: datasnapshot is null");
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    userID = null;
                }
            });
        }
    }


    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context, "Thanks! Permission Granted", Toast.LENGTH_SHORT).show();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                        dialog.setMessage("This Permission is important, Please permit it!")
                                .setTitle("Important permission Denied!");

                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                            }
                        });
                    }
                } else {
                    Toast.makeText(context, "We will never show this to you again", Toast.LENGTH_SHORT).show();
                }
        }
    }


}


