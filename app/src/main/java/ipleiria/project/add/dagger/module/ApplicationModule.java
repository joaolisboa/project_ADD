package ipleiria.project.add.dagger.module;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import ipleiria.project.add.dagger.scope.ApplicationScope;

/**
 * Created by J on 12/09/2017.
 */

@Module
public final class ApplicationModule {

    private Context context;

    public ApplicationModule(Context context) {
        this.context = context;
    }

    @Provides
    @ApplicationScope
    public Context provideContext() {
        return context;
    }

}
