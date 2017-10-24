package ipleiria.project.add.dagger.component;

import dagger.Component;
import ipleiria.project.add.dagger.module.ApplicationModule;
import ipleiria.project.add.dagger.scope.ApplicationScope;
import ipleiria.project.add.view.main.MainActivity;

/**
 * Created by J on 12/09/2017.
 */

@ApplicationScope
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(MainActivity mainActivity);

}
