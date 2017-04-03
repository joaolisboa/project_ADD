package ipleiria.project.add;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.util.List;

import ipleiria.project.add.Dropbox.DownloadFileTask;
import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.DeleteFileTask;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Tasks.GetMetadataTask;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.Tasks.MEODownloadFileTask;
import ipleiria.project.add.MEOCloud.Tasks.SearchFileTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("services", MODE_PRIVATE);

        if(!preferences.getString("dropbox_access_token", "").isEmpty()){
            DropboxClientFactory.init(preferences.getString("dropbox_access_token", ""));
        }
        if(!preferences.getString("meo_access_token", "").isEmpty()){
            MEOCloudClient.init(preferences.getString("meo_access_token", ""));
        }
    }

    public void goToAccounts(View view) {
        startActivity(new Intent(this, ServiceChooserActivity.class));
    }

    public void downloadFile(View view) {
        new MEODownloadFileTask(MainActivity.this, new MEOCallback<FileResponse>() {

            @Override
            public void onComplete(MEOCloudResponse<FileResponse> result) {
                FileResponse fileResponse = result.getResponse();
                System.out.println(fileResponse.getPath());
                System.out.println(fileResponse.length());

            }

            @Override
            public void onRequestError(HttpErrorException httpE) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("DownloadError", e.getMessage(), e);
            }
        }).execute("/exploring_luciddreaming.pdf");

        new GetMetadataTask(new MEOCallback<Metadata>() {

            @Override
            public void onComplete(MEOCloudResponse<Metadata> result) {
                System.out.println(result.getResponse().toJson());
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("MetadataError", e.getMessage(), e);
            }
        }).execute("/");

        new DownloadFileTask(MainActivity.this, DropboxClientFactory.getClient(), new DownloadFileTask.Callback() {

            @Override
            public void onDownloadComplete(File result) {
                System.out.println(result.getName());
            }

            @Override
            public void onError(Exception e) {
                Log.e("ServiceError", e.getMessage(), e);
            }
        }).execute("/exploring_luciddreaming.pdf");

    }

    public void searchFile(View view) {
        new SearchFileTask(new MEOCallback<List<Metadata>>() {

            @Override
            public void onComplete(MEOCloudResponse<List<Metadata>> result) {
                for (Metadata m : result.getResponse()) {
                    System.out.println(m.toJson());
                }
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("SearchError", e.getMessage(), e);
            }
        }).execute("/", "lucid");
    }

    public void deleteFile(View view) {
        new DeleteFileTask(new MEOCallback<Metadata>() {

            @Override
            public void onComplete(MEOCloudResponse<Metadata> result) {
                System.out.println(result.getResponse().toJson());
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {

            }

            @Override
            public void onError(Exception e) {
                Log.e("DeleteError", e.getMessage(), e);
            }
        }).execute("/exploring_luciddreaming.pdf");
    }
}
