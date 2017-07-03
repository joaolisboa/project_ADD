package ipleiria.project.add;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Lisboa on 08-May-17.
 */

public class Application extends android.app.Application {

    private static final String TAG = "Application";

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();

        /*if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);*/

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            // crashes in subsequent calls
            Log.d(TAG, e.getMessage());
        }

        mContext = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getAppContext() {
        return mContext;
    }

}
