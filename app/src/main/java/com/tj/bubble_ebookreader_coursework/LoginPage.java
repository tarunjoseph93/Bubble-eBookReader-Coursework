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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityLoginPageBinding;

public class LoginPage extends AppCompatActivity {

    private ActivityLoginPageBinding bind;
    private FirebaseAuth fireAuth;
    private ProgressDialog proDia;
    private String uEmail = "", uPword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityLoginPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        fireAuth = FirebaseAuth.getInstance();

        proDia = new ProgressDialog(this);
        proDia.setTitle("Gotta wait sometime, buddy!");
        proDia.setCanceledOnTouchOutside(false);

        bind.newUserRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginPage.this, RegistrationPage.class));
            }
        });

        bind.lgnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userValidation();
            }
        });
    }

    private void userValidation() {
        uEmail = bind.emailLogin.getText().toString().trim();
        uPword = bind.passwordLogin.getText().toString().trim();

        if(!Patterns.EMAIL_ADDRESS.matcher(uEmail).matches()) {
            Toast.makeText(this, "Your Email ID pattern is invalid! Please try again!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(uPword)) {
            Toast.makeText(this, "Password field is empty. Please enter a password!", Toast.LENGTH_SHORT).show();
        }
        else {
            userLogin();
        }
    }

    private void userLogin() {
        proDia.setMessage("Logging you into your account!");
        proDia.show();
        fireAuth.signInWithEmailAndPassword(uEmail,uPword)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        userOrAdminCheck();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(LoginPage.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void userOrAdminCheck() {
        FirebaseUser fireUser = fireAuth.getCurrentUser();
        proDia.setMessage("Checking the user type!");
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Accounts");
        dRef.child(fireUser.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        proDia.dismiss();
                        String uType = ""+snapshot.child("userType").getValue();
                        if(uType.equals("user")){
                            startActivity(new Intent(LoginPage.this, UserDashboard.class));
                            finish();
                        }
                        else if(uType.equals("administrator")) {
                            startActivity(new Intent(LoginPage.this, AdminDashboard.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}