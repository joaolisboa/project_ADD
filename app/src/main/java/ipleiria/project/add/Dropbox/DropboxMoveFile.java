package ipleiria.project.add.Dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Lisboa on 19-Apr-17.
 */

public class DropboxMoveFile extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final DropboxMoveFile.Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onMoveComplete(Metadata result);
        void onError(Exception e);
    }

    public DropboxMoveFile(DbxClientV2 dbxClient, DropboxMoveFile.Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onMoveComplete(result);
        }
    }

    @Override
    protected Metadata doInBackground(String... params) {
        try {
            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }
            if (params[1].startsWith("/")) {
                params[1] = params[1].substring(1);
            }

            String fromPath = "/" + params[0];
            String toPath = "/" + params[1];

            return mDbxClient.files().move(fromPath, toPath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
