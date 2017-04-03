package ipleiria.project.add.Dropbox;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ipleiria.project.add.Utils.UriHelper;

/**
 * Async task to upload a file to a directory
 */
public class DropboxUploadFile extends AsyncTask<String, Void, FileMetadata> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onUploadComplete(FileMetadata result);

        void onError(Exception e);
    }

    public DropboxUploadFile(Context context, DbxClientV2 dbxClient, Callback callback) {
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
        Uri localUri = Uri.parse(params[0]);

        try (InputStream is = mContext.getContentResolver().openInputStream(localUri)) {
            return mDbxClient.files().upload("/" + UriHelper.getFileName(mContext, localUri)).uploadAndFinish(is);
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
