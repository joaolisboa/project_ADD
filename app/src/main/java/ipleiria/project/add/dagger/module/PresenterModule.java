package ipleiria.project.add.dagger.module;

import dagger.Module;
import dagger.Provides;
import ipleiria.project.add.dagger.scope.ControllerScope;
import ipleiria.project.add.view.home.HomePresenter;
import ipleiria.project.add.view.root.RootController;
import ipleiria.project.add.view.root.RootPresenter;

/**
 * Created by J on 12/09/2017.
 */

@Module
public class PresenterModule {

    @Provides
    @ControllerScope
    HomePresenter providehomePresenter(){
        return new HomePresenter();
    }

    @Provides
    @ControllerScope
    RootPresenter provideRootPresenter(){
        return new RootPresenter();
    }

}
