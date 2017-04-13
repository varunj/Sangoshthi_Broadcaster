package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Varun on 13-Apr-17.
 */

public class StartShowCallsActivity extends AppCompatActivity  {
    private String showName, senderPhoneNum;
    private String VIDEO_URI = "/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startshowcalls);

        // get showName and senderPhoneNumber and videoname
        Intent i = getIntent();
        showName = i.getStringExtra("showName");
        VIDEO_URI = "/" + i.getStringExtra("videoname");
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        senderPhoneNum = pref.getString("phoneNum", "0000000000");
    }




}
