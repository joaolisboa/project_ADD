package ipleiria.project.add.MEOCloud.Tasks;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Utils.HttpStatus;
import ipleiria.project.add.Utils.RemotePath;
import okhttp3.Response;

/**
 * Created by Lisboa on 24-Apr-17.
 */

public class MEOGetThumbnail extends AsyncTask<String, Void, MEOCloudResponse<File>> {

    private final MEOCallback<File> callback;
    private Exception exception;
    private Context context;

    public MEOGetThumbnail(Context context, MEOCallback<File> callback) {
        this.context = context;
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<File> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result.getResponse());
            }else{
                callback.onRequestError(new HttpErrorException(result.getError()));
            }
        }
    }

    @Override
    protected MEOCloudResponse<File> doInBackground(String... params) {
        try {
            if (params == null) {
                throw new MissingParametersException();
            } else if (params[0] == null || params[0].isEmpty()) {
                throw new MissingFilePathException();
            }

            if (params[0].startsWith("/")) {
                params[0] = params[0].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String remoteFilePath = params[0];
            String thumbnailFilename = "thumb_" + RemotePath.filename(remoteFilePath);

            HashMap<String, String> map = new HashMap<>();
            if (params.length > 1 && params[1] != null) {
                map.put("format", params[1]);
            }
            if (params.length > 2 && params[2] != null) {
                map.put("size", params[2]);
            }
            if (params.length > 3 && params[3] != null) {
                map.put("crop", params[3]);
            }

            String path = MEOCloudAPI.API_METHOD_THUMBNAILS + "/" + MEOCloudAPI.API_MODE + "/" + remoteFilePath;
            System.out.println(path);
            Response response = HttpRequestor.getContent(token, path, map);
            if (response != null) {
                MEOCloudResponse<File> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    InputStream is = response.body().byteStream();
                    FileOutputStream fos = context.openFileOutput(thumbnailFilename, Context.MODE_PRIVATE);
                    byte[] buffer = new byte[1024 * 100];
                    int nBytes;
                    while((nBytes = is.read(buffer)) != -1){
                        fos.write(buffer, 0, nBytes);
                        fos.flush();
                    }
                    is.close();
                    fos.close();
                    String downloadPath = context.getFilesDir().getAbsolutePath() + "/" + thumbnailFilename;
                    meoCloudResponse.setResponse(new File(downloadPath));
                }
                return meoCloudResponse;
            }
            return null;
        } catch (IOException
                | MissingParametersException
                | MissingFilePathException
                | MissingAccessTokenException e) {
            exception = e;
        }
        return null;
    }
}