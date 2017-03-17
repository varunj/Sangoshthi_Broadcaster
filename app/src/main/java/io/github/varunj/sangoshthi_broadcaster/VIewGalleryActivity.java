package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Varun on 14-Mar-17.
 */

public class VIewGalleryActivity extends AppCompatActivity {

    private static int REQUEST_VIEW_GALLERY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openGallery();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == REQUEST_VIEW_GALLERY && resultCode == RESULT_OK) {
            Toast.makeText(this, "Just Viewed Gallery", Toast.LENGTH_SHORT).show();
            finish();
        }
        else if (requestCode == REQUEST_VIEW_GALLERY && resultCode == RESULT_CANCELED) {
            finish();
        }
    }

    private void openGallery() {
        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS + "/Sangoshthi");

        String bucketId = "";
        final String[] projection = new String[] {"DISTINCT " + MediaStore.Images.Media.BUCKET_DISPLAY_NAME + ", " + MediaStore.Images.Media.BUCKET_ID};
        final Cursor cur = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);
        while (cur != null && cur.moveToNext()) {
            final String bucketName = cur.getString((cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
            if (bucketName.equals("Sangoshthi")) {
                bucketId = cur.getString((cur.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)));
                break;
            }
        }

        Uri mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
//        Uri mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        if (bucketId.length() > 0) {
            mediaUri = mediaUri.buildUpon()
                    .authority("media")
                    .appendQueryParameter("bucketId", bucketId)
                    .build();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, mediaUri);
        startActivityForResult(intent, REQUEST_VIEW_GALLERY);

    }

}
