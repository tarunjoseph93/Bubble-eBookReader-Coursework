package com.tj.bubble_ebookreader_coursework.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.tj.bubble_ebookreader_coursework.MyApplication;
import com.tj.bubble_ebookreader_coursework.databinding.CustomRowsForPdfUserBinding;
import com.tj.bubble_ebookreader_coursework.filters.Pdf_User_Filter;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;
import com.tj.bubble_ebookreader_coursework.pages.BookDetailPage;

import java.util.ArrayList;

public class Book_User_Adapter extends RecyclerView.Adapter<Book_User_Adapter.HolderPdfUser> implements Filterable {
    private Context con;
    public ArrayList<Pdf_Model> pdfList, filtList;
    private Pdf_User_Filter filt;

    private CustomRowsForPdfUserBinding bind;

    public Book_User_Adapter(Context con, ArrayList<Pdf_Model> pdfList) {
        this.con = con;
        this.pdfList = pdfList;
        this.filtList = pdfList;
    }

    @NonNull
    @Override
    public HolderPdfUser onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bind = CustomRowsForPdfUserBinding.inflate(LayoutInflater.from(con), parent, false);
        return new HolderPdfUser(bind.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfUser holder, int position) {
        Pdf_Model mod = pdfList.get(position);
        String bookId = mod.getId();
        String title = mod.getTitle();
        String desc = mod.getDescription();
        String pdfUrl = mod.getUrl();
        String catId = mod.getCategoryId();
        long timestamp = mod.getTimestamp();
        String date = MyApplication.timestampFormat(timestamp);

        holder.bookTitleText.setText(title);
        holder.bookDescText.setText(desc);
        holder.bookDate.setText(date);

        MyApplication.pdfFromUrlSinglePageLoad("" + pdfUrl, "" + title, holder.viewPdf, holder.progBarPdf, null);
        MyApplication.catLoad("" + catId, holder.bookCatText);
        MyApplication.pdfSizeLoad("" + pdfUrl, "" + title, holder.bookSize);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(con, BookDetailPage.class);
                intent.putExtra("bookId", bookId);
                con.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return pdfList.size();
    }

    @Override
    public Filter getFilter() {
        if(filt == null) {
            filt = new Pdf_User_Filter(filtList, this);
        }
        return filt;
    }

    class HolderPdfUser extends RecyclerView.ViewHolder {

        TextView bookTitleText, bookDescText, bookCatText, bookSize, bookDate;
        PDFView viewPdf;
        ProgressBar progBarPdf;

        public HolderPdfUser(@NonNull View itemView) {
            super(itemView);

            bookTitleText = bind.bookTitleText;
            bookDescText = bind.bookDescText;
            bookCatText = bind.bookCatText;
            bookSize = bind.bookSize;
            bookDate = bind.bookDate;
            viewPdf = bind.viewPdf;
            progBarPdf = bind.progBarPdf;
        }
    }
}
