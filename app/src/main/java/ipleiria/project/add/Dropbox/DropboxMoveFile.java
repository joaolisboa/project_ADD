package ipleiria.project.add.Dropbox;

import android.os.AsyncTask;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.GetMetadataErrorException;
import com.dropbox.core.v2.files.Metadata;
import com.dropbox.core.v2.files.RelocationErrorException;

/**
 * Created by Lisboa on 19-Apr-17.
 */

public class DropboxMoveFile extends AsyncTask<String, Void, Metadata> {

    private final DbxClientV2 mDbxClient;
    private final DropboxCallback<Metadata> mCallback;
    private Exception mException;

    public DropboxMoveFile(DbxClientV2 dbxClient, DropboxCallback<Metadata> callback) {
        mDbxClient = dbxClient;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Metadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(result);
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

            // if file with same name already exists in destination delete the source
            try{
                // should give error if file doesn't exist in destination
                if(mDbxClient.files().getMetadata(toPath) != null){
                    if(fromPath.substring(fromPath.lastIndexOf("/") + 1, fromPath.length()).equals(
                            toPath.substring(toPath.lastIndexOf("/") + 1, toPath.length()))){
                        mDbxClient.files().delete(fromPath);
                    }
                }
            }catch(RelocationErrorException ex){
                Log.e("MOVE_FILE_DROPBOX", ex.getMessage(), ex);
            }catch(GetMetadataErrorException metaEx){
                // file wasn't found so it can move to destination
                Log.e("MOVE_FILE_DROPBOX", metaEx.getMessage(), metaEx);
            }

            return mDbxClient.files().move(fromPath, toPath);
        } catch (DbxException e) {
            mException = e;
        }

        return null;
    }
}
