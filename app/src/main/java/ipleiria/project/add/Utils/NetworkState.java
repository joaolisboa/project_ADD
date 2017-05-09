package ipleiria.project.add.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import ipleiria.project.add.Application;

/**
 * Created by Lisboa on 16-Apr-17.
 */

public class NetworkState {

    public static boolean isOnline(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // test isOnline with AppContext
    public static boolean isOnline(){
        ConnectivityManager cm = (ConnectivityManager) Application.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
