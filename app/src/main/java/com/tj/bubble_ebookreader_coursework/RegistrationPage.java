package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityRegistrationPageBinding;

import java.util.HashMap;

public class RegistrationPage extends AppCompatActivity {

    private ActivityRegistrationPageBinding bind;
    private FirebaseAuth fireAuth;
    private String uname = "", email = "", pword = "";
    private ProgressDialog proDia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityRegistrationPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        fireAuth = FirebaseAuth.getInstance();

        proDia = new ProgressDialog(this);
        proDia.setTitle("Gotta wait sometime, buddy!");
        proDia.setCanceledOnTouchOutside(false);

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bind.registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
            }
        });
    }

    private void validation() {
        uname = bind.nameRegister.getText().toString().trim();
        email = bind.emailRegister.getText().toString().trim();
        pword = bind.passwordRegister.getText().toString().trim();
        String cPword = bind.confirmPasswordRegister.getText().toString().trim();

        if(TextUtils.isEmpty(uname)) {
            Toast.makeText(this, "Name field is empty. Please enter your name!", Toast.LENGTH_SHORT).show();
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Your Email ID pattern is invalid! Please try again!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(pword)) {
            Toast.makeText(this, "Password field is empty. Please enter a password!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(cPword)) {
            Toast.makeText(this, "Confirm Password field is empty. Please confirm your entered password!", Toast.LENGTH_SHORT).show();
        }
        else if(!pword.equals(cPword)) {
            Toast.makeText(this, "Passwords do not match. Please enter your passwords again!", Toast.LENGTH_SHORT).show();
        }
        else {
            userProfileCreation();
        }
    }

    private void userProfileCreation() {
        proDia.setMessage("Your account is being created! Hold on...");
        proDia.show();

        fireAuth.createUserWithEmailAndPassword(email,pword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                profileUpdation();
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RegistrationPage.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void profileUpdation() {
        proDia.setMessage("Your profile is being saved. Almost there!");
        long timestamp = System.currentTimeMillis();
        String userID = fireAuth.getUid();
        HashMap<String,Object> hmap = new HashMap<>();

        hmap.put("uid", userID);
        hmap.put("email", email);
        hmap.put("name", uname);
        hmap.put("userImage", "");
        hmap.put("userType", "user");
        hmap.put("timestamp", timestamp);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Accounts");
        dRef.child(userID).setValue(hmap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        proDia.dismiss();
                        Toast.makeText(RegistrationPage.this, "Your account has been created! Please login now.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegistrationPage.this, LoginPage.class));
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(RegistrationPage.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}