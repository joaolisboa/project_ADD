package ipleiria.project.add.Dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;

/**
 * Created by J on 03/04/2017.
 */

public class DropboxRevokeToken extends AsyncTask<String, Void, Void> {

    private final DbxClientV2 mDbxClient;
    private final DropboxRevokeToken.Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onComplete();
        void onError(Exception e);
    }

    public DropboxRevokeToken(DbxClientV2 dbxClient, DropboxRevokeToken.Callback callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete();
        }
    }

    @Override
    protected Void doInBackground(String... params) {
        try {
            mDbxClient.auth().tokenRevoke();
        } catch (DbxException e) {
            mException = e;
        }
        return null;
    }
}