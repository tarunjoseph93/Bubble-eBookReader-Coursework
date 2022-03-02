package com.tj.bubble_ebookreader_coursework;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityBookDetailPageBinding;

public class BookDetailPage extends AppCompatActivity {

    private ActivityBookDetailPageBinding bind;
    String bookId, bookTitle, bookUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityBookDetailPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        bind.bookDownloadButton.setVisibility(View.GONE);

        bookDetailLoad();

        MyApplication.viewCountIncrement(bookId);

        bind.bookOpenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(BookDetailPage.this, PdfViewPage.class);
                intent1.putExtra("bookId", bookId);
                startActivity(intent1);
            }
        });

        bind.bookDownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(BookDetailPage.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    MyApplication.bookDownload(BookDetailPage.this, "" + bookId, "" + bookTitle, "" + bookUrl);
                }
                else {
                    reqPermLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }
        });

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private ActivityResultLauncher<String> reqPermLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted-> {
        if(isGranted) {
            MyApplication.bookDownload(this, "" + bookId, "" + bookTitle, "" + bookUrl);
        }
        else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
        }
    });

    private void bookDetailLoad() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        bookTitle  = "" + snapshot.child("title").getValue();
                        String desc = "" + snapshot.child("description").getValue();
                        String catId = "" + snapshot.child("categoryId").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        bookUrl = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String date = MyApplication.timestampFormat(Long.parseLong(timestamp));

                        bind.bookDownloadButton.setVisibility(View.VISIBLE);

                        MyApplication.catLoad("" + catId, bind.catTextView);
                        MyApplication.pdfFromUrlSinglePageLoad("" + bookUrl, "" + bookTitle, bind.viewPdf, bind.progBarPdf);
                        MyApplication.pdfSizeLoad("" + bookUrl, "" + bookTitle, bind.sizeTextView);

                        bind.titleText.setText(bookTitle);
                        bind.bookDetailedDescription.setText(desc);
                        bind.viewTextView.setText(viewsCount.replace("null","N/A"));
                        bind.downloadCountTextView.setText(downloadsCount.replace("null","N/A"));
                        bind.dateTextView.setText(date);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}