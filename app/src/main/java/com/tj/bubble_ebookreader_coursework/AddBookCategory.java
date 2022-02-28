package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityAddBookCategoryBinding;

import java.util.HashMap;

public class AddBookCategory extends AppCompatActivity {

    private ActivityAddBookCategoryBinding bind;
    private String cat = "";
    private FirebaseAuth fireAuth;
    private ProgressDialog proDia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityAddBookCategoryBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        fireAuth = FirebaseAuth.getInstance();
        proDia = new ProgressDialog(this);
        proDia.setTitle("Wait up...");
        proDia.setCanceledOnTouchOutside(false);

        bind.addCatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catValidation();
            }
        });

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void catValidation() {
        cat = bind.catText.getText().toString().trim();
        if(TextUtils.isEmpty(cat)) {
            Toast.makeText(this, "The text field is empty. Please enter a category!", Toast.LENGTH_SHORT).show();
        }
        else {
            addCatIntoDb();
        }
    }

    private void addCatIntoDb() {
        proDia.setMessage("Adding your category now!");
        proDia.show();
        long timestamp = System.currentTimeMillis();
        HashMap<String,Object> hmap = new HashMap<>();
        hmap.put("id", ""+timestamp);
        hmap.put("category", ""+cat);
        hmap.put("timestamp", timestamp);
        hmap.put("uid", ""+fireAuth.getUid());

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.child(""+timestamp).setValue(hmap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        proDia.dismiss();
                        Toast.makeText(AddBookCategory.this, "Your category has been added successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(AddBookCategory.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}