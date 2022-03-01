package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.adapters.Book_Admin_Adapter;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityPdfListAdminPageBinding;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;

import java.util.ArrayList;

public class PdfListAdminPage extends AppCompatActivity {

    private ActivityPdfListAdminPageBinding bind;
    private ArrayList<Pdf_Model> pdfList;
    private Book_Admin_Adapter adapBookAdmin;
    private String catId, catTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityPdfListAdminPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        catId = intent.getStringExtra("categoryId");
        catTitle = intent.getStringExtra("categoryTitle");

        bind.bookSubTitleHead.setText(catTitle);
        pdfListLoad();

        bind.bookSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapBookAdmin.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void pdfListLoad() {
        pdfList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.orderByChild("categoryId").equalTo(catId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfList.clear();
                        for(DataSnapshot dSnap : snapshot.getChildren()) {
                            Pdf_Model mod = dSnap.getValue(Pdf_Model.class);
                            pdfList.add(mod);
                        }
                        adapBookAdmin = new Book_Admin_Adapter(PdfListAdminPage.this, pdfList);
                        bind.bookListAdmin.setAdapter(adapBookAdmin);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}