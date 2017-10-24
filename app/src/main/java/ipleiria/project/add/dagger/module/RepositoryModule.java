package ipleiria.project.add.dagger.module;

import dagger.Module;
import dagger.Provides;
import ipleiria.project.add.dagger.scope.ApplicationScope;
import ipleiria.project.add.data.source.DropboxService;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.MEOCloudService;
import ipleiria.project.add.data.source.UserService;

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
    public FilesRepository provideFilesRepository(UserService userService, DropboxService dropboxService, MEOCloudService meoCloudService){
        return new FilesRepository(userService, dropboxService, meoCloudService);
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
