package com.example.pathfinder;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.logo.Alignment;
import com.yandex.mapkit.logo.HorizontalAlignment;
import com.yandex.mapkit.logo.VerticalAlignment;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.CompositeIcon;
import com.yandex.mapkit.map.IconStyle;
import com.yandex.mapkit.map.InputListener;
import com.yandex.mapkit.map.Map;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.TextStyle;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.image.ImageProvider;

import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static boolean isMapKitInit = false;
    private static boolean isBuilderModEnabled = false;

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private double longitude = 36.215984f;
    private double latitude = 51.740429f;
    private MapView mapView;
    Bundle object;
    Marker marker;

    private final MapObjectTapListener placemarkTapListener = Markers.getTapListener(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }

        if (!isMapKitInit) {
            MapKitFactory.setApiKey("00f001ab-bb4f-423d-9a55-c527e58d412d");
            MapKitFactory.initialize(this);
            isMapKitInit = true;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastKnownLocation();
        }

        mapView = findViewById(R.id.mapview);
        mapView.getMap().getLogo().setAlignment(new Alignment(HorizontalAlignment.LEFT, VerticalAlignment.TOP));
        object =getIntent().getExtras();
        if (object!=null){
            marker= (Marker) object.getSerializable("Marker");
            mapView.getMap().move(new CameraPosition(new Point(0, 0),17.0f, 150.0f, 0.0f));
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View btn_browse = findViewById(R.id.browse_btn);
        btn_browse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BrowseActivity.class));
                finish();
            }
        });

        View btn_profile = findViewById(R.id.button_profile);


        btn_profile.setOnClickListener(new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            startActivity(new Intent(MainActivity.this, ProfileActivity.class));
        }
        });

        findViewById(R.id.button_find_me).setOnClickListener(view -> {
            mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), 17.0f, 150.0f, 0.0f));
            Markers.load(MainActivity.this, mapView, getApplicationContext(), latitude, longitude, placemarkTapListener);
        });

//        mapView.getMap().addCameraListener((map, cameraPosition, cameraUpdateSource, finished) -> {
//            if (finished) {
//                Point center = cameraPosition.getTarget();
//                Markers.load(MainActivity.this, mapView, getApplicationContext(), center.getLatitude(), center.getLongitude(), placemarkTapListener);
//            }
//        });

        mapView.getMap().addInputListener(new InputListener() {
            @Override
            public void onMapTap(@NonNull Map map, @NonNull Point point) {
                if (isBuilderModEnabled) {
                    Dialog dialog = new Dialog(MainActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.user_input);

                    EditText userInput = dialog.findViewById(R.id.userinput);
                    EditText desc = dialog.findViewById(R.id.descriptioninput);
                    Button confirm = dialog.findViewById(R.id.confirmbtn);

                    confirm.setOnClickListener(v -> {
                        if (!userInput.getText().toString().isEmpty() && !desc.getText().toString().isEmpty()) {
                            Markers.Push(userInput.getText().toString(), desc.getText().toString(),
                                    FirebaseAuth.getInstance().getCurrentUser().getUid(),
                                    point.getLatitude(), point.getLongitude());
                            dialog.dismiss();
                            Markers.load(MainActivity.this, mapView, getApplicationContext(), latitude, longitude, placemarkTapListener);
                        }
                    });

                    dialog.show();
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    dialog.getWindow().setGravity(Gravity.BOTTOM);
                }
            }

            @Override
            public void onMapLongTap(@NonNull Map map, @NonNull Point point) {}
        });

        ImageButton builderModToggle = findViewById(R.id.button_add);
        builderModToggle.setOnClickListener(view -> {
            isBuilderModEnabled = !isBuilderModEnabled;
            builderModToggle.setRotation(isBuilderModEnabled ? 45 : 0);
        });

        UserInfoFetch();
    }

    private void getLastKnownLocation() {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location location = task.getResult();
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            mapView.getMap().move(new CameraPosition(new Point(latitude, longitude), 17.0f, 150.0f, 0.0f));
                            Markers.load(MainActivity.this, mapView, getApplicationContext(), latitude, longitude, placemarkTapListener);
                            if (object!=null){
                                marker= (Marker) object.getSerializable("Marker");
                                mapView.getMap().move(new CameraPosition(marker.getCords(),17.0f, 150.0f, 0.0f));
                                double delta = 0.2f;
                                double minLat = latitude - delta;
                                double maxLat = latitude + delta;
                                double minLon = longitude - delta;
                                double maxLon = longitude + delta;
                                if ((!(marker.getCords().getLatitude() >= minLat && marker.getCords().getLatitude() <= maxLat) || !(marker.getCords().getLongitude() >= minLon && marker.getCords().getLongitude() <= maxLon))) {
                                    Toast.makeText(getApplicationContext(), "New point loaded successfully", Toast.LENGTH_SHORT).show();
                                    Markers.load(MainActivity.this, mapView, getApplicationContext(), marker.getCords().getLatitude(), marker.getCords().getLongitude(), placemarkTapListener);
                                }
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
                        }
                    });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void UserInfoFetch() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String profileImage = snapshot.child("thumbnail").getValue(String.class);
                        CircleImageView profileThumbnail = findViewById(R.id.button_profile);
                        if (profileImage != null && !profileImage.isEmpty()) {
                            Glide.with(MainActivity.this).load(profileImage).into(profileThumbnail);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }


    @Override
    public void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }


}

class Markers {

    public static void decorate(Activity activity, PlacemarkMapObject placemark, String name) {
        CompositeIcon compositeIcon = placemark.useCompositeIcon();
        compositeIcon.setIcon("pin_upper", ImageProvider.fromResource(activity, R.drawable.pin), new IconStyle()
                .setScale(0.4f)
                .setAnchor(new PointF(0.5f, 0.9f)));
        compositeIcon.setIcon("point", ImageProvider.fromResource(activity, R.drawable.point), new IconStyle()
                .setScale(0.4f)
                .setFlat(true)
                .setAnchor(new PointF(0.5f, 0.5f)));

        placemark.setText(name, new TextStyle()
                .setColor(Color.WHITE)
                .setOutlineColor(Color.parseColor("#3D1C80"))
                .setOutlineWidth(3));
    }

    public static void Push(String name, String description, String ownerid, double lat, double lng) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        java.util.Map<String, Object> marker = new HashMap<>();
        marker.put("name", name);
        marker.put("description", description);
        marker.put("ownerid", ownerid);
        marker.put("latitude", lat);
        marker.put("longitude", lng);

        db.collection("markers")
                .add(marker)
                .addOnSuccessListener(documentReference -> {})
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public static void load(Activity activity, MapView mapView, Context context, Double userLat, Double userLon, MapObjectTapListener tapListener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        MapObjectCollection mapObjects = mapView.getMap().getMapObjects();
        mapObjects.clear();

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
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot document : querySnapshots) {
                        double lat = document.getDouble("latitude");
                        double lon = document.getDouble("longitude");
                        String name = document.getString("name");

                        PlacemarkMapObject placemark = mapObjects.addPlacemark(new Point(lat, lon));
                        decorate(activity, placemark, name);

                        // Передаём статический обработчик (один и тот же для всех меток)
                        placemark.addTapListener(tapListener);

                        // Можно также сохранить мета-данные в placemark.setUserData(...) при необходимости
                        placemark.setUserData(document.getData());
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public static MapObjectTapListener getTapListener(Activity activity) {
        return new MapObjectTapListener() {
            private Dialog currentDialog;

            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                if (!(mapObject instanceof PlacemarkMapObject)) return false;

                java.util.Map<String, Object> data = (java.util.Map<String, Object>) mapObject.getUserData();
                if (data == null) return false;

                String name = (String) data.get("name");
                String description = (String) data.get("description");
                String ownerid = (String) data.get("ownerid");

                if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();

                currentDialog = new Dialog(activity);
                currentDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                currentDialog.setContentView(R.layout.popup_info);

                Window window = currentDialog.getWindow();
                if (window != null) {
                    window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    window.setWindowAnimations(R.style.DialogAnimation);
                    window.setGravity(Gravity.BOTTOM);
                }

                TextView popupPointname = currentDialog.findViewById(R.id.pointname_popup);
                TextView descriptionPopup = currentDialog.findViewById(R.id.description_popup);
                TextView usernamePopup = currentDialog.findViewById(R.id.username_popup);
                ImageButton closebtn = currentDialog.findViewById(R.id.closebtn);
                CircleImageView profileThumbnail = currentDialog.findViewById(R.id.profileview_popup);

                popupPointname.setText(name);
                descriptionPopup.setText(description != null && description.length() > 300 ? description.substring(0, 300) + "..." : description);

                closebtn.setOnClickListener(view -> currentDialog.dismiss());

                FirebaseDatabase.getInstance().getReference().child("Users").child(ownerid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String username = "by " + snapshot.child("username").getValue(String.class);
                                String profileImage = snapshot.child("thumbnail").getValue(String.class);

                                usernamePopup.setText(username);

                                if (profileImage != null && !profileImage.isEmpty()) {
                                    Glide.with(activity).load(profileImage).into(profileThumbnail);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {}
                        });

                currentDialog.show();
                return true;
            }
        };
    }
}
