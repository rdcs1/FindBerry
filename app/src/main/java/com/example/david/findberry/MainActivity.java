package com.example.david.findberry;

import android.*;
import android.Manifest;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    TabLayout tabLayout;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String name,email,id;
    Double latitude=0.0,longitude = 0.0;
    String dname;
    AlertDialog.Builder builder;
    AlertDialog dialog;
    int flag2 = 0;
    Fragment fragment = new OrdersFragment();

    final int icons[] = new int[]{
            R.drawable.ic_home_black_24dp,
            R.drawable.ic_directions_run_black_24dp,
            R.drawable.ic_shopping_cart_black_24dp,
            R.drawable.ic_place_black_24dp,
            R.drawable.ic_settings_black_24dp
    };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            name = user.getDisplayName();
            email = user.getEmail();
            id = user.getUid();
        } else {
            logOut();
        }

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        tabLayout.getTabAt(0).setIcon(icons[0]);
        tabLayout.getTabAt(1).setIcon(icons[1]);
        tabLayout.getTabAt(2).setIcon(icons[2]);
        tabLayout.getTabAt(3).setIcon(icons[3]);
        tabLayout.getTabAt(4).setIcon(icons[4]);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String desc="";
                switch(tabLayout.getSelectedTabPosition()){
                    case 0:
                        desc = getString(R.string.selectdesc);
                        break;
                    case 1:
                        desc = getString(R.string.deliverydesc);
                        break;
                    case 2:
                        desc = getString(R.string.ordersdesc);
                        break;
                    case 3:
                        desc = getString(R.string.mapdesc);
                        break;
                    case 4:
                        desc = getString(R.string.configdesc);
                        break;
                }
                Snackbar.make(view,desc,Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child(id).exists()){
                    databaseReference.child(id).child("name").setValue(name);
                    databaseReference.child(id).child("mail").setValue(email);
                    databaseReference.child(id).child("score").setValue("5");
                    databaseReference.child(id).child("nscore").setValue("1");
                }else{
                    if(!dataSnapshot.child(id).child("dname").exists()){
                        builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setMessage("Hemos detectado que no eres deliverer, para usar la ventana de deliverer debes registrarte, ¿deseas registrarte como deliverer?");
                        builder.setCancelable(true);
                        ;
                        builder.setPositiveButton(
                                "Sí",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Toast.makeText(getApplicationContext(),R.string.tyforDel,Toast.LENGTH_LONG).show();
                                        Log.d("passwd",PasswordGenerator.getUsername());
                                        dname = PasswordGenerator.getUsername();
                                        int flag = 1;
                                        while(flag == 1){
                                            flag = 0;
                                            dname = PasswordGenerator.getUsername();
                                            for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                                                if(snapshot.child("dname").exists()){
                                                    if(snapshot.child("dname").getValue().toString().equals(dname)){
                                                        flag=1;
                                                    }
                                                }
                                            }
                                        }
                                        databaseReference.child(id).child("dname").setValue(dname);
                                        databaseReference.child(id).child("dscore").setValue("5");
                                        databaseReference.child(id).child("dnscore").setValue("1");
                                        mViewPager.setCurrentItem(1);
                                    }
                                });

                        builder.setNegativeButton(
                                "No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        dialog = builder.create();
                        dialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }

    }


    private class SectionsPagerAdapter extends FragmentPagerAdapter {


        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            final Fragment result;
            Bundle bundle = new Bundle();
            bundle.putDouble("lat",latitude);
            bundle.putDouble("lng",longitude);
            bundle.putString("id",id);
            switch(position){
                case 0:
                    result =  new RequestFragment();
                    break;
                case 1:
                    result = new DeliveryFragment();
                    break;
                case 2:
                    result = new  OrdersFragment();
                    result.setArguments(bundle);
                    break;
                case 3:
                    result = new MapFragment();
                    result.setArguments(bundle);
                    break;
                case 4:
                    result = new SettingsFragment();
                    break;
                default: result = null;
            }
            return result;

        }

        @Override
        public int getCount() {
            return 5;
        }

    }

    private void logOut() {
        Intent intent = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Localizacion Local = new Localizacion();
        Local.setMainActivity(this);
        final boolean gpsEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, Local);
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, Local);

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationStart();
            }
        }
    }

    /* Aqui empieza la Clase Localizacion */
    private class Localizacion implements LocationListener {
        MainActivity mainActivity;

        public MainActivity getMainActivity() {
            return mainActivity;
        }

        void setMainActivity(MainActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion

            latitude=loc.getLatitude();
            longitude=loc.getLongitude();
            databaseReference.child(id).child("lat").setValue(loc.getLatitude());
            databaseReference.child(id).child("lng").setValue(loc.getLongitude());


        }

        @Override
        public void onProviderDisabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es desactivado
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este metodo se ejecuta cuando el GPS es activado
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }
    }
}
