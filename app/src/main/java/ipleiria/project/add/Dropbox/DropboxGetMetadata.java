package ipleiria.project.add.Dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;

import java.io.IOException;
import java.io.InputStream;

import ipleiria.project.add.Utils.UriHelper;

/**
 * Created by J on 03/04/2017.
 */

public class DropboxGetMetadata extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final DropboxGetMetadata.Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onComplete(Metadata result);
        void onError(Exception e);
    }

    public DropboxGetMetadata(DbxClientV2 dbxClient, DropboxGetMetadata.Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onComplete(result);
        }
    }

    @Override
    protected Metadata doInBackground(String... params) {
        String remotePath = params[0];
        try {
            return mDbxClient.files().getMetadata(remotePath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
