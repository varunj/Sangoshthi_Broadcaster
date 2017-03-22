package io.github.varunj.sangoshthi_broadcaster;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Varun on 17-Mar-17.
 */

public class AddShowActivity extends AppCompatActivity {

    private static int REQUEST_PICK_VIDEO = 0;
    EditText newshow_showname, newshow_showparticipants;
    private String senderPhoneNum;
    public String showTime = "-1", showDate = "-1", showPath = "-1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addshow);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        senderPhoneNum = pref.getString("phoneNum", "0000000000");

        // AMQP stuff
        AMQPPublish.setupConnectionFactory();
        AMQPPublish.publishToAMQP();

        // initialise screen elements
        newshow_showname = (EditText)findViewById(R.id.newshow_showname);
        newshow_showparticipants = (EditText)findViewById(R.id.newshow_showparticipants);

        final Button newshow_showtime = (Button) findViewById(R.id.newshow_showtime);
        newshow_showtime.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.picker_participate));
        newshow_showtime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(AddShowActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        showTime = "" + selectedHour + ":" + selectedMinute + ":00" ;
                        newshow_showtime.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.picker_add_story));
                    }
                }, 15 , 0 , false);
                mTimePicker.setTitle("Select Time For the Show");
                mTimePicker.show();
            }
        });

        final Button newshow_showdate = (Button) findViewById(R.id.newshow_showdate);
        newshow_showdate.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.picker_participate));
        newshow_showdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                class mDateSetListener implements DatePickerDialog.OnDateSetListener {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        showDate = "" + dayOfMonth + "/" + (monthOfYear+1) + "/" + year;
                        newshow_showdate.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.picker_add_story));
                    }
                }
                DatePickerDialog dialog = new DatePickerDialog(AddShowActivity.this, new mDateSetListener(), mYear, mMonth, mDay);
                dialog.show();
            }
        });

        final Button newshow_showpath = (Button) findViewById(R.id.newshow_showpath);
        newshow_showpath.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.picker_participate));
        newshow_showpath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("video/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_PICK_VIDEO);
                newshow_showpath.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.picker_add_story));
            }
        });

        final Button newshow_ok = (Button) findViewById(R.id.newshow_ok);
        newshow_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (newshow_showname.getText().toString().trim().length() > 0 && newshow_showparticipants.getText().toString().trim().length() > 0
                        && !showPath.equals("-1") && !showDate.equals("-1") && !showTime.equals("-1")) {
                    try {
                        final JSONObject jsonObject = new JSONObject();
                        //primary key: <broadcaster, show_name>
                        jsonObject.put("objective", "create_show");
                        jsonObject.put("show_name", newshow_showname.getText().toString().trim());
                        jsonObject.put("time_of_airing", showDate + " " + showTime);
                        jsonObject.put("broadcaster", senderPhoneNum);
                        jsonObject.put("timestamp", DateFormat.getDateTimeInstance().format(new Date()));
                        jsonObject.put("video_name", showPath);
                        ArrayList<String> temp= new ArrayList<>();
                        for (String x: newshow_showparticipants.getText().toString().trim().split(",")) {
                            temp.add(x.trim());
                        }
                        jsonObject.put("list_of_asha", new JSONArray(temp));
                        AMQPPublish.queue.putLast(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    Toast.makeText(AddShowActivity.this, "Enter Valid Data!", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(AddShowActivity.this, "Show Created! Press Back Now.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (AMQPPublish.publishThread != null)
            AMQPPublish.publishThread.interrupt();
    }

    @Override
    public void onBackPressed() {
        if (AMQPPublish.publishThread != null)
            AMQPPublish.publishThread.interrupt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_PICK_VIDEO && resultCode == RESULT_OK) {
            Uri selectedImageURI = intent.getData();
            File imageFile = new File(getRealPathFromURI(selectedImageURI));
            showPath = imageFile.getName();
        }
    }

    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
