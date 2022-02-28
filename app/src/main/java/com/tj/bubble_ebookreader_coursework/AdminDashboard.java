package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityAdminDashboardBinding;

import java.util.ArrayList;

public class AdminDashboard extends AppCompatActivity {

    private ActivityAdminDashboardBinding bind;
    private FirebaseAuth fireAuth;
    private ArrayList<Category_Model> catList;
    private Category_Adapter catAdap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        fireAuth = FirebaseAuth.getInstance();
        userCheck();
        loadAllCategories();

        bind.catSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                catAdap.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        bind.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireAuth.signOut();
                userCheck();
            }
        });

        bind.catAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, AddBookCategory.class));
            }
        });

        bind.catPdfAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AdminDashboard.this, PdfAddPage.class));
            }
        });
    }

    private void loadAllCategories() {
        catList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catList.clear();
                for(DataSnapshot dSnap : snapshot.getChildren()) {
                    Category_Model catMod = dSnap.getValue(Category_Model.class);
                    catList.add(catMod);
                }
                catAdap = new Category_Adapter(AdminDashboard.this, catList);
                bind.catRecyclerView.setAdapter(catAdap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void userCheck() {
        FirebaseUser fireUser = fireAuth.getCurrentUser();
        if(fireUser == null) {
            startActivity(new Intent(AdminDashboard.this, MainActivity.class));
            finish();
        }
        else {
            String uEmail = fireUser.getEmail();
            bind.adminEmailHead.setText(uEmail);
        }
    }
}