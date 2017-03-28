package ipleiria.project.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.util.List;

import ipleiria.project.add.MEOCloud.Data.File;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.GetMetadataTask;
import ipleiria.project.add.MEOCloud.MEODownloadFileTask;
import ipleiria.project.add.MEOCloud.SearchFileTask;

public class MainActivity extends AppCompatActivity {

    String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accessToken = getSharedPreferences("services", MODE_PRIVATE).getString("meo_access_token", "");

    }

    public void goToAccounts(View view){
        startActivity(new Intent(this, ServiceChooserActivity.class));
    }

    public void downloadFile(View view){
        new MEODownloadFileTask(MainActivity.this, new MEODownloadFileTask.Callback(){

            @Override
            public void onComplete(MEOCloudResponse<File> result) {
                if (result.responseSuccessful()) {
                    File file = result.getResponse();
                    System.out.println(file.getPath());
                    System.out.println(file.length());
                }else{
                    System.out.println(result.getError());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("DownloadError", e.getMessage(), e);
            }
        }).execute(accessToken, "exploring_luciddreaming.pdf");

        new GetMetadataTask(new GetMetadataTask.Callback(){

            @Override
            public void onComplete(MEOCloudResponse<Metadata> result) {
                if(result.responseSuccessful()){
                    System.out.println(result.getResponse().toJson());
                }else{
                    System.out.println(result.getError());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("MetadataError", e.getMessage(), e);
            }
        }).execute(accessToken, "/");

    }

    public void searchFile(View view){
        new SearchFileTask(new SearchFileTask.Callback(){

            @Override
            public void onComplete(MEOCloudResponse<List<Metadata>> result) {
                if(result.responseSuccessful()){
                    for(Metadata m: result.getResponse()){
                        System.out.println(m.toJson());
                    }
                }else{
                    System.out.println(result.getError());
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("SearchError", e.getMessage(), e);
            }
        }).execute(accessToken, "/", "lucid");
    }
}
