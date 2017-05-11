package ipleiria.project.add.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ListFolderResult;

/**
 * Created by J on 03/04/2017.
 */

public class DropboxListFolder  extends AsyncTask<String, Void, ListFolderResult> {

    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<ListFolderResult> mCallback;
    private Exception mException;

    public DropboxListFolder(DbxClientV2 dbxClient, DropboxCallback<ListFolderResult> callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(ListFolderResult result) {
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
    protected ListFolderResult doInBackground(String... params) {
        String remotePath = params[0];
        try {
            return mDbxClient.files().listFolder(remotePath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
