package ipleiria.project.add.dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import ipleiria.project.add.Application;
import ipleiria.project.add.utils.PathUtils;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
public class DropboxDownloadFile extends AsyncTask<String, Void, File> {

    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<File> mCallback;
    private Exception mException;

    public DropboxDownloadFile(DbxClientV2 dbxClient, DropboxCallback<File> callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(result);
        }
    }

    @Override
    protected File doInBackground(String... params) {
        try {
            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }


            String path = params[0];

            File dFile;
            if(params.length > 1 && params[1] != null){
                if (params[1].startsWith("/")) {
                    params[1] = params[1].substring(1);
                }
                dFile = new File(Application.getAppContext().getFilesDir(), params[1]);
            }else{
                dFile = new File(Application.getAppContext().getFilesDir(), path);
            }

            dFile.getParentFile().mkdirs();
            try (OutputStream outputStream = new FileOutputStream(dFile)) {
                mDbxClient.files().download("/" + path).download(outputStream);
            }

            return dFile;
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
