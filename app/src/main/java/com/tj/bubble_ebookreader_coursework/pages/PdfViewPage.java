package com.tj.bubble_ebookreader_coursework.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.tj.bubble_ebookreader_coursework.Constants;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityPdfViewPageBinding;

public class PdfViewPage extends AppCompatActivity {

    private ActivityPdfViewPageBinding bind;
    private String bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityPdfViewPageBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        bookDetailsLoad();

        bind.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void bookDetailsLoad() {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String pdfUrl = "" + snapshot.child("url").getValue();
                        bookFromUrlLoad(pdfUrl);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void bookFromUrlLoad(String pdfUrl) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        sRef.getBytes(Constants.PDF_MAX_BYTES)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        bind.pdfRead.fromBytes(bytes).swipeHorizontal(false)
                                .onPageChange(new OnPageChangeListener() {
                                    @Override
                                    public void onPageChanged(int page, int pageCount) {
                                        int currentPage = (page + 1);
                                        bind.readSubTitleHead.setText(currentPage + "/" + pageCount);
                                    }
                                })
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        Toast.makeText(PdfViewPage.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        Toast.makeText(PdfViewPage.this, "Error on page: "+ page + " Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }).load();
                        bind.progBarRead.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        bind.progBarRead.setVisibility(View.GONE);
                    }
                });
    }
}