package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Varun on 12-Mar-17.
 */

public class PickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);

        final Button button_picker_add_photo = (Button) findViewById(R.id.picker_add_photo);
        button_picker_add_photo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddPhotoActivity.class);
                startActivity(intent);
            }
        });

        final Button button_picker_add_video = (Button) findViewById(R.id.picker_add_video);
        button_picker_add_video.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddVideoActivity.class);
                startActivity(intent);
            }
        });

        final Button button_picker_add_story = (Button) findViewById(R.id.picker_add_story);
        button_picker_add_story.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AddShowActivity.class);
                startActivity(intent);
            }
        });

        final Button button_picker_gallery = (Button) findViewById(R.id.picker_gallery);
        button_picker_gallery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VIewGalleryActivity.class);
                startActivity(intent);
            }
        });

        final Button button_picker_participate = (Button) findViewById(R.id.picker_participate);
        button_picker_participate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ListSessionsActivity.class);
                startActivity(intent);
            }
        });
    }
}
