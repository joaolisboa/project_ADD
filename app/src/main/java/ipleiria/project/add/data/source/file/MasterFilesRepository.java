package ipleiria.project.add.data.source.file;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ipleiria.project.add.Application;

/**
 * Created by Lisboa on 09-Dec-17.
 */

public abstract class MasterFilesRepository implements FilesDataSource{

    private static final String TAG = "MASTER_FILES_REPO";

    static final String APP_DIR = Application.getAppContext().getFilesDir().getAbsolutePath();
    static final String TRASH_PATH = "/trash";
    static final String THUMBNAIL_PREFIX = "/thumb_";
    static final String PENDING_PATH = "";

    public File createImageFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date());
            String imageFileName = timeStamp + "_";
            File storageDir = Application.getAppContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(
                    imageFileName, /* prefix */
                    ".jpg",  /* suffix */
                    storageDir     /* directory */
            );
        } catch (IOException e) {
            Log.d(TAG, "createImageFile: " + e);
        }

        return null;
    }

}
