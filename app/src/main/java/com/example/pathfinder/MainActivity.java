package com.example.pathfinder;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.Calendar;
import java.util.HashMap;
import android.util.Log;


public class MainActivity extends AppCompatActivity {
    private static boolean isMapKitInit = false;    //TOGGLE MAPKIT INITIALISATION

    private static boolean isBuilderModEnabled = false;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    //GLOBAL COORDINATES VALUES
    private double longitude = 36.215984f;
    private double latitude = 51.740429f;
    private MapView mapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //REDIRECT USER TO AUTH SCREEN IF NOT AUTHORISED
        if (FirebaseAuth.getInstance().getCurrentUser()==null){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
        UserInfoFetch();

        //Yandex MapKitSDK Initialisation
        if (!isMapKitInit) {
            MapKitFactory.setApiKey("00f001ab-bb4f-423d-9a55-c527e58d412d");
            MapKitFactory.initialize(this);
            isMapKitInit = true;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //CHECK PERMISSIONS
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }

        mapView = findViewById(R.id.mapview); //MAPVIEW
        mapView.getMap().move(new CameraPosition(new Point(latitude, longitude),17.0f, 150.0f, 0.0f));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Markers.load(MainActivity.this, mapView, getApplicationContext(), latitude, longitude);

        //SET MAP POSITION TO USER (ON "Find ME" BUTTON CLICK)
        View btn = findViewById(R.id.button_find_me);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapView.getMap().move(new CameraPosition(new Point(latitude, longitude),17.0f, 150.0f, 0.0f));
            }
        });
        View btn_profile = findViewById(R.id.button_profile);
        btn_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            }
        });


        InputListener inputListener = new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) {
                if (isBuilderModEnabled) {
                    View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.user_input, null);
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertBuilder.setView(view);
                    final EditText userInput = (EditText) view.findViewById(R.id.userinput);

                    alertBuilder.setCancelable(true)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    var placemark = mapView.getMap().getMapObjects().addPlacemark();
                                    placemark.setGeometry(point);

                                    Markers.decorate(MainActivity.this, placemark, userInput.getText().toString(), 1);

                                    Markers.Push(userInput.getText().toString(),"Lorem ipsum", FirebaseAuth.getInstance().getCurrentUser().getUid(), point.getLatitude(), point.getLongitude());
                                }
                            });
                    Dialog dialog = alertBuilder.create();
                    dialog.show();
                }
            }

            @Override
            public void onMapLongTap(@NonNull Map map, @NonNull Point point) {

            }
        };

        mapView.getMap().addInputListener(inputListener);

        ImageButton builderModToggle = (ImageButton) findViewById(R.id.button_add);
        builderModToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isBuilderModEnabled = !isBuilderModEnabled;
                if (isBuilderModEnabled) {
                    builderModToggle.setRotation(45);
                } else {
                    builderModToggle.setRotation(0);
                }
            }
        });
    }

    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    public void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    private void UserInfoFetch(){
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = snapshot.child("thumbnail").getValue().toString();

                        de.hdodenhof.circleimageview.CircleImageView profileThumbnail = findViewById(R.id.button_profile);

                        if (!profileImage.isEmpty()) {
                            Glide.with(MainActivity.this).load(profileImage).into(profileThumbnail);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void getLastKnownLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Location location = task.getResult();
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                mapView.getMap().move(new CameraPosition(new Point(latitude, longitude),17.0f, 150.0f, 0.0f));
//                                mapView.getMap().set2DMode(true);
                                if (8 >= Calendar.getInstance().get(Calendar.HOUR_OF_DAY) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 20) {
                                    mapView.getMap().setNightModeEnabled(true);
                                }
                                var placemark = mapView.getMap().getMapObjects().addPlacemark();
                                placemark.setGeometry(new Point(latitude, longitude));
                                placemark.setIcon(ImageProvider.fromResource(MainActivity.this, R.drawable.point));
                                placemark.setIconStyle(
                                        new IconStyle()
                                                .setScale(0.5f)
                                                .setAnchor(new PointF(0.5f, 1.0f))
                                                .setFlat(true)
                                                );
                                // Используйте полученные координаты по необходимости
                            } else {
                                //ex
                            }
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                // Разрешение не предоставлено, обработайте этот случай
            }
        }
    }
}

class Markers {
    public static void decorate(Activity MainActivity, PlacemarkMapObject placemark, String name, Integer priority) {
        CompositeIcon compositeIcon = placemark.useCompositeIcon();
        compositeIcon.setIcon("pin_upper", ImageProvider.fromResource(MainActivity, R.drawable.pin), new IconStyle()
                .setScale(0.4f)
                .setAnchor(new PointF(0.50f, 0.9f)));
        compositeIcon.setIcon("point", ImageProvider.fromResource(MainActivity, R.drawable.point), new IconStyle()
                .setScale(0.4f)
                .setFlat(true)
                .setAnchor(new PointF(0.5f, 0.5f)));
        placemark.setText(name);
    }
    public static void Push(String name, String description, String ownerid, double lat, double lng) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        HashMap<String, Object> marker = new HashMap<>();
        marker.put("name", name);
        marker.put("description", description);
        marker.put("ownerid", ownerid);
        marker.put("latitude", lat);
        marker.put("longitude", lng);

        db.collection("markers")
                .add(marker)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Метка добавлена: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Ошибка добавления", e));
    }
    public static void load(Activity MainActivity, MapView mapView, Context context, Double userLat, Double userLon) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        double delta = 0.4f;
        double minLat = userLat - delta;
        double maxLat = userLat + delta;
        double minLon = userLon - delta;
        double maxLon = userLon + delta;

        db.collection("markers")
                .whereGreaterThanOrEqualTo("latitude", minLat)
                .whereLessThanOrEqualTo("latitude", maxLat)
                .whereGreaterThanOrEqualTo("longitude", minLon)
                .whereLessThanOrEqualTo("longitude", maxLon)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Log.d("Firestore", "Данные получены: " + queryDocumentSnapshots.size());

                        double latitude = document.getDouble("latitude");
                        double longitude = document.getDouble("longitude");
                        String name = document.getString("name");
                        PlacemarkMapObject placemark = mapView.getMap().getMapObjects().addPlacemark(new Point(latitude, longitude));
                        decorate(MainActivity, placemark, name, 1);
                    }
                })
                .addOnFailureListener(e -> Log.w("Firestore", "Ошибка получения данных", e));
    }
}