package ipleiria.project.add.Dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

/**
 * Created by J on 03/04/2017.
 */

public class DropboxDeleteFile extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<Metadata> mCallback;
    private Exception mException;

    public DropboxDeleteFile(DbxClientV2 dbxClient, DropboxCallback<Metadata> callback) {
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
        try  {
            return mDbxClient.files().delete(remotePath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
