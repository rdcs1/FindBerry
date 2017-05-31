package com.example.david.findberry;

import android.*;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    String flag,data,uid,uname,pricel,titlel,ulugar,descrip;
    TextView title,price;
    AlertDialog.Builder builder1;
    AlertDialog alert11;
    Double precio;
    Double lat,lng;
    String pedido;
    EditText desc,elugar;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    List<ProductItems> productItemsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        title = (TextView)findViewById(R.id.tvTitle);
        price = (TextView)findViewById(R.id.tvPriced);
        desc = (EditText) findViewById(R.id.etProddesc);
        elugar = (EditText)findViewById(R.id.etElocation);
        Bundle bundle = getIntent().getExtras();
        flag = bundle.getString("op");
        data = bundle.getString("data");
        titlel = bundle.getString("title");
        pricel = bundle.getString("price");
        title.setText(titlel);
        String pr = "$"+pricel;
        price.setText(pr);
        descrip = "No agregado.";
        ulugar = "No agregado.";

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uname = user.getDisplayName();
            uid = user.getUid();
        }

        desc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                descrip = s.toString();
                if(s.toString().equals("")){
                    descrip = "No agregado.";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        elugar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ulugar = s.toString();
                if(s.toString().equals("")){
                    ulugar = "No agregado.";
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("sides").child(data).child(flag).child("products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                productItemsList.clear();
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    productItemsList.add(new ProductItems(postSnapshot.child("nombre").getValue().toString(),postSnapshot.child("precio").getValue().toString(),"0"));
                }
                recyclerView = (RecyclerView)findViewById(R.id.paRecycler);
                recyclerView.setHasFixedSize(true);

                layoutManager = new LinearLayoutManager(getApplicationContext());
                recyclerView.setLayoutManager(layoutManager);

                adapter = new ProductItemsAdapter(productItemsList);
                recyclerView.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void pushdata(){
        databaseReference = firebaseDatabase.getReference("orders");
        pedido = PasswordGenerator.getPassword(PasswordGenerator.MINUSCULAS+PasswordGenerator.MAYUSCULAS+PasswordGenerator.NUMEROS,16);
        precio = Double.parseDouble(pricel);
        for(int i = 0;i<productItemsList.size();i++){
            precio += Double.parseDouble(productItemsList.get(i).getPrice())*Double.parseDouble(productItemsList.get(i).getQuantity());
        }
        if(precio != Double.parseDouble(pricel)){

            builder1 = new AlertDialog.Builder(ProductActivity.this);
            builder1.setMessage("El precio total será de "+precio.toString()+"$ ¿Estás seguro de aceptar el pedido?");
            builder1.setCancelable(true);
            ;
            builder1.setPositiveButton(
                    "Aceptar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            String pass;
                            for(int i = 0; i<productItemsList.size(); i++){
                                if(!productItemsList.get(i).getQuantity().equals("0")){
                                    pass= PasswordGenerator.getPassword(PasswordGenerator.MINUSCULAS+PasswordGenerator.MAYUSCULAS+PasswordGenerator.NUMEROS,16);
                                    databaseReference.child(pedido).child("list").child(pass).child("producto").setValue(productItemsList.get(i).getProduct());
                                    databaseReference.child(pedido).child("list").child(pass).child("precio").setValue(productItemsList.get(i).getPrice());
                                    databaseReference.child(pedido).child("list").child(pass).child("cantidad").setValue(productItemsList.get(i).getQuantity());
                                }
                            }
                            pass= "z"+PasswordGenerator.getPassword(PasswordGenerator.MINUSCULAS+PasswordGenerator.MAYUSCULAS+PasswordGenerator.NUMEROS,15);
                            databaseReference.child(pedido).child("list").child(pass).child("producto").setValue("Domicilio");
                            databaseReference.child(pedido).child("list").child(pass).child("precio").setValue(pricel);
                            databaseReference.child(pedido).child("list").child(pass).child("cantidad").setValue("1");
                            databaseReference.child(pedido).child("precio").setValue(precio);
                            databaseReference.child(pedido).child("uid").setValue(uid);
                            databaseReference.child(pedido).child("uname").setValue(uname);
                            databaseReference.child(pedido).child("lugar").setValue(titlel);
                            databaseReference.child(pedido).child("hora").setValue(Calendar.getInstance().getTimeInMillis()/60000);
                            databaseReference.child(pedido).child("estado").setValue(0);
                            databaseReference.child(pedido).child("ulugar").setValue(ulugar);
                            databaseReference.child(pedido).child("desc").setValue(descrip);
                            databaseReference.child(pedido).child("ulat").setValue(lat);
                            databaseReference.child(pedido).child("ulng").setValue(lng);

                            Toast.makeText(getApplicationContext(),"Pedido aceptado",Toast.LENGTH_SHORT).show();
                            finish();
                            dialog.cancel();
                        }
                    });

            builder1.setNegativeButton(
                    "Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            alert11 = builder1.create();
            alert11.show();
        }else {
            Toast.makeText(getApplicationContext(),"No ha seleccionado ningún producto.",Toast.LENGTH_SHORT).show();
        }


        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ProductActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        } else {
            locationStart();
        }

    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.opFAccept:
                pushdata();
                break;
            case R.id.opFCancel:
                finish();
                break;
        }
    }


    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ProductActivity.Localizacion Local = new ProductActivity.Localizacion();
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
        ProductActivity mainActivity;

        public ProductActivity getMainActivity() {
            return mainActivity;
        }

        void setMainActivity(ProductActivity mainActivity) {
            this.mainActivity = mainActivity;
        }

        @Override
        public void onLocationChanged(Location loc) {
            // Este metodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la deteccion de un cambio de ubicacion
            lat = loc.getLatitude();
            lng = loc.getLongitude();
            String Text = "Mi ubicacion actual es: " + "\n Lat = "
                    + loc.getLatitude() + "\n Long = " + loc.getLongitude();
            this.mainActivity.setLocation(loc);
            //Toast.makeText(getApplicationContext(),Text,Toast.LENGTH_LONG).show();
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

    public void setLocation(Location loc) {
        if (loc.getLatitude() != 0.0 && loc.getLongitude() != 0.0) {
            try {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> list = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                if (!list.isEmpty()) {
                    Address address = list.get(0);
                    //Toast.makeText(getApplicationContext(),"Mi dirección es: \n" + address.getAddressLine(0),Toast.LENGTH_SHORT).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
