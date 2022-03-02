package com.tj.bubble_ebookreader_coursework.pages;

import androidx.activity.result.ActivityResultLauncher;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.MyApplication;
import com.tj.bubble_ebookreader_coursework.R;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityBookDetailPageBinding;

public class BookDetailPage extends AppCompatActivity {

    private ActivityBookDetailPageBinding bind;
    String bookId, bookTitle, bookUrl;
    boolean isFavourite = false;
    FirebaseAuth fireAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityBookDetailPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        bind.bookDownloadButton.setVisibility(View.GONE);

        fireAuth = FirebaseAuth.getInstance();
        if(fireAuth.getCurrentUser() != null) {
            isFavouriteCheck();
        }

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

        bind.bookFavouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fireAuth.getCurrentUser() == null) {
                    Toast.makeText(BookDetailPage.this, "You are not logged in! Please login to favourite this book!", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(isFavourite) {
                        MyApplication.removeFromFavourite(BookDetailPage.this, bookId);
                    }
                    else {
                        MyApplication.addToFavourite(BookDetailPage.this, bookId);
                    }
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

    private void isFavouriteCheck() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Accounts");
        dRef.child(fireAuth.getUid()).child("Favourites").child(bookId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        isFavourite = snapshot.exists();
                        if(isFavourite) {
                            bind.bookFavouriteButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_baseline_favorite_unselected_white, 0, 0);
                            bind.bookFavouriteButton.setText(R.string.fav_remove);
                        }
                        else {
                            bind.bookFavouriteButton.setCompoundDrawablesRelativeWithIntrinsicBounds(0, R.drawable.ic_baseline_favorite_selected_white, 0, 0);
                            bind.bookFavouriteButton.setText(R.string.fav);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

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
                        MyApplication.pdfFromUrlSinglePageLoad("" + bookUrl, "" + bookTitle, bind.viewPdf, bind.progBarPdf, bind.pageCountTextView);
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