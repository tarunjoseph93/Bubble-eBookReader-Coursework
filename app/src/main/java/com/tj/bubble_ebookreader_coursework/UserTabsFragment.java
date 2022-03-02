package com.tj.bubble_ebookreader_coursework;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.adapters.Book_User_Adapter;
import com.tj.bubble_ebookreader_coursework.databinding.FragmentUserTabsBinding;
import com.tj.bubble_ebookreader_coursework.models.Pdf_Model;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserTabsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserTabsFragment extends Fragment {

    private String cat, catId, uid;
    private ArrayList<Pdf_Model> pdfList;
    private Book_User_Adapter bookUserAdap;
    private FragmentUserTabsBinding bind;

    public UserTabsFragment() {
        // Required empty public constructor
    }

    public static UserTabsFragment newInstance(String catId, String cat, String uid) {
        UserTabsFragment fragment = new UserTabsFragment();
        Bundle args = new Bundle();
        args.putString("categoryId", catId);
        args.putString("category", cat);
        args.putString("uid", uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            catId = getArguments().getString("categoryId");
            cat = getArguments().getString("category");
            uid = getArguments().getString("uid");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bind = FragmentUserTabsBinding.inflate(LayoutInflater.from(getContext()), container, false);
        if(cat.equals("All")) {
            allBooksLoad();
        }
        else if (cat.equals("Most Viewed")) {
            mostViewedOrDownloadedBooksLoad("viewsCount");
        }
        else if(cat.equals("Most Downloaded")) {
            mostViewedOrDownloadedBooksLoad("downloadsCount");
        }
        else {
            categorisedBooksLoad();
        }
        bind.userBookSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                bookUserAdap.getFilter().filter(charSequence);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        return bind.getRoot();
    }

    private void allBooksLoad() {
        pdfList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfList.clear();
                for(DataSnapshot dSnap : snapshot.getChildren()) {
                    Pdf_Model mod = dSnap.getValue(Pdf_Model.class);
                    pdfList.add(mod);
                }
                bookUserAdap = new Book_User_Adapter(getContext(), pdfList);
                bind.booksUserRecyclerView.setAdapter(bookUserAdap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void mostViewedOrDownloadedBooksLoad(String sortBy) {
        pdfList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.orderByChild(sortBy).limitToLast(10)
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pdfList.clear();
                for(DataSnapshot dSnap : snapshot.getChildren()) {
                    Pdf_Model mod = dSnap.getValue(Pdf_Model.class);
                    pdfList.add(mod);
                }
                bookUserAdap = new Book_User_Adapter(getContext(), pdfList);
                bind.booksUserRecyclerView.setAdapter(bookUserAdap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void categorisedBooksLoad() {
        pdfList = new ArrayList<>();
        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Books");
        dRef.orderByChild("categoryId").equalTo(catId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        pdfList.clear();
                        for(DataSnapshot dSnap : snapshot.getChildren()) {
                            Pdf_Model mod = dSnap.getValue(Pdf_Model.class);
                            pdfList.add(mod);
                        }
                        bookUserAdap = new Book_User_Adapter(getContext(), pdfList);
                        bind.booksUserRecyclerView.setAdapter(bookUserAdap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}