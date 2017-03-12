package io.github.varunj.sangoshthi_broadcaster;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Varun on 04-03-2017.
 */

public class ListSessionsActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        //print email and username and phonenum
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        System.out.println("xxx: " + pref.getString("name", "0000000000"));
        System.out.println("xxx: " + pref.getString("phoneNum", "0000000000"));
        System.out.println("xxx: " + pref.getString("googleEmail", "0000000000"));

        // Create the adapter that will return a fragment . Set up the ViewPager with the sections adapter.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fragManager) {
            super(fragManager);
        }
        @Override
        public Fragment getItem(int position) {
            // xxx: can add more fragments in home page by 1,2,...
            switch (position) {
                case 0:
                    return new ListGroupsFragment();
            }
            return null ;
        }
        @Override
        public int getCount() {
            return 1;
        }
    }
}
