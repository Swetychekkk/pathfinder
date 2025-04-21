package com.example.pathfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pathfinder.databinding.ActivityBrowseBinding;
import com.example.pathfinder.databinding.ActivityRegisterBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yandex.mapkit.Image;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;

public class BrowseActivity extends AppCompatActivity {

    private ActivityBrowseBinding binding;

    ArrayList<Marker> markers = new ArrayList<Marker>();
    RecyclerView recyclerView;
    StateAdapter adapter;

    private boolean searchfield_state = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_browse);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
       // binding = ActivityBrowseBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_browse);

        //setContentView(R.layout.activity_main);
        StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {
            @Override
            public void onStateClick(Marker marker, int position) {

               Intent in=new Intent(getApplicationContext(),MainActivity.class);
               in.putExtra("Marker",marker);
               startActivity(in);
               finish();
            }
        };


        recyclerView = findViewById(R.id.pointlist);
        setInitialData();
        adapter = new StateAdapter(this, markers, stateClickListener);

        recyclerView.setAdapter(adapter);

        ImageButton searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!searchfield_state) {
                    findViewById(R.id.textPrev).setVisibility(View.INVISIBLE);
                    findViewById(R.id.closebtn).setVisibility(View.GONE);
                    findViewById(R.id.searchField).setVisibility(View.VISIBLE);
                    searchfield_state = true;
                } else {
                    findViewById(R.id.textPrev).setVisibility(View.VISIBLE);
                    findViewById(R.id.closebtn).setVisibility(View.VISIBLE);
                    findViewById(R.id.searchField).setVisibility(View.GONE);
                    searchfield_state = false;
                }
            }
        });

        ImageButton closebtn = findViewById(R.id.closebtn);
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BrowseActivity.this, MainActivity.class));
                finishAfterTransition();
            }
        });
    }

    private void setInitialData() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("markers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                   String name =document.getString("name");
                                   String description=document.getString("description");
                                   int maxlenght = 30;
                                   if (description.length() >= maxlenght) {
                                       description = description.substring(0, maxlenght) + "..";
                                   }
                                   Double latitude = document.getDouble("latitude");
                                   Double longitude = document.getDouble("longitude");
                                   String ownerid = document.getString("ownerid");
                                   String priority = document.getString("priority");
                                   Marker marker=new Marker(name,description,latitude,longitude,ownerid, priority);
                                   markers.add(marker);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}