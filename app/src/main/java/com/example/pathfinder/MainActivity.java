package com.example.pathfinder;

import android.Manifest;
import android.app.Dialog;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    private static boolean isMapKitInit = false;    //TOGGLE MAPKIT INITIALISATION

    private static boolean isBulderModEnabled = false;

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
                if (isBulderModEnabled) {
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
//                                    placemark.setText(userInput.getText().toString());
//                                    placemark.useCompositeIcon();
                                    CompositeIcon compositeIcon = placemark.useCompositeIcon();
                                    compositeIcon.setIcon("pin_upper", ImageProvider.fromResource(MainActivity.this, R.drawable.pin), new IconStyle()
                                            .setScale(0.4f)
                                            .setAnchor(new PointF(0.50f, 0.9f)));
                                    compositeIcon.setIcon("point", ImageProvider.fromResource(MainActivity.this, R.drawable.point), new IconStyle()
                                            .setScale(0.4f)
                                            .setFlat(true)
                                            .setAnchor(new PointF(0.5f, 0.5f)));
                                    Toast.makeText(getApplicationContext(), userInput.getText().toString(), Toast.LENGTH_SHORT).show();
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
                isBulderModEnabled = !isBulderModEnabled;
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