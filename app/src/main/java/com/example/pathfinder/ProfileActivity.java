package com.example.pathfinder;

import static java.security.AccessController.getContext;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pathfinder.databinding.ActivityProfileBinding;
import com.example.pathfinder.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {

    private ActivityProfileBinding binding;

    ArrayList<Marker> markers = new ArrayList<Marker>();
    RecyclerView recyclerView;
    StateAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

//        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        UserInfoFetch();

        StateAdapter.OnStateClickListener stateClickListener = new StateAdapter.OnStateClickListener() {
            @Override
            public void onStateClick(Marker marker, int position) {

                Intent in=new Intent(getApplicationContext(),MainActivity.class);
                in.putExtra("Marker",marker);
                startActivity(in);
            }
        };


        recyclerView = findViewById(R.id.upointlist);
        setInitialData();
        adapter = new StateAdapter(this, markers, stateClickListener);

        recyclerView.setAdapter(adapter);

        FrameLayout profileframe = findViewById(R.id.profileframe);
        profileframe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });
    }
    private void UserInfoFetch(){
        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String username = snapshot.child("username").getValue().toString();
                        String profileImage = snapshot.child("thumbnail").getValue().toString();
                        String joindate = snapshot.child("joindate").getValue().toString();

                        TextView usernameTextView = findViewById(R.id.username_profile);
                        TextView joindateTextView = findViewById(R.id.joindate_textview);
                        usernameTextView.setText(username);
                        joindateTextView.setText("Member since " + joindate);

                        de.hdodenhof.circleimageview.CircleImageView profileThumbnail = findViewById(R.id.profile_view);

                        if (!profileImage.isEmpty()) {
                            Glide.with(ProfileActivity.this).load(profileImage).into(profileThumbnail);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void setInitialData() {
//        Marker marker=new Marker("name","description",2,2,"ownerid");
//        markers.add(marker);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("markers")
                .whereEqualTo("ownerid", FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String name =document.getString("name");
                        String description=document.getString("description");
                        int maxlenght = 30;
                        if (description.length() >= maxlenght) {
                            description = description.substring(0, maxlenght) + "..";
                        }
                        Double latitude=document.getDouble("latitude");
                        Double longitude =document.getDouble("longitude");
                        String  ownerid=document.getString("ownerid");
                        Marker marker =new Marker(name,description,latitude,longitude,ownerid);
                        markers.add(marker);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void showEditDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.edit_profile);

        Button reset_pass_btn = dialog.findViewById(R.id.resetpassword);
        reset_pass_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String email = snapshot.child("email").getValue().toString();
                                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(ProfileActivity.this, "Check your E-mail", Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                }
                                            }
                                        });

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

        Button confirmbtn = dialog.findViewById(R.id.confirmchanges);
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText usernameET = dialog.findViewById(R.id.editUsername);
                EditText thumbnailET = dialog.findViewById(R.id.editThumbnail);
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                DatabaseReference userRef = db.child("Users").child(FirebaseAuth.getInstance().getUid());
                if (!usernameET.getText().toString().isEmpty())
                {
                    userRef.child("username").setValue(usernameET.getText().toString());
                    Toast.makeText(ProfileActivity.this, "Username changed!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());
                }
                if (!thumbnailET.getText().toString().isEmpty()) {
                    userRef.child("thumbnail").setValue(thumbnailET.getText().toString());
                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);
    }
}