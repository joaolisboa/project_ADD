package ipleiria.project.add.dropbox;

import android.content.Context;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.ThumbnailFormat;
import com.dropbox.core.v2.files.ThumbnailSize;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import ipleiria.project.add.Application;
import ipleiria.project.add.utils.PathUtils;

/**
 * Created by J on 24/04/2017.
 */

public class DropboxGetThumbnail  extends AsyncTask<String, Void, File> {

    private final Context mContext;
    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<File> mCallback;
    private Exception mException;

    public DropboxGetThumbnail(Context context, DbxClientV2 dbxClient, DropboxCallback<File> callback) {
        mContext = context;
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

            String filename = params[0];
            String thumbnailFilename = "thumb_" + PathUtils.filename(filename);
            try (OutputStream outputStream = Application.getAppContext().openFileOutput(thumbnailFilename, Context.MODE_PRIVATE)) {
                mDbxClient.files().getThumbnailBuilder("/" + filename)
                        .withFormat(ThumbnailFormat.JPEG)
                        .withSize(ThumbnailSize.W128H128)
                        .download(outputStream);
            }

            return new File(Application.getAppContext().getFilesDir().getAbsolutePath() + "/" + thumbnailFilename);
        } catch (DbxException | IOException e) {
            mException = e;
        }

        return null;
    }
}
