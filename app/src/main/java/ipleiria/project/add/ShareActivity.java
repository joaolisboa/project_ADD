package ipleiria.project.add;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.UUID;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOUploadFileTask;
import ipleiria.project.add.Utils.UriHelper;

public class ShareActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_data);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            handleFile(intent);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            handleMultipleFiles(intent);
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }

    private void handleFile(Intent intent) {
        String accessToken = getSharedPreferences("services", MODE_PRIVATE).getString("meo_access_token", "");

        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null) {
            new MEOUploadFileTask(ShareActivity.this, new MEOUploadFileTask.Callback() {

                @Override
                public void onComplete(MEOCloudResponse<Metadata> result) {
                    if (result.responseSuccessful()) {
                        System.out.println("Upload successful");
                    } else {
                        System.out.println("Upload failed");
                        System.out.println(result.getCode() + ": " + result.getError());
                    }
                }

                @Override
                public void onError(Exception e) {
                    System.out.println("Upload failed");
                    System.out.println(e.getMessage());
                }
            }).execute(accessToken, uri.toString(), UriHelper.getFileName(ShareActivity.this, uri));
        }
    }

    private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                System.out.println(uri.toString());
            }
        }
    }
}
