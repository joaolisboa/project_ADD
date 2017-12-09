package ipleiria.project.add.dagger.component;

import dagger.Component;
import ipleiria.project.add.dagger.module.RepositoryModule;
import ipleiria.project.add.dagger.scope.ApplicationScope;
import ipleiria.project.add.data.source.file.FilesDataSource;
import ipleiria.project.add.data.source.file.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;

/**
 * Created by Lisboa on 24-Sep-17.
 */

@ApplicationScope
@Component(modules = {RepositoryModule.class})
public interface RepositoryComponent {

    UserService getUserService();

    FilesDataSource getFilesRepository();

    ItemsRepository getItemsRepository();

}
