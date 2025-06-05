package com.example.pathfinder;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.pathfinder.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");

        binding.regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.emailRegEt.getText().toString().isEmpty() || binding.passwordRegEt.getText().toString().isEmpty() //check if fields not null
                        || binding.rpasswordRegEt.getText().toString().isEmpty() || binding.usernameRegEt.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Some of important filds cannot be empty", Toast.LENGTH_SHORT).show();
                    binding.usernameRegEt.setHintTextColor(Color.parseColor("#ce3867"));
                    binding.passwordRegEt.setHintTextColor(Color.parseColor("#ce3867"));
                    binding.rpasswordRegEt.setHintTextColor(Color.parseColor("#ce3867"));
                    binding.emailRegEt.setHintTextColor(Color.parseColor("#ce3867"));
                } else if (!(binding.rpasswordRegEt.getText().toString().equals(binding.passwordRegEt.getText().toString()))) { //if password repeated not correctly
                    Toast.makeText(getApplicationContext(), "Repeat password compare error", Toast.LENGTH_SHORT).show();
                    binding.passwordRegEt.setTextColor(Color.parseColor("#ce3867"));
                    binding.rpasswordRegEt.setTextColor(Color.parseColor("#ce3867"));
                } else if (!(binding.passwordRegEt.getText().toString().length() >= 6)) { //if password not satisfied FireBase security requirements
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 symbol", Toast.LENGTH_SHORT).show();
                    binding.passwordRegEt.setTextColor(Color.parseColor("#ce3867"));
                } else if (binding.checkBox.isChecked()) { //check if checkbox activated
                    Query usernameQuery = userRef.orderByChild("username").equalTo(binding.usernameRegEt.getText().toString());

                    usernameQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailRegEt.getText().toString(), binding.passwordRegEt.getText().toString())
                                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if (task.isSuccessful()) {
                                                    HashMap<String, Object> userInfo = new HashMap<>();
                                                    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);
                                                    userInfo.put("email", binding.emailRegEt.getText().toString());
                                                    userInfo.put("username", binding.usernameRegEt.getText().toString());
                                                    userInfo.put("thumbnail", "");
                                                    userInfo.put("joindate", sdf.format(new Date()));
                                                    userInfo.put("telegram", "");

//                                                    Map<String, Object> friendsMap = new HashMap<>();
//                                                    Map<String, Object> placeholderFriend = new HashMap<>();
//                                                    placeholderFriend.put("status", "accepted");
//                                                    friendsMap.put("YbqiV8zMb5YJKf7aMiYtEdtkn4l1", placeholderFriend);
//
//                                                    userInfo.put("friends", friendsMap); // вложенная карта
                                                    FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                            .setValue(userInfo);
                                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                                    finish();
                                                } else if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                    Toast.makeText(getApplicationContext(), "User with this E-mail already exists", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            } else { Toast.makeText(getApplicationContext(), "User with selected username already exists", Toast.LENGTH_SHORT).show(); }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                } else { //if checkbox not active
                    binding.checkBox.setTextColor(Color.parseColor("#ce3867"));
                    binding.passwordRegEt.setTextColor(Color.parseColor("#BDB0D8"));
                    binding.rpasswordRegEt.setTextColor(Color.parseColor("#BDB0D8"));
                    Toast.makeText(getApplicationContext(), "Agree our Term of Service", Toast.LENGTH_SHORT).show();
                }
            }
        });

        binding.loginLinkAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}