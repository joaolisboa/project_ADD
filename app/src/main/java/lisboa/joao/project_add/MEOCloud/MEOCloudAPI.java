package lisboa.joao.project_add.MEOCloud;

import com.github.scribejava.core.builder.api.DefaultApi20;

/**
 * Created by Lisboa on 14-Mar-17.
 */

public class MEOCloudAPI extends DefaultApi20 {

    private String consumerKey;

    private static class InstanceHolder {
        private static final MEOCloudAPI INSTANCE = new MEOCloudAPI();
    }

    public static MEOCloudAPI instance() {
        return InstanceHolder.INSTANCE;
    }

    public static final String AUTHORIZE_URL = "https://meocloud.pt/oauth2/authorize?client_id=%s&response_type=code";
    public static final String ACCESS_URL = "https://meocloud.pt/oauth2/token";

    @Override
    public String getAccessTokenEndpoint() {
        return ACCESS_URL;
    }

    @Override
    public String getAuthorizationBaseUrl(){
        return String.format(AUTHORIZE_URL, consumerKey);
    }

    public void setConsumerKey(String consumerKey){
        this.consumerKey = consumerKey;
    }



}
