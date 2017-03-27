package ipleiria.project.add.MEOCloud.Data;

import android.support.annotation.NonNull;

import ipleiria.project.add.MEOCloud.ErrorMessageResponse;
import ipleiria.project.add.Utils.HttpStatus;

/**
 * Created by J on 21/03/2017.
 */

public class File extends java.io.File implements ErrorMessageResponse {

    public File(@NonNull String pathname) {
        super(pathname);
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
