package ipleiria.project.add.Dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ipleiria.project.add.Utils.UriHelper;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<String, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, DbxClientV2 dbxClient, Callback callback) {
        mContext = context;
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        String localUri = params[0];
        File localFile = UriHelper.getFileForUri(mContext, Uri.parse(localUri));

        try {
            //return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
            //      .withMode(WriteMode.OVERWRITE)
            //    .uploadAndFinish(inputStream);
            return mDbxClient.files().upload("/test/hllo.txt").uploadAndFinish(new ByteArrayInputStream("hello".getBytes()));
        } catch (DbxException | IOException e) {
            mException = e;
        }

        if (localFile != null) {
            String remoteFolderPath = params[1];

            // Note - this is not ensuring the name is a valid dropbox file name
            String remoteFileName = localFile.getName();

            try (InputStream inputStream = new FileInputStream(localFile)) {
                //return mDbxClient.files().uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                  //      .withMode(WriteMode.OVERWRITE)
                    //    .uploadAndFinish(inputStream);
                return mDbxClient.files().upload("/test/hllo.txt").uploadAndFinish(new ByteArrayInputStream("hello".getBytes()));
            } catch (DbxException | IOException e) {
                mException = e;
            }
        }

        return null;
    }
}
