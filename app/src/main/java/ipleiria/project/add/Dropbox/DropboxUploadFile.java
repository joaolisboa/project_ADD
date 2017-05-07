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

/**
 * Async task to upload a file to a directory
 */
public class DropboxUploadFile extends AsyncTask<String, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<FileMetadata> mCallback;
    private Exception mException;

    public DropboxUploadFile(Context context, DbxClientV2 dbxClient, DropboxCallback<FileMetadata> callback) {
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
            mCallback.onComplete(result);
        }
    }

    @Override
    protected FileMetadata doInBackground(String... params) {
        Uri localUri = Uri.parse(params[0]);

        if (params[1].startsWith("/")) {
            params[1] = params[1].substring(1);
        }
        String remoteFilePath = params[1];

        try (InputStream is = mContext.getContentResolver().openInputStream(localUri)) {
            return mDbxClient.files().upload("/" + remoteFilePath).uploadAndFinish(is);
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
