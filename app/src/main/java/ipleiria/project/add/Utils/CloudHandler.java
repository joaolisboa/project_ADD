package ipleiria.project.add.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;

import ipleiria.project.add.AddItemActivity;
import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxUploadFile;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolderTree;
import ipleiria.project.add.MEOCloud.Tasks.MEOUploadFile;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.ItemFile;

/**
 * Created by J on 24/04/2017.
 */

public class CloudHandler {

    public static void uploadFileToCloud(final Context context, final Uri uri, ItemFile file, final Criteria criteria) {
        final String remotePath = RemotePath.getRemoteFilePath(file, criteria);
        // the code below is absolutely atrocious
        // because MEOCloud doesn't create the directory to a file being uploaded
        // the folders need to be added manually and one... by one
        if(MEOCloudClient.isClientInitialized()) {
            // create folder for dimension
            new MEOCreateFolderTree(new MEOCallback<MEOMetadata>() {
                @Override
                public void onComplete(MEOMetadata result) {
                    new MEOUploadFile(context, new MEOCallback<MEOMetadata>() {

                        @Override
                        public void onComplete(MEOMetadata result) {
                            System.out.println("MEO Upload successful: " + result.getPath());
                        }

                        @Override
                        public void onRequestError(HttpErrorException httpE) {
                            Log.e("UploadError", httpE.getMessage(), httpE);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("UploadError", e.getMessage(), e);
                        }
                    }).execute(uri.toString(), remotePath);
                }
                @Override
                public void onRequestError(HttpErrorException httpE) {}
                @Override
                public void onError(Exception e) {}
            }).execute(String.valueOf(criteria.getDimension().getReference()),
                    String.valueOf(criteria.getArea().getReference()),
                    String.valueOf(criteria.getReference()));
        }
        if (DropboxClientFactory.isClientInitialized()) {
            new DropboxUploadFile(context, DropboxClientFactory.getClient(), new DropboxUploadFile.Callback() {

                @Override
                public void onUploadComplete(FileMetadata result) {
                    System.out.println("Dropbox Upload successful :" + result.getName());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("UploadDropError", e.getMessage(), e);
                }
            }).execute(uri.toString(), remotePath);
        }
    }

}
