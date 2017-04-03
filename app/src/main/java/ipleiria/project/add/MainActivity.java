package ipleiria.project.add;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.List;

import ipleiria.project.add.Dropbox.DropboxDownloadFile;
import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxGetMetadata;
import ipleiria.project.add.Dropbox.DropboxListFolder;
import ipleiria.project.add.MEOCloud.Data.FileResponse;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEODeleteFile;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetMetadata;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.Tasks.MEODownloadFile;
import ipleiria.project.add.MEOCloud.Tasks.MEOSearchFile;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("services", MODE_PRIVATE);

        if(!preferences.getString("dropbox_access_token", "").isEmpty()){
            DropboxClientFactory.init(preferences.getString("dropbox_access_token", ""));
            new DropboxListFolder(DropboxClientFactory.getClient(), new DropboxListFolder.Callback(){

                @Override
                public void onComplete(ListFolderResult result) {
                    System.out.println(result.getEntries().get(0));
                }

                @Override
                public void onError(Exception e) {
                    Log.e("ServiceError", e.getMessage(), e);
                }
            }).execute("");

            new DropboxGetMetadata(DropboxClientFactory.getClient(), new DropboxGetMetadata.Callback(){
                @Override
                public void onComplete(Metadata result) {
                    if(result instanceof FileMetadata){
                        System.out.println("path is file");
                    }else if(result instanceof FolderMetadata){
                        System.out.println("path is folder");
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("ServiceError", e.getMessage(), e);
                }
            }).execute("/exploring_luciddreaming.pdf");
        }
        if(!preferences.getString("meo_access_token", "").isEmpty()){
            MEOCloudClient.init(preferences.getString("meo_access_token", ""));
        }
    }

    public void listFiles(View view){
        startActivity(new Intent(this, ListActivity.class));
    }

    public void goToAccounts(View view) {
        startActivity(new Intent(this, ServiceChooserActivity.class));
    }

    public void downloadFile(View view) {
        new MEODownloadFile(MainActivity.this, new MEOCallback<FileResponse>() {

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

        new MEOGetMetadata(new MEOCallback<MEOMetadata>() {

            @Override
            public void onComplete(MEOCloudResponse<MEOMetadata> result) {
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

        new DropboxDownloadFile(MainActivity.this, DropboxClientFactory.getClient(), new DropboxDownloadFile.Callback() {

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
        new MEOSearchFile(new MEOCallback<List<MEOMetadata>>() {

            @Override
            public void onComplete(MEOCloudResponse<List<MEOMetadata>> result) {
                for (MEOMetadata m : result.getResponse()) {
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
        new MEODeleteFile(new MEOCallback<MEOMetadata>() {

            @Override
            public void onComplete(MEOCloudResponse<MEOMetadata> result) {
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
