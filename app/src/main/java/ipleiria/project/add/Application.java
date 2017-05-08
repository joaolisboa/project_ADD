package ipleiria.project.add;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * Created by Lisboa on 08-May-17.
 */

public class Application extends android.app.Application {

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return mContext;
    }

}
