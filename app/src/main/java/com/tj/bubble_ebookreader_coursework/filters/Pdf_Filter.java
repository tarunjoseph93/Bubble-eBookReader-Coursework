package com.tj.bubble_ebookreader_coursework.filters;

import android.widget.Filter;

import com.tj.bubble_ebookreader_coursework.adapters.Book_Admin_Adapter;
import com.tj.bubble_ebookreader_coursework.adapters.Category_Adapter;
import com.tj.bubble_ebookreader_coursework.models.Category_Model;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;

import java.util.ArrayList;

public class Pdf_Filter extends Filter {

    ArrayList<Pdf_Model> filtList;
    Book_Admin_Adapter bAdminAdap;

    public Pdf_Filter(ArrayList<Pdf_Model> filtList, Book_Admin_Adapter bAdminAdap) {
        this.filtList = filtList;
        this.bAdminAdap = bAdminAdap;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults res = new FilterResults();
        if(charSequence != null && charSequence.length() > 0) {
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
        bAdminAdap.pdfList = (ArrayList<Pdf_Model>)filterResults.values;
        bAdminAdap.notifyDataSetChanged();
    }
}
