package com.tj.bubble_ebookreader_coursework.pages;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityUserDashboardBinding;

public class UserDashboard extends AppCompatActivity {

    private ActivityUserDashboardBinding bind;
    private FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        fireAuth = FirebaseAuth.getInstance();
        userCheck();

        bind.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireAuth.signOut();
                userCheck();
            }
        });
    }

    private void userCheck() {
        FirebaseUser fireUser = fireAuth.getCurrentUser();
        if(fireUser == null) {
            startActivity(new Intent(UserDashboard.this, MainActivity.class));
            finish();
        }
        else {
            String uEmail = fireUser.getEmail();
            bind.userEmailHead.setText(uEmail);
        }
    }
}