package com.tj.bubble_ebookreader_coursework.filters;

import android.widget.Filter;

import com.tj.bubble_ebookreader_coursework.adapters.Book_User_Adapter;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;

import java.util.ArrayList;
import java.util.Locale;

public class Pdf_User_Filter extends Filter {
    ArrayList<Pdf_Model> filtList;
    Book_User_Adapter bookUserAdap;

    public Pdf_User_Filter(ArrayList<Pdf_Model> filtList, Book_User_Adapter bookUserAdap) {
        this.filtList = filtList;
        this.bookUserAdap = bookUserAdap;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults res = new FilterResults();
        if(charSequence != null || charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<Pdf_Model> filtMods = new ArrayList<>();
            for(int i = 0; i < filtList.size(); i++) {
                if(filtList.get(i).getTitle().toUpperCase().contains(charSequence)) {
                    filtMods.add(filtList.get(i));
                }
            }
            res.count = filtMods.size();
            res.values = filtMods;
        }
        else {
            res.count = filtList.size();
            res.values = filtList;
        }

        return res;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
        bookUserAdap.pdfList = (ArrayList<Pdf_Model>)filterResults.values;
        bookUserAdap.notifyDataSetChanged();
    }
}
