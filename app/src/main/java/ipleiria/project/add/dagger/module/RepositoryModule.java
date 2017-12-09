package ipleiria.project.add.dagger.module;

import dagger.Module;
import dagger.Provides;
import ipleiria.project.add.BuildConfig;
import ipleiria.project.add.dagger.scope.ApplicationScope;
import ipleiria.project.add.data.source.DropboxService;
import ipleiria.project.add.data.source.file.DummyFilesRepository;
import ipleiria.project.add.data.source.file.FilesDataSource;
import ipleiria.project.add.data.source.file.FilesRepository;
import ipleiria.project.add.data.source.MEOCloudService;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;

/**
 * Created by Lisboa on 24-Sep-17.
 */

@Module(includes = NetworkModule.class)
public class RepositoryModule {

    @Provides
    @ApplicationScope
    public UserService provideUserService(){
        return new UserService();
    }

    @Provides
    @ApplicationScope
    public FilesDataSource provideFilesRepository(DropboxService dropboxService, MEOCloudService meoCloudService){
        if(BuildConfig.DEMO_MODE){
            return new DummyFilesRepository();
        }
        return new FilesRepository(dropboxService, meoCloudService);
    }

    @Provides
    @ApplicationScope
    public ItemsRepository provideItemsRepository(UserService userService, FilesDataSource filesRepository){
        return new ItemsRepository(userService, filesRepository);
    }

    @Provides
    @ApplicationScope
    public MEOCloudService getMEOCloudService(UserService userService){
        return new MEOCloudService(userService.getMeoCloudToken());
    }

    @Provides
    @ApplicationScope
    public DropboxService getDropboxService(UserService userService){
        return new DropboxService(userService.getMeoCloudToken());
    }

}
