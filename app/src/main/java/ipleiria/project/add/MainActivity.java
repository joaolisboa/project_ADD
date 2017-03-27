package ipleiria.project.add;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ipleiria.project.add.MEOCloud.Data.File;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.GetMetadataInfoTask;
import ipleiria.project.add.MEOCloud.MEODownloadFileTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void goToAccounts(View view){
        startActivity(new Intent(this, ServiceChooserActivity.class));
    }

    public void downloadFile(View view){
        String accessToken = getSharedPreferences("services", MODE_PRIVATE).getString("meo_access_token", "");
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
                System.out.println(e.getMessage());
            }
        }).execute(accessToken, "exploring_luciddreaming.pdf");

        new GetMetadataInfoTask(new GetMetadataInfoTask.Callback(){

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
                System.out.println(e.getMessage());
            }
        }).execute(accessToken, "/");

    }
}
