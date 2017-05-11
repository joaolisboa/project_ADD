package ipleiria.project.add.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by J on 21/03/2017.
 */

public abstract class JsonObject {

    public static <T> T fromJson(String json, Class<T> clazz){
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(json, clazz);
    }

    public String toJson(){
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

}
