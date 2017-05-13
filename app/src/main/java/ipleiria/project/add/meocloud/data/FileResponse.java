package ipleiria.project.add.meocloud.data;

import android.support.annotation.NonNull;

import java.io.File;

import ipleiria.project.add.meocloud.ErrorMessageResponse;
import ipleiria.project.add.utils.HttpStatus;

/**
 * Created by J on 21/03/2017.
 */

public class FileResponse extends java.io.File implements ErrorMessageResponse {

    public FileResponse(@NonNull String pathname) {
        super(pathname);
    }

    public FileResponse(@NonNull File dir, String path){
        super(dir, path);
    }

    @Override
    public String processRequestCode(int code) {
        switch(code){
            case HttpStatus.NOT_FOUND: return "Ficheiro ou versão não encontrados";
            case HttpStatus.NOT_ACCEPTABLE: return "Parâmetros incorretos recebidos";
            default: return HttpStatus.processRequestCode(code);
        }
    }
}
