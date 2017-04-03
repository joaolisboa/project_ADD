package ipleiria.project.add.Dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.sharing.FileMemberRemoveActionResult;

import java.io.IOException;
import java.io.InputStream;

import ipleiria.project.add.Utils.UriHelper;

/**
 * Created by J on 03/04/2017.
 */

public class DropboxDeleteFile extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final DropboxDeleteFile.Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(Metadata result);
        void onError(Exception e);
    }

    public DropboxDeleteFile(DbxClientV2 dbxClient, DropboxDeleteFile.Callback callback) {
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
            mCallback.onUploadComplete(result);
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
