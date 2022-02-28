package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        fireAuth = FirebaseAuth.getInstance();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                userCheck();
            }
        }, 2000);
    }

    private void userCheck() {
        FirebaseUser fireUser = fireAuth.getCurrentUser();
        if(fireUser == null) {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finish();
        }
        else {
            DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Accounts");
            dRef.child(fireUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String uType = ""+snapshot.child("userType").getValue();
                            if(uType.equals("user")){
                                startActivity(new Intent(SplashScreen.this, UserDashboard.class));
                                finish();
                            }
                            else if(uType.equals("administrator")) {
                                startActivity(new Intent(SplashScreen.this, AdminDashboard.class));
                                finish();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
        }
    }
}