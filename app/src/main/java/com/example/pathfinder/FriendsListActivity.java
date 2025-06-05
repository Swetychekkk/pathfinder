package com.example.pathfinder;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class FriendsListActivity extends AppCompatActivity {
    String profileUID;
    ArrayList<User> users = new ArrayList<User>();
    RecyclerView recyclerView;
    UsersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_friends_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //PARSE UID FROM ACTIVITIES
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("USER_UID")) {
            profileUID = intent.getStringExtra("USER_UID");

            Log.d("TargetActivity", "Received UID: " + profileUID);
        }

        ImageButton backButton = findViewById(R.id.returnButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        DatabaseReference friendsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(profileUID);
        friendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView ownersname = findViewById(R.id.ownername);
                ownersname.setText(snapshot.child("username").getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        UsersAdapter.OnUserClickListener userClickListener = new UsersAdapter.OnUserClickListener() {
            @Override
            public void onUserClick(User user, int position) {
                Intent intent = new Intent(FriendsListActivity.this, ProfileActivity.class);
                intent.putExtra("USER_UID", FirebaseAuth.getInstance().getUid().toString());
                startActivity(intent);
            }
        };


        recyclerView = findViewById(R.id.ufriendlist);
        setInitialData();

        adapter = new UsersAdapter(this, users, userClickListener);

        recyclerView.setAdapter(adapter);
    }
    private void setInitialData() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(profileUID).child("friends");

        Query acceptedQuery = ref.orderByChild("status").equalTo("accepted");

        acceptedQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot friendSnapshot : dataSnapshot.getChildren()) {
                    String UID = friendSnapshot.getKey();
                    Log.i("GGG", UID);

                    User user = new User(UID, "name", "telegramID");
                    users.add(user);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}