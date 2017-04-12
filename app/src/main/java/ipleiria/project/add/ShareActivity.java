package ipleiria.project.add;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxUploadFile;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOUploadFile;
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
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        handleFile(uri);
    }

    private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                handleFile(uri);
            }
        }
    }

    private void handleFile(Uri uri){
        if (uri != null && MEOCloudClient.isClientInitialized()) {

            try {
                InputStream is = getContentResolver().openInputStream(uri);
                FileOutputStream fos = openFileOutput(UriHelper.getFileName(ShareActivity.this, uri), Context.MODE_PRIVATE);
                byte[] buffer = new byte[1024 * 100];
                int nBytes;
                while((nBytes = is.read(buffer)) != -1){
                    fos.write(buffer, 0, nBytes);
                    fos.flush();
                }
                is.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            new MEOUploadFile(ShareActivity.this, new MEOCallback<MEOMetadata>() {

                @Override
                public void onComplete(MEOCloudResponse<MEOMetadata> result) {
                    System.out.println("Upload successful");
                }

                @Override
                public void onRequestError(HttpErrorException httpE) {

                }

                @Override
                public void onError(Exception e) {
                    Log.e("UploadError", e.getMessage(), e);
                }
            }).execute(uri.toString(), UriHelper.getFileName(ShareActivity.this, uri));

            new DropboxUploadFile(ShareActivity.this, DropboxClientFactory.getClient(), new DropboxUploadFile.Callback() {

                @Override
                public void onUploadComplete(FileMetadata result) {
                    System.out.println(result.getName());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("UploadDropError", e.getMessage(), e);
                }
            }).execute(uri.toString());
        }
    }
}
