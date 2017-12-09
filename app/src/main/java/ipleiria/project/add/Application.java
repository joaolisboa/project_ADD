package ipleiria.project.add;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.lang.ref.WeakReference;

import io.fabric.sdk.android.Fabric;
import ipleiria.project.add.dagger.component.DaggerRepositoryComponent;
import ipleiria.project.add.dagger.component.RepositoryComponent;
import ipleiria.project.add.dagger.module.NetworkModule;
import ipleiria.project.add.dagger.module.RepositoryModule;

/**
 * Created by Lisboa on 08-May-17.
 */

public class Application extends android.app.Application {

    private static final String TAG = "Application";

    // avoid possible memory leak
    private static WeakReference<Context> context;
    public static RefWatcher refWatcher;

    private static RepositoryComponent repositoryComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        context = new WeakReference<Context>(this);
        repositoryComponent = createRepositoryComponent();

        if (BuildConfig.DEBUG) {
            if (LeakCanary.isInAnalyzerProcess(this)) {
                // This process is dedicated to LeakCanary for heap analysis.
                // You should not init your app in this process.
                return;
            }
            refWatcher = LeakCanary.install(this);
        }else{
            Fabric.with(this, new Crashlytics());
        }

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            // crashes in subsequent calls
            Log.d(TAG, e.getMessage());
        }
    }

    public static RepositoryComponent getRepositoryComponent() {
        return repositoryComponent;
    }

    protected RepositoryComponent createRepositoryComponent() {
        return DaggerRepositoryComponent.builder()
                .repositoryModule(new RepositoryModule())
                .networkModule(new NetworkModule())
                .build();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getAppContext() {
        return context.get();
    }

}
