package ipleiria.project.add;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;

import java.util.ArrayList;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.UploadFileTask;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOUploadFileTask;
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
        if (!MEOCloudClient.isClientInitialized()) {
            String accessToken = getSharedPreferences("services", MODE_PRIVATE).getString("meo_access_token", "");
            if(!accessToken.isEmpty()) {
                MEOCloudClient.init(accessToken);
            }
        }

        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (uri != null && MEOCloudClient.isClientInitialized()) {
            new MEOUploadFileTask(ShareActivity.this, new MEOCallback<Metadata>() {

                @Override
                public void onComplete(MEOCloudResponse<Metadata> result) {
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

            new UploadFileTask(ShareActivity.this, DropboxClientFactory.getClient(), new UploadFileTask.Callback() {

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

    private void handleMultipleFiles(Intent intent) {
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uri : uris) {
                System.out.println(uri.toString());
            }
        }
    }
}
