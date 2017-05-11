package ipleiria.project.add.dropbox;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.FullAccount;

/**
 * Async task for getting user account info
 */
public class DropboxGetAccount extends AsyncTask<Void, Void, FullAccount> {

    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<FullAccount> mCallback;
    private Exception mException;

    public DropboxGetAccount(DbxClientV2 dbxClient, DropboxCallback<FullAccount> callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(FullAccount account) {
        super.onPostExecute(account);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(account);
        }
    }

    @Override
    protected FullAccount doInBackground(Void... params) {

        try {
            return mDbxClient.users().getCurrentAccount();

        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
