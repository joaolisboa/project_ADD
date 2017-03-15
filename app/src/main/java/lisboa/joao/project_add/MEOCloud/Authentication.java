package lisboa.joao.project_add.MEOCloud;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dropbox.core.android.AuthActivity;

import java.util.Locale;

import static android.content.ContentValues.TAG;
import static android.content.Intent.EXTRA_UID;
import static com.dropbox.core.android.AuthActivity.AUTH_PATH_CONNECT;
import static com.dropbox.core.android.AuthActivity.EXTRA_ACCESS_SECRET;
import static com.dropbox.core.android.AuthActivity.EXTRA_ACCESS_TOKEN;
import static com.dropbox.core.android.AuthActivity.EXTRA_AUTH_STATE;

/**
 * Created by Lisboa on 15-Mar-17.
 */

public class Authentication extends Activity {
    private static final String TAG = AuthActivity.class.getName();

    public static final String EXTRA_CONSUMER_KEY = "CONSUMER_KEY";
    public static final String EXTRA_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String EXTRA_ACCESS_SECRET = "ACCESS_SECRET";
    public static final String EXTRA_UID = "UID";
    public static final String EXTRA_AUTH_STATE = "AUTH_STATE";

    /**
     * The path for a successful callback with token (not the initial auth request).
     */
    public static final String AUTH_PATH_CONNECT = "/connect";

    private static final String DEFAULT_WEB_HOST = "www.dropbox.com";

    // saved instance state keys
    private static final String SIS_KEY_AUTH_STATE_NONCE = "SIS_KEY_AUTH_STATE_NONCE";

    /** Used internally. */
    public static Intent result = null;

    // Temporary storage for parameters before Activity is created
    private static String sAppKey;
    private static String sWebHost = DEFAULT_WEB_HOST;
    private static String sApiType;
    private static String sDesiredUid;
    private static String[] sAlreadyAuthedUids;

    // These instance variables need not be stored in savedInstanceState as onNewIntent()
    // does not read them.
    private String mAppKey;
    private String mWebHost;
    private String mApiType;
    private String mDesiredUid;
    private String[] mAlreadyAuthedUids;

    // Stored in savedInstanceState to track an ongoing auth attempt, which
    // must include a locally-generated nonce in the response.
    private String mAuthStateNonce = null;

    private boolean mActivityDispatchHandlerPosted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAppKey = sAppKey;
        mWebHost = sWebHost;
        mApiType = sApiType;
        mDesiredUid = sDesiredUid;
        mAlreadyAuthedUids = sAlreadyAuthedUids;

        if (savedInstanceState == null) {
            result = null;
            mAuthStateNonce = null;
        } else {
            mAuthStateNonce = savedInstanceState.getString(SIS_KEY_AUTH_STATE_NONCE);
        }

        setTheme(android.R.style.Theme_Translucent_NoTitleBar);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFinishing()) {
            return;
        }

        if (mAuthStateNonce != null || mAppKey == null) {
            // We somehow returned to this activity without being forwarded
            // here by the official app.
            // Most commonly caused by user hitting "back" from the auth screen
            // or (if doing browser auth) task switching from auth task back to
            // this one.
            authFinished(null);
            return;
        }

        result = null;

        if (mActivityDispatchHandlerPosted) {
            Log.w(TAG, "onResume called again before Handler run");
            return;
        }

        // Random entropy passed through auth makes sure we don't accept a
        // response which didn't come from our request.  Each random
        // value is only ever used once.
        final String state = createStateNonce();

        /*
         * An Android bug exists where onResume may be called twice in rapid succession.
         * As mAuthNonceState would already be set at start of the second onResume, auth would fail.
         * Empirical research has found that posting the remainder of the auth logic to a handler
         * mitigates the issue by delaying remainder of auth logic to after the
         * previously posted onResume.
         */
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {

                Log.d(TAG, "running startActivity in handler");
                try {
                    startWebAuth(state);
                } catch (ActivityNotFoundException e) {
                    Log.e(TAG, "Could not launch intent. User may have restricted profile", e);
                    finish();
                    return;
                }
                // Save state that indicates we started a request, only after
                // we started one successfully.
                mAuthStateNonce = state;
                setAuthParams(null, null, null);
            }
        });

        mActivityDispatchHandlerPosted = true;
    }

    public static Intent makeIntent(Context context, String appKey, String webHost,
                                    String apiType) {
        if (appKey == null) throw new IllegalArgumentException("'appKey' can't be null");
        setAuthParams(appKey, null, null, webHost, apiType);
        return new Intent(context, AuthActivity.class);
    }

    static void setAuthParams(String appKey, String desiredUid,
                              String[] alreadyAuthedUids, String webHost, String apiType) {
        sAppKey = appKey;
        sDesiredUid = desiredUid;
        sAlreadyAuthedUids = (alreadyAuthedUids != null) ? alreadyAuthedUids : new String[0];
        sWebHost = (webHost != null) ? webHost : DEFAULT_WEB_HOST;
        sApiType = apiType;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // Reject attempt to finish authentication if we never started (nonce=null)
        if (null == mAuthStateNonce) {
            authFinished(null);
            return;
        }

        String token = null, secret = null, uid = null, state = null;

        if (intent.hasExtra(EXTRA_ACCESS_TOKEN)) {
            // Dropbox app auth.
            token = intent.getStringExtra(EXTRA_ACCESS_TOKEN);
            secret = intent.getStringExtra(EXTRA_ACCESS_SECRET);
            uid = intent.getStringExtra(EXTRA_UID);
            state = intent.getStringExtra(EXTRA_AUTH_STATE);
        } else {
            // Web auth.
            Uri uri = intent.getData();
            if (uri != null) {
                String path = uri.getPath();
                if (AUTH_PATH_CONNECT.equals(path)) {
                    try {
                        token = uri.getQueryParameter("oauth_token");
                        secret = uri.getQueryParameter("oauth_token_secret");
                        uid = uri.getQueryParameter("uid");
                        state = uri.getQueryParameter("state");
                    } catch (UnsupportedOperationException e) {}
                }
            }
        }

        Intent newResult;
        if (token != null && !token.equals("") &&
                (secret != null && !secret.equals("")) &&
                uid != null && !uid.equals("") &&
                state != null && !state.equals("")) {
            // Reject attempt to link if the nonce in the auth state doesn't match,
            // or if we never asked for auth at all.
            if (!mAuthStateNonce.equals(state)) {
                authFinished(null);
                return;
            }

            // Successful auth.
            newResult = new Intent();
            newResult.putExtra(EXTRA_ACCESS_TOKEN, token);
            newResult.putExtra(EXTRA_ACCESS_SECRET, secret);
            newResult.putExtra(EXTRA_UID, uid);
        } else {
            // Unsuccessful auth, or missing required parameters.
            newResult = null;
        }

        authFinished(newResult);
    }

    private void authFinished(Intent authResult) {
        result = authResult;
        mAuthStateNonce = null;
        setAuthParams(null, null, null);
        finish();
    }

    private void startWebAuth(String state) {
        String path = "1/connect";
        Locale locale = Locale.getDefault();

        // We use first alreadyAuthUid arbitrarily.
        // Note that the API treats alreadyAuthUid of 0 and not present equivalently.
        String alreadyAuthedUid = (mAlreadyAuthedUids.length > 0) ? mAlreadyAuthedUids[0] : "0";

        String[] params = {
                "k", mAppKey,
                "n", alreadyAuthedUid,
                "api", mApiType,
                "state", state};

        String url = DbxRequestUtil.buildUrlWithParams(locale.toString(), mWebHost, path, params);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(service.getAuthorizationUrl()));
        startActivity(intent);
    }
}


