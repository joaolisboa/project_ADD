package ipleiria.project.add.dagger.module;

import dagger.Module;
import dagger.Provides;
import ipleiria.project.add.dagger.scope.ControllerScope;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.view.home.HomePresenter;
import ipleiria.project.add.view.login.LoginPresenter;
import ipleiria.project.add.view.root.RootController;
import ipleiria.project.add.view.root.RootPresenter;

/**
 * Created by J on 12/09/2017.
 */

@Module()
public class PresenterModule {

    @Provides
    @ControllerScope
    RootPresenter provideRootPresenter(){
        return new RootPresenter();
    }

    @Provides
    @ControllerScope
    HomePresenter providehomePresenter(UserService userService, ItemsRepository itemsRepository, FilesRepository filesRepository){
        return new HomePresenter(userService, itemsRepository, filesRepository);
    }

    @Provides
    @ControllerScope
    LoginPresenter provideLoginPresenter(UserService userService){
        return new LoginPresenter(userService);
    }

}
