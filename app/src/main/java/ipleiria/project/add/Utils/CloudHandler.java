package ipleiria.project.add.Utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.Arrays;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.Dropbox.DropboxMoveFile;
import ipleiria.project.add.Dropbox.DropboxUploadFile;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolder;
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolderTree;
import ipleiria.project.add.MEOCloud.Tasks.MEOMoveFile;
import ipleiria.project.add.MEOCloud.Tasks.MEOUploadFile;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.ItemFile;

import static ipleiria.project.add.Utils.PathUtils.TRASH_FOLDER;

/**
 * Created by J on 24/04/2017.
 */

public class CloudHandler {

    public static void uploadFileToCloud(final Context context, final Uri uri, ItemFile file, final Criteria criteria) {
        final String remotePath = PathUtils.getRemoteFilePath(file);
        // the code below is just dumb
        // because MEOCloud doesn't create the directory to a file being uploaded
        // the folders need to be added manually and one... by one
        if (MEOCloudClient.isClientInitialized()) {
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
                public void onRequestError(HttpErrorException httpE) {
                }

                @Override
                public void onError(Exception e) {
                }
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

    public static void moveFileMEO(String from, String to) {
        new MEOMoveFile(new MEOCallback<MEOMetadata>() {
            @Override
            public void onComplete(MEOMetadata result) {
                System.out.println("successfully moved file: " + result.getPath());
            }

            @Override
            public void onRequestError(HttpErrorException httpE) {
                Log.e("MoveFile", httpE.getMessage(), httpE);
            }

            @Override
            public void onError(Exception e) {
                Log.e("MoveFile", e.getMessage(), e);
            }
        }).execute(from, to);
    }

    public static void moveFileDropbox(String from, String to) {
        new DropboxMoveFile(DropboxClientFactory.getClient(), new DropboxMoveFile.Callback() {

            @Override
            public void onMoveComplete(Metadata result) {
                System.out.println("successfully moved file: " + result.getPathLower());
            }

            @Override
            public void onError(Exception e) {
                Log.e("MoveFile", e.getMessage(), e);
            }
        }).execute(from, to);
    }

    public static void uploadFileToCloud(final Context context, final File file) {
        final String remotePath = PathUtils.getLocalRelativePath(context, file);
        System.out.println("relative path to appDir = " + remotePath);
        String[] splitPath = remotePath.substring(1, remotePath.lastIndexOf("/")).split("/");
        System.out.println(Arrays.toString(splitPath));
        String dimensionPath = splitPath[0];
        String areaPath = splitPath[1];
        String criteriaPath = splitPath[2];
        System.out.println("dimension path " + dimensionPath);
        System.out.println("area path " + areaPath);
        System.out.println("criteria path " + criteriaPath);

        final Uri uri = Uri.fromFile(file);
        // TODO: 27-Apr-17  delete local file if it uploaded successfully to both services
        if (MEOCloudClient.isClientInitialized()) {
            // create folder for dimension
            new MEOCreateFolderTree(new MEOCallback<MEOMetadata>() {
                @Override
                public void onComplete(MEOMetadata result) {
                    new MEOUploadFile(context, new MEOCallback<MEOMetadata>() {

                        @Override
                        public void onComplete(MEOMetadata result) {
                            System.out.println("MEO Upload successful: " + result.getPath());
                            // delete local ile if other service is not connected
                            if (!DropboxClientFactory.isClientInitialized()) {
                                file.delete();
                            }
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
                public void onRequestError(HttpErrorException httpE) {
                }

                @Override
                public void onError(Exception e) {
                }
            }).execute(dimensionPath, areaPath, criteriaPath);
        }
        if (DropboxClientFactory.isClientInitialized()) {
            new DropboxUploadFile(context, DropboxClientFactory.getClient(), new DropboxUploadFile.Callback() {

                @Override
                public void onUploadComplete(FileMetadata result) {
                    System.out.println("Dropbox Upload successful :" + result.getName());
                    // delete local ile if other service is not connected
                    if (!MEOCloudClient.isClientInitialized()) {
                        file.delete();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("UploadDropError", e.getMessage(), e);
                }
            }).execute(uri.toString(), remotePath);
        }
    }

    public static void uploadFileToCloudTrash(final Context context, final File file) {
        final Uri uri = Uri.fromFile(file);
        final String remotePath = TRASH_FOLDER + "/" + file.getName();
        if(MEOCloudClient.isClientInitialized()){
            new MEOCreateFolder(new MEOCallback<MEOMetadata>() {
                @Override
                public void onComplete(MEOMetadata result) {
                    new MEOUploadFile(context, new MEOCallback<MEOMetadata>() {

                        @Override
                        public void onComplete(MEOMetadata result) {
                            System.out.println("MEO Upload successful: " + result.getPath());
                            // delete local ile if other service is not connected
                            if (!DropboxClientFactory.isClientInitialized()) {
                                file.delete();
                            }
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
                public void onRequestError(HttpErrorException httpE) {
                    Log.e("UploadDropError", httpE.getMessage(), httpE);
                }

                @Override
                public void onError(Exception e) {
                    Log.e("UploadDropError", e.getMessage(), e);
                }
            }).execute(TRASH_FOLDER);
        }
        if(DropboxClientFactory.isClientInitialized()){
            new DropboxUploadFile(context, DropboxClientFactory.getClient(), new DropboxUploadFile.Callback() {

                @Override
                public void onUploadComplete(FileMetadata result) {
                    System.out.println("Dropbox Upload successful :" + result.getName());
                    // delete local ile if other service is not connected
                    if (!MEOCloudClient.isClientInitialized()) {
                        file.delete();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("UploadDropError", e.getMessage(), e);
                }
            }).execute(uri.toString(), remotePath);
        }
    }
}

