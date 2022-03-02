package com.tj.bubble_ebookreader_coursework.pages;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tj.bubble_ebookreader_coursework.UserTabsFragment;
import com.tj.bubble_ebookreader_coursework.databinding.ActivityUserDashboardBinding;
import com.tj.bubble_ebookreader_coursework.models.Category_Model;

import java.util.ArrayList;

public class UserDashboard extends AppCompatActivity {

    private ActivityUserDashboardBinding bind;
    private FirebaseAuth fireAuth;
    public ArrayList<Category_Model> catList;
    public ViewPagerAdapter vPagerAdap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bind = ActivityUserDashboardBinding.inflate(getLayoutInflater());
        setContentView(bind.getRoot());
        fireAuth = FirebaseAuth.getInstance();
        userCheck();
        setupViewPagerAdapter(bind.userViewPager);
        bind.userTabLayout.setupWithViewPager(bind.userViewPager);
        bind.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fireAuth.signOut();
                userCheck();
            }
        });
    }

    private void setupViewPagerAdapter(ViewPager vPager) {
        vPagerAdap = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        catList = new ArrayList<>();

        DatabaseReference dRef = FirebaseDatabase.getInstance().getReference("Categories");
        dRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                catList.clear();
                Category_Model modAll = new Category_Model("01", "All", "", 1);
                Category_Model modMostViewed = new Category_Model("02", "Most Viewed", "", 1);
                Category_Model modMostDownloaded = new Category_Model("03", "Most Downloaded", "", 1);

                catList.add(modAll);
                catList.add(modMostViewed);
                catList.add(modMostDownloaded);

                vPagerAdap.addFrag(UserTabsFragment.newInstance("" + modAll.getId(), "" + modAll.getCategory(), "" + modAll.getUid()), modAll.getCategory());
                vPagerAdap.addFrag(UserTabsFragment.newInstance("" + modMostViewed.getId(), "" + modMostViewed.getCategory(), "" + modMostViewed.getUid()), modMostViewed.getCategory());
                vPagerAdap.addFrag(UserTabsFragment.newInstance("" + modMostDownloaded.getId(), "" + modMostDownloaded.getCategory(), "" + modMostDownloaded.getUid()), modMostDownloaded.getCategory());
                vPagerAdap.notifyDataSetChanged();

                for(DataSnapshot dSnap : snapshot.getChildren()) {
                    Category_Model mod = dSnap.getValue(Category_Model.class);
                    catList.add(mod);
                    vPagerAdap.addFrag(UserTabsFragment.newInstance("" + mod.getId(), "" + mod.getCategory(), "" + mod.getUid()), mod.getCategory());
                    vPagerAdap.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        vPager.setAdapter(vPagerAdap);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<UserTabsFragment> fragList = new ArrayList<>();
        private ArrayList<String> fragTitleList = new ArrayList<>();
        private Context con;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context con) {
            super(fm, behavior);
            this.con = con;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragList.get(position);
        }

        @Override
        public int getCount() {
            return fragList.size();
        }

        private void addFrag(UserTabsFragment frag, String title) {
            fragList.add(frag);
            fragTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            return fragTitleList.get(position);
        }
    }

    private void userCheck() {
        FirebaseUser fireUser = fireAuth.getCurrentUser();
        if(fireUser == null) {
            startActivity(new Intent(UserDashboard.this, MainActivity.class));
            finish();
        }
        else {
            String uEmail = fireUser.getEmail();
            bind.userEmailHead.setText(uEmail);
        }
    }
}