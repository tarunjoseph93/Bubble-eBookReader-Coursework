package com.tj.bubble_ebookreader_coursework.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityPdfAddPageBinding;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityPdfEditPageBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditPage extends AppCompatActivity {

    private ActivityPdfEditPageBinding bind;
    private String bookId, catIdSelected = "", catTitleSelected = "", title = "", desc = "";
    private ProgressDialog proDia;
    private ArrayList<String> catTitleList, catIdList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityPdfEditPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        bookId = getIntent().getStringExtra("bookId");
        proDia = new ProgressDialog(this);
        proDia.setTitle("Gotta wait...");
        proDia.setCanceledOnTouchOutside(false);

        catLoad();
        bookInformationLoad();

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bind.catOptionsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                catDia();
            }
        });

        bind.editPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
            }
        });
    }

    private void validation() {
        title = bind.titleText.getText().toString().trim();
        desc = bind.descText.getText().toString().trim();
        if(TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Title field is empty!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(desc)) {
            Toast.makeText(this, "Description field is empty!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(catIdSelected)) {
            Toast.makeText(this, "Choose a category!", Toast.LENGTH_SHORT).show();
        }
        else {
            editPdf();
        }
    }

    private void editPdf() {
        proDia.setMessage("Editing your book");
        proDia.show();
        HashMap<String,Object> hMap = new HashMap<>();
        hMap.put("title", "" + title);
        hMap.put("description", "" + desc);
        hMap.put("categoryId", "" + catIdSelected);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.child(bookId).updateChildren(hMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        proDia.dismiss();
                        Toast.makeText(PdfEditPage.this, "PDF has been edited successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(PdfEditPage.this, "PDF could not be edited! Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bookInformationLoad() {
        DatabaseReference dRefBooks = FirebaseDatabase.getInstance().getReference("Books");
        dRefBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        catIdSelected = "" + snapshot.child("categoryId").getValue();
                        String desc = "" + snapshot.child("description").getValue();
                        String title = "" + snapshot.child("title").getValue();
                        bind.titleText.setText(title);
                        bind.descText.setText(desc);

                        DatabaseReference dRefBookCat = FirebaseDatabase.getInstance().getReference("Categories");
                        dRefBookCat.child(catIdSelected)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        String cat = "" + snapshot.child("category").getValue();
                                        bind.catOptionsText.setText(cat);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void catDia() {
        String[] catArray = new String[catTitleList.size()];
        for(int i = 0; i < catTitleList.size(); i++) {
            catArray[i] = catTitleList.get(i);
        }

        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Choose your category")
                .setItems(catArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        catIdSelected = catIdList.get(i);
                        catTitleSelected = catTitleList.get(i);
                        bind.catOptionsText.setText(catTitleSelected);
                    }
                }).show();
    }

    private void catLoad() {
        catIdList = new ArrayList<>();
        catTitleList = new ArrayList<>();

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catIdList.clear();
                catTitleList.clear();
                for(DataSnapshot dSnap : snapshot.getChildren()) {
                    String id = "" + dSnap.child("id").getValue();
                    catIdList.add(id);
                    String cat = "" + dSnap.child("category").getValue();
                    catTitleList.add(cat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}