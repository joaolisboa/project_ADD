package ipleiria.project.add.MEOCloud;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.dropbox.core.android.FixedSecureRandom;

import java.security.SecureRandom;
import java.util.HashMap;

/**
 * Created by Lisboa on 15-Mar-17.
 */

public class MEOAuth extends Activity {
    private static final String TAG = MEOAuth.class.getName();

    public static final String EXTRA_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String EXTRA_TOKEN_EXPIRE = "TOKEN_EXPIRE";
    private static final String SIS_KEY_AUTH_STATE_NONCE = "SIS_KEY_AUTH_STATE_NONCE";

    public static Intent result = null;
    private String consumerKey;
    private static String sConsumerKey;
    ;

    // Stored in savedInstanceState to track an ongoing auth attempt, which
    // must include a locally-generated nonce in the response.
    private String mAuthStateNonce = null;

    private boolean mActivityDispatchHandlerPosted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        consumerKey = sConsumerKey;

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SIS_KEY_AUTH_STATE_NONCE, mAuthStateNonce);
    }

    public static Intent makeIntent(Context context, String consumerKey) {
        sConsumerKey = consumerKey;
        return new Intent(context, MEOAuth.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isFinishing()) {
            return;
        }

        if (mAuthStateNonce != null) {
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
            }
        });

        mActivityDispatchHandlerPosted = true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        if (null == mAuthStateNonce) {
            System.out.println("mAuthStateNonce = null, authfinished failed");
            authFinished(null);
            return;
        }

        String state = null, accessToken = null, expiresIn = null;

        // Web auth.
        Uri uri = intent.getData();
        if (uri != null) {
            System.out.println("Authorization response: ");
            System.out.println(uri.toString());
            String uriFragment = uri.getFragment();
            HashMap<String, String> map = new HashMap<>();
            for(String s: uriFragment.split("&")){
                String[] pair = s.split("=");
                map.put(pair[0], pair[1]);
            }

            accessToken = map.get("access_token");
            System.out.println("Access token: " + accessToken);
            expiresIn = map.get("expires_in");
            System.out.println("Expires in: " + expiresIn);
            state = map.get("state");
            System.out.println("State: " + state);
        }

        Intent newResult;
        if (accessToken != null && !accessToken.isEmpty()
                && state != null && !state.isEmpty()
                && expiresIn != null && !expiresIn.isEmpty()) {
            if (!mAuthStateNonce.equals(state)) {
                authFinished(null);
                Log.d(TAG, "state received different than sent - invalid auth");
                return;
            }

            // Successful auth.
            newResult = new Intent();
            newResult.putExtra(EXTRA_ACCESS_TOKEN, accessToken);
            newResult.putExtra(EXTRA_TOKEN_EXPIRE, expiresIn);
        } else {
            newResult = null;
            System.out.println("webauth result = null, authfinished failed");
        }

        authFinished(newResult);
    }

    private void authFinished(Intent authResult) {
        result = authResult;
        mAuthStateNonce = null;
        finish();
    }

    private void startWebAuth(String state) {
        String formattedUrl = String.format(MEOCloudAPI.AUTHORIZE_URL, consumerKey, state);
        System.out.println(formattedUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(formattedUrl));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private String createStateNonce() {
        final int NONCE_BYTES = 16; // 128 bits of randomness.
        byte randomBytes[] = new byte[NONCE_BYTES];
        getSecureRandom().nextBytes(randomBytes);
        StringBuilder sb = new StringBuilder();
        sb.append("oauth2:");
        for (int i = 0; i < NONCE_BYTES; ++i) {
            sb.append(String.format("%02x", (randomBytes[i] & 0xff)));
        }
        return sb.toString();
    }

    interface SecurityProvider {
        SecureRandom getSecureRandom();
    }

    // Class-level state used to replace the default SecureRandom implementation
    // if desired.
    private static SecurityProvider sSecurityProvider = new SecurityProvider() {
        @Override
        public SecureRandom getSecureRandom() {
            return FixedSecureRandom.get();
        }
    };
    private static final Object sSecurityProviderLock = new Object();

    private static SecureRandom getSecureRandom() {
        SecurityProvider prov = getSecurityProvider();
        if (null != prov) {
            return prov.getSecureRandom();
        }
        return new SecureRandom();
    }

    private static SecurityProvider getSecurityProvider() {
        synchronized (sSecurityProviderLock) {
            return sSecurityProvider;
        }
    }

}
