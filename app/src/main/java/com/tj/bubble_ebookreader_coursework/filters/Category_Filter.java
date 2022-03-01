package com.tj.bubble_ebookreader_coursework.filters;

import android.widget.Filter;

import com.tj.bubble_ebookreader_coursework.models.Category_Model;
import com.tj.bubble_ebookreader_coursework.adapters.Category_Adapter;

import java.util.ArrayList;

public class Category_Filter extends Filter {

    ArrayList<Category_Model> filtList;
    Category_Adapter cAdap;

    public Category_Filter(ArrayList<Category_Model> filtList, Category_Adapter cAdap) {
        this.filtList = filtList;
        this.cAdap = cAdap;
    }

    @Override
    protected FilterResults performFiltering(CharSequence charSequence) {
        FilterResults res = new FilterResults();
        if(charSequence != null && charSequence.length() > 0) {
            charSequence = charSequence.toString().toUpperCase();
            ArrayList<Category_Model> filtMods = new ArrayList<>();
            for(int i = 0; i < filtList.size(); i++) {
                if(filtList.get(i).getCategory().toUpperCase().contains(charSequence)) {
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
        cAdap.catList = (ArrayList<Category_Model>)filterResults.values;
        cAdap.notifyDataSetChanged();
    }
}
