package com.tj.bubble_ebookreader_coursework;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityBookDetailPageBinding;

public class BookDetailPage extends AppCompatActivity {

    private ActivityBookDetailPageBinding bind;
    String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityBookDetailPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        bookDetailLoad();

        MyApplication.viewCountIncrement(bookId);

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void bookDetailLoad() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String title = "" + snapshot.child("title").getValue();
                        String desc = "" + snapshot.child("description").getValue();
                        String catId = "" + snapshot.child("categoryId").getValue();
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        String downloadsCount = "" + snapshot.child("downloadsCount").getValue();
                        String url = "" + snapshot.child("url").getValue();
                        String timestamp = "" + snapshot.child("timestamp").getValue();
                        String date = MyApplication.timestampFormat(Long.parseLong(timestamp));

                        MyApplication.catLoad("" + catId, bind.catTextView);
                        MyApplication.pdfFromUrlSinglePageLoad("" + url, "" + title, bind.viewPdf, bind.progBarPdf);
                        MyApplication.pdfSizeLoad("" + url, "" + title, bind.sizeTextView);

                        bind.titleText.setText(title);
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