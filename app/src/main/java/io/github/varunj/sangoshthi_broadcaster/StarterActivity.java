package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Varun on 04-03-2017.
 */

public class StarterActivity extends AppCompatActivity {

    public static final String IP_ADDR = "192.168.2.71";
    public static final String SERVER_USERNAME = "root";
    public static final String SERVER_PASS = "root";
    public static final Integer SERVER_PORT = 5672;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(prefs.getBoolean("isLoggedIn", false)) {
            Intent intent = new Intent(this,LauncherActivity.class);
            startActivity(intent);
            finish();
        }
        else {
            Intent intent = new Intent(this,Register.class);
            startActivity(intent);
            finish();
        }
    }
}
