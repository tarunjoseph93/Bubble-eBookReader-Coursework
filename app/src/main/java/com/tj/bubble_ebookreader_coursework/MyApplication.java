package com.tj.bubble_ebookreader_coursework;

import static com.tj.bubble_ebookreader_coursework.Constants.PDF_MAX_BYTES;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.tj.bubble_ebookreader_coursework.adapters.Book_Admin_Adapter;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static final String timestampFormat(long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);
        String date = DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;
    }

    public static void deleteBook(Context con, String bookId, String bookUrl, String bookTitle) {
        ProgressDialog proDia = new ProgressDialog(con);
        proDia.setTitle("Gotta wait...");
        proDia.setMessage("Deleting: " + bookTitle);
        proDia.show();
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        sRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
                        dRef.child(bookId).removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        proDia.dismiss();
                                        Toast.makeText(con, "Your book has been deleted successfully!", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        proDia.dismiss();
                                        Toast.makeText(con, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        proDia.dismiss();
                        Toast.makeText(con, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public static void pdfSizeLoad(String pdfUrl, String pdfTitle, TextView bookSize) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        sRef.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        double kb = bytes/1024;
                        double mb = kb/1024;
                        if(mb >= 1) {
                            bookSize.setText(String.format("%.2f", mb) + "MB");
                        }
                        else if(kb >= 1) {
                            bookSize.setText(String.format("%.2f", kb) + "KB");
                        }
                        else {
                            bookSize.setText(String.format("%.2f", bytes) + "bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }

    public static void pdfFromUrlSinglePageLoad(String pdfUrl, String pdfTitle, PDFView viewPdf, ProgressBar progBar) {
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        sRef.getBytes(PDF_MAX_BYTES)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        viewPdf.fromBytes(bytes).pages(0).spacing(0).swipeHorizontal(false)
                                .enableSwipe(false).onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                progBar.setVisibility(View.INVISIBLE);
                            }
                        })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        progBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        progBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progBar.setVisibility(View.INVISIBLE);
                    }
                });
    }

    public static void catLoad(String catId, TextView bookCatText) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.child(catId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String cat = "" + snapshot.child("category").getValue();
                        bookCatText.setText(cat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public static void viewCountIncrement(String bookId) {
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String viewsCount = "" + snapshot.child("viewsCount").getValue();
                        if(viewsCount.equals("") || viewsCount.equals("null")) {
                            viewsCount = "0";
                        }

                        long newViewsCount = Long.parseLong(viewsCount) + 1;
                        HashMap<String, Object> hMap = new HashMap<>();
                        hMap.put("viewsCount", newViewsCount);

                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Books");
                        ref.child(bookId)
                                .updateChildren(hMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
