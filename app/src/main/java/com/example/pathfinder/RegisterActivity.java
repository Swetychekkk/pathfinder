package com.example.pathfinder;

import android.content.Intent;
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
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

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

        binding.regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.emailRegEt.getText().toString().isEmpty() || binding.passwordRegEt.getText().toString().isEmpty()
                        || binding.rpasswordRegEt.getText().toString().isEmpty() || binding.usernameRegEt.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Some of important filds cannot be empty", Toast.LENGTH_SHORT).show();
                } else if (!(binding.rpasswordRegEt.getText().toString().equals(binding.passwordRegEt.getText().toString()))) {
                    Toast.makeText(getApplicationContext(), "Repeat password compare error", Toast.LENGTH_SHORT).show();
                } else if (!(binding.passwordRegEt.getText().toString().length() >= 6)) {
                    Toast.makeText(getApplicationContext(), "Password must be at least 6 symbol", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.emailRegEt.getText().toString(), binding.passwordRegEt.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        HashMap<String, String> userInfo = new HashMap<>();
                                        userInfo.put("email", binding.emailRegEt.getText().toString());
                                        userInfo.put("username", binding.usernameRegEt.getText().toString());
                                        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(userInfo);
                                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    }
                                }
                            });
                }
            }
        });
    }
}