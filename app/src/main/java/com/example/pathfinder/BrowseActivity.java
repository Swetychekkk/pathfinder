package com.example.pathfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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

    ProgressBar progressBar;

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

        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.pointlist);
        setInitialData(null);

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

        LinearLayout dropdownMenu = findViewById(R.id.dropdownMenu);
        TextView textPrev = findViewById(R.id.textPrev);
        RecyclerView pointlst = findViewById(R.id.pointlist);

        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.dropdown_slide_in);
        Animation slideOut = AnimationUtils.loadAnimation(this, R.anim.dropdown_slide_out);

        textPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dropdownMenu.getVisibility() == View.GONE && recyclerView.getVisibility() == View.VISIBLE) {
                    dropdownMenu.setVisibility(View.VISIBLE);
                    dropdownMenu.startAnimation(slideIn);
                    recyclerView.startAnimation(slideOut);
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            recyclerView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                } else {
                    dropdownMenu.startAnimation(slideOut);
                    slideOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            dropdownMenu.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                }
            }
        });

        EditText searchField = findViewById(R.id.searchField);
        searchField.setOnEditorActionListener(((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {

                String searchData = searchField.getText().toString().trim();
                markers.clear();
                progressBar.setVisibility(View.VISIBLE);
                setInitialData(searchData);
//                progressBar.setVisibility(View.INVISIBLE);
                return true;
            }
            return false;
        }));

        ImageButton closebtn = findViewById(R.id.closebtn);
        closebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(BrowseActivity.this, MainActivity.class));
                finishAfterTransition();
            }
        });
    }

    private void setInitialData(String searchData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("markers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                                   String name =document.getString("name");
                                   String description=document.getString("description");

                                    int maxLength = 30;
                                    if (description != null && description.length() >= maxLength) {
                                        description = description.substring(0, maxLength) + "..";
                                    }

                                    Double latitude = document.getDouble("latitude");
                                    Double longitude = document.getDouble("longitude");
                                    String ownerid = document.getString("ownerid");
                                    String priority = document.getString("priority");

                                    Marker marker = new Marker(name, description, latitude, longitude, ownerid, priority);

                                    if (searchData != null && name.toLowerCase().contains(searchData.toLowerCase())) {
                                        markers.add(marker);
                                        progressBar.setVisibility(View.GONE);
                                    } else if (searchData != null) {
                                        Toast.makeText(getApplicationContext(), "Its empty", Toast.LENGTH_SHORT).show();
                                    } else {
                                        markers.add(marker);
                                        progressBar.setVisibility(View.GONE);
                                    }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}