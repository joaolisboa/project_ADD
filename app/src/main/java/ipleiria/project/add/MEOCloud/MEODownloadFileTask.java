package ipleiria.project.add.MEOCloud;

import android.content.Context;
import android.os.AsyncTask;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import ipleiria.project.add.MEOCloud.Data.File;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MEODownloadFileTask extends AsyncTask<String, Void, MEOCloudResponse<File>> {

    private final Callback callback;
    private Exception exception;
    private Context context;

    public interface Callback {
        void onComplete(MEOCloudResponse<File> result);
        void onError(Exception e);
    }

    public MEODownloadFileTask(Context context, Callback callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<File> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(result);
        }
    }

    @Override
    protected MEOCloudResponse<File> doInBackground(String... params) {
        try {
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingAccessTokenException();
            }if(params[1] == null || params[1].isEmpty()){
                throw new MissingFilePathException();
            }

            if (params[1].startsWith("/")) {
                params[1] = params[1].substring(1);
            }

            String path = MEOCloudAPI.API_METHOD_FILES + "/" + MEOCloudAPI.API_MODE + "/" + params[1];
            System.out.println(path);

            Response response = HttpRequestor.getContent(params[0], path, null);
            if (response != null) {
                MEOCloudResponse<File> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    InputStream is = response.body().byteStream();
                    //File tempFile = new File(UUID.randomUUID().toString());
                    FileOutputStream fos = context.openFileOutput(params[1], Context.MODE_PRIVATE);
                    byte[] buffer = new byte[1024 * 100];
                    int nBytes;
                    while((nBytes = is.read(buffer)) != -1){
                        fos.write(buffer, 0, nBytes);
                        fos.flush();
                    }
                    is.close();
                    fos.close();
                    String downloadPath = context.getFilesDir().getAbsolutePath() + "/" + params[1];
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