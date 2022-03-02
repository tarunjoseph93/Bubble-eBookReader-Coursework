package com.tj.bubble_ebookreader_coursework.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tj.bubble_ebookreader_coursework.pages.PdfListAdminPage;
import com.tj.bubble_ebookreader_coursework.filters.Category_Filter;
import com.tj.bubble_ebookreader_coursework.models.Category_Model;
import com.tj.bubble_ebookreader_coursework.databinding.CustomRowsForCategoryAdminBinding;

import java.util.ArrayList;

public class Category_Adapter extends RecyclerView.Adapter<Category_Adapter.Category_Holder> implements Filterable {
    private Context con;
    public ArrayList<Category_Model> catList, filtList;
    private CustomRowsForCategoryAdminBinding bind;
    private Category_Filter filt;

    public Category_Adapter(Context con, ArrayList<Category_Model> catList) {
        this.con = con;
        this.catList = catList;
        this.filtList = catList;
    }

    @NonNull
    @Override
    public Category_Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        bind = CustomRowsForCategoryAdminBinding.inflate(LayoutInflater.from(con), parent, false);
        return new Category_Holder(bind.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull Category_Holder holder, int position) {
        Category_Model modCat = catList.get(position);
        String id = modCat.id;
        String uid = modCat.uid;
        long timestamp = modCat.timestamp;
        String cat = modCat.category;

        holder.catData.setText(cat);
        holder.catDelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ad = new AlertDialog.Builder(con);
                ad.setTitle("Category Deletion").setMessage("Please confirm if you want this category deleted.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                catDeletion(modCat, holder);
                                Toast.makeText(con, "Deleting your category now!", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(con, PdfListAdminPage.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", cat);
                con.startActivity(intent);
            }
        });
    }

    private void catDeletion(Category_Model modCat, Category_Holder holder) {
        String id = modCat.getId();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(con, "Deletion Complete!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(con, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    @Override
    public Filter getFilter() {
        if(filt == null) {
            filt = new Category_Filter(filtList, this);
        }
        return filt;
    }

    class Category_Holder extends RecyclerView.ViewHolder {
        TextView catData;
        ImageButton catDelBtn;
        public Category_Holder(@NonNull View itemView) {
            super(itemView);

            catData = bind.catData;
            catDelBtn = bind.catDelBtn;
        }
    }
}
