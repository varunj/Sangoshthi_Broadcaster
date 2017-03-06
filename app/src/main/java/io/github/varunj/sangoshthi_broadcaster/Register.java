package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by Varun on 04-03-2017.
 */

public class Register extends AppCompatActivity {

    EditText editTextName, editTextPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        editTextName = (EditText)findViewById(R.id.name);
        editTextPhone = (EditText)findViewById(R.id.phone);
    }

    public void setData(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        if(editTextName.getText().toString().length() >= Integer.parseInt(getString(R.string.register_minLen_name))) {
            if(editTextPhone.getText().toString().length() == Integer.parseInt(getString(R.string.register_len_phoneNum))) {
                editor.putString("name", editTextName.getText().toString());
                editor.putString("phoneNum", editTextPhone.getText().toString());
                editor.putBoolean("isLoggedIn" , true);
                editor.commit();

                Intent intent = new Intent(this, LauncherActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                Toast.makeText(Register.this, getString(R.string.toast_wrong_phoneNum), Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(Register.this, getString(R.string.toast_wrong_name), Toast.LENGTH_LONG).show();
        }

    }
}
