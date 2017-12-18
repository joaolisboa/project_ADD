package ipleiria.project.add.dagger.component;

import dagger.Component;
import ipleiria.project.add.dagger.module.PresenterModule;
import ipleiria.project.add.dagger.scope.ControllerScope;
import ipleiria.project.add.view.home.HomeController;
import ipleiria.project.add.view.login.LoginController;
import ipleiria.project.add.view.root.RootController;
import ipleiria.project.add.view.settings.SettingsController;

/**
 * Created by J on 12/09/2017.
 */

@ControllerScope
@Component(dependencies = RepositoryComponent.class, modules = {PresenterModule.class})
public interface ControllerComponent {

    void inject(RootController controller);

    void inject(HomeController controller);

    void inject(LoginController controller);

    void inject(SettingsController controller);

}
