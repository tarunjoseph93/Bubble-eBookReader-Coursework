package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityPdfAddPageBinding;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddPage extends AppCompatActivity {

    private ActivityPdfAddPageBinding bind;
    private FirebaseAuth fireAuth;
    private static final String PDF_TAG = "PDF_TAG_ADD";
    private static final int PDF_CODE_PICK = 1000;
    private Uri customPdfUri = null;
    private ArrayList<String> catTitleList, catIdList;
    private String titleText = "", descText = "";
    private String catIdSelected, catTitleSelected;
    private ProgressDialog proDia;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityPdfAddPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        fireAuth = FirebaseAuth.getInstance();
        proDia = new ProgressDialog(this);
        proDia.setTitle("Gotta wait buddy...");
        proDia.setCanceledOnTouchOutside(false);
        loadCategoryOptions();
        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        bind.attachButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickPdfIntent();
            }
        });

        bind.catOptionsText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickCatOptions();
            }
        });

        bind.uploadPdfButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validation();
            }
        });
    }

    private void validation() {
        titleText = bind.titleText.getText().toString().trim();
        descText = bind.descText.getText().toString().trim();

        if(TextUtils.isEmpty(titleText)) {
            Toast.makeText(this, "Title field is empty!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(descText)) {
            Toast.makeText(this, "Description Field is empty!", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(catTitleSelected)) {
            Toast.makeText(this, "Category Field is empty!", Toast.LENGTH_SHORT).show();
        }
        else if(customPdfUri == null) {
            Toast.makeText(this, "No PDF has been attached. Please attach a PDF!", Toast.LENGTH_SHORT).show();
        }
        else {
            sendPdfToStore();
        }
    }

    private void sendPdfToStore() {
        proDia.setMessage("Sending your PDF to the Database!");
        proDia.show();
        long timestamp = System.currentTimeMillis();
        String pathAndNameOfFile = "Books/" + timestamp;
        StorageReference sRef = FirebaseStorage.getInstance().getReference(pathAndNameOfFile);
        sRef.putFile(customPdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> customUriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while(!customUriTask.isSuccessful());
                        String uploadResult = "" + customUriTask.getResult();
                        sendPdfToDB(uploadResult,timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(PdfAddPage.this, "PDF was not uploaded. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendPdfToDB(String uploadResult, long timestamp) {
        proDia.setMessage("Sending the PDF information to the Database!");
        String uid = fireAuth.getUid();
        HashMap<String,Object> hMap = new HashMap<>();
        hMap.put("uid", "" + uid);
        hMap.put("id", "" + timestamp);
        hMap.put("title", "" + titleText);
        hMap.put("description", "" + descText);
        hMap.put("categoryId", "" + catIdSelected);
        hMap.put("url", "" + uploadResult);
        hMap.put("timestamp", timestamp);

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.child("" + timestamp).setValue(hMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        proDia.dismiss();
                        Toast.makeText(PdfAddPage.this, "Your PDF has been uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(PdfAddPage.this, "PDF upload error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadCategoryOptions() {
        Log.d(PDF_TAG, "loadingCategories: Loading all categories!");
        catTitleList = new ArrayList<>();
        catIdList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catTitleList.clear();
                catIdList.clear();
                for(DataSnapshot dSnap : snapshot.getChildren()) {
                    String catId = "" + dSnap.child("id").getValue();
                    String catTitleText = "" + dSnap.child("category").getValue();
                    catIdList.add(catId);
                    catTitleList.add(catTitleText);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void pickCatOptions() {
        Log.d(PDF_TAG,"Picking Options List: display of Category Options List");
        String[] catArray = new String[catTitleList.size()];
        for(int i = 0; i < catTitleList.size(); i++) {
            catArray[i] = catTitleList.get(i);
        }
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setTitle("Choose your category: ").setItems(catArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                catIdSelected = catIdList.get(i);
                catTitleSelected = catTitleList.get(i);
                bind.catOptionsText.setText(catTitleSelected);
                Log.d(PDF_TAG,"Category Picked: " + catIdSelected + " " + catTitleSelected);
            }
        }).show();
    }

    private void pickPdfIntent() {
        Log.d(PDF_TAG, "Pick PDF Intent: Beginning the PDF intent picking...");
        Intent pick = new Intent();
        pick.setType("application/pdf");
        pick.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(pick,"Select the appropriate PDF"),PDF_CODE_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == PDF_CODE_PICK) {
                Log.d(PDF_TAG, "Activity Result: PDF has been picked!");
                customPdfUri = data.getData();
                Log.d(PDF_TAG, "Activity Result: URI: "+customPdfUri);
            }
        }
        else {
            Log.d(PDF_TAG, "Activity Result: PDF picking disbanded!");
            Toast.makeText(this, "PDF picking disbanded!", Toast.LENGTH_SHORT).show();
        }
    }
}