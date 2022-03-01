package com.tj.bubble_ebookreader_coursework.adapters;

import static com.tj.bubble_ebookreader_coursework.Constants.PDF_MAX_BYTES;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import com.tj.bubble_ebookreader_coursework.MyApplication;
import com.tj.bubble_ebookreader_coursework.PdfEditPage;
import com.tj.bubble_ebookreader_coursework.databinding.CustomRowsForPdfAdminBinding;
import com.tj.bubble_ebookreader_coursework.filters.Pdf_Filter;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;

import java.util.ArrayList;

public class Book_Admin_Adapter extends RecyclerView.Adapter<Book_Admin_Adapter.PdfHolderAdmin> implements Filterable {
    private Context con;
    public ArrayList<Pdf_Model> pdfList, filtList;
    private CustomRowsForPdfAdminBinding bind;
    private Pdf_Filter filtPdf;
    private ProgressDialog proDia;

    public Book_Admin_Adapter(Context con, ArrayList<Pdf_Model> pdfList) {
        this.con = con;
        this.pdfList = pdfList;
        this.filtList = pdfList;

        proDia = new ProgressDialog(con);
        proDia.setTitle("Gotta wait...");
        proDia.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public PdfHolderAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bind = CustomRowsForPdfAdminBinding.inflate(LayoutInflater.from(con));
        return new PdfHolderAdmin(bind.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull PdfHolderAdmin holder, int position) {
        Pdf_Model modPdf = pdfList.get(position);
        String title = modPdf.getTitle();
        String desc = modPdf.getDescription();
        long timestamp = modPdf.getTimestamp();
        String dateFormat = MyApplication.timestampFormat(timestamp);
        holder.bookTitleText.setText(title);
        holder.bookDescText.setText(desc);
        holder.bookDate.setText(dateFormat);

        catLoad(modPdf,holder);
        pdfFromUrlLoad(modPdf,holder);
        pdfSizeLoad(modPdf,holder);

        holder.additionalInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                additionalOptionsList(modPdf,holder);
            }
        });

    }

    private void additionalOptionsList(Pdf_Model modPdf, PdfHolderAdmin holder) {
        String bookId = modPdf.getId();
        String bookUrl = modPdf.getUrl();
        String bookTitle = modPdf.getTitle();

        String[] options = {"Edit", "Delete"};
        AlertDialog.Builder ad = new AlertDialog.Builder(con);
        ad.setTitle("Choose your options: ")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i == 0) {
                            Intent intent = new Intent(con, PdfEditPage.class);
                            intent.putExtra("bookId", bookId);
                            con.startActivity(intent);
                        }
                        else if(i == 1){
                            deleteBook(modPdf, holder);
                        }
                    }
                }).show();
    }

    private void deleteBook(Pdf_Model modPdf, PdfHolderAdmin holder) {
        String bookId = modPdf.getId();
        String bookUrl = modPdf.getUrl();
        String bookTitle = modPdf.getTitle();
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

    private void pdfSizeLoad(Pdf_Model modPdf, PdfHolderAdmin holder) {
        String pdfUrl = modPdf.getUrl();
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        sRef.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes = storageMetadata.getSizeBytes();
                        double kb = bytes/1024;
                        double mb = kb/1024;
                        if(mb >= 1) {
                            holder.bookSize.setText(String.format("%.2f", mb) + "MB");
                        }
                        else if(kb >= 1) {
                            holder.bookSize.setText(String.format("%.2f", kb) + "KB");
                        }
                        else {
                            holder.bookSize.setText(String.format("%.2f", bytes) + "bytes");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(con, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pdfFromUrlLoad(Pdf_Model modPdf, PdfHolderAdmin holder) {
        String pdfUrl = modPdf.getUrl();
        StorageReference sRef = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        sRef.getBytes(PDF_MAX_BYTES)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        holder.viewPdf.fromBytes(bytes).pages(0).spacing(0).swipeHorizontal(false)
                                .enableSwipe(false).onError(new OnErrorListener() {
                            @Override
                            public void onError(Throwable t) {
                                holder.progBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(con, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progBar.setVisibility(View.INVISIBLE);
                                        Toast.makeText(con, "" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progBar.setVisibility(View.INVISIBLE);
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(con, "Failed to get Pdf Url. Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void catLoad(Pdf_Model modPdf, PdfHolderAdmin holder) {
        String catId = modPdf.getCategoryId();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.child(catId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String cat = "" + snapshot.child("category").getValue();
                        holder.bookCatText.setText(cat);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    @Override
    public Filter getFilter() {
        if(filtPdf == null) {
            filtPdf = new Pdf_Filter(filtList, this);
        }
        return filtPdf;
    }

    class PdfHolderAdmin extends RecyclerView.ViewHolder {

        PDFView viewPdf;
        ProgressBar progBar;
        TextView bookTitleText,bookDescText,bookCatText,bookSize,bookDate;
        ImageButton additionalInfoButton;
        public PdfHolderAdmin(@NonNull View itemView) {
            super(itemView);
            viewPdf = bind.viewPdf;
            progBar = bind.progBarPdf;
            bookTitleText = bind.bookTitleText;
            bookDescText = bind.bookDescText;
            bookCatText = bind.bookCatText;
            bookSize = bind.bookSize;
            bookDate = bind.bookDate;
            additionalInfoButton = bind.additionalInfoButton;
        }
    }
}
