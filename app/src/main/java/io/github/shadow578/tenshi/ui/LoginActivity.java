package io.github.shadow578.tenshi.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.databinding.ActivityLoginBinding;
import io.github.shadow578.tenshi.mal.AuthService;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.Urls;
import io.github.shadow578.tenshi.mal.model.Token;
import io.github.shadow578.tenshi.secret.Secrets;
import io.github.shadow578.tenshi.util.CustomTabsHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.concat;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;


/**
 * The activity that handles logging in
 */
public class LoginActivity extends TenshiActivity {
    private ActivityLoginBinding b;
    private String oauthState;
    private String verifierCode;
    private Token token;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // notify user if offline
        // TODO: maybe a bit more on the login activity...
        showSnackbarIfOffline(b.getRoot());

        // hide loading indicator
        b.loginLoadingIndicator.setVisibility(View.INVISIBLE);

        // generate verifier code
        verifierCode = MalApiHelper.getRandomCode(128);

        // generate OAUTH state
        oauthState = concat("TENSHI" + MalApiHelper.getRandomCode(64));

        // build login url
        final String loginUri = concat(Urls.OAUTH,
                "authorize?response_type=code&client_id=", Secrets.MAL_CLIENT_ID,
                "&code_challenge=", verifierCode,
                "&state=", oauthState);

        // set listener on login button
        b.loginBtn.setOnClickListener(view -> CustomTabsHelper.openInCustomTab(this, loginUri));

        // check if this is the login response
        with(getIntent().getData(), redirectUri -> {
            if (redirectUri.toString().startsWith(Urls.OAUTH_REDIRECT))
                getLoginData(redirectUri);
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // check if this is the login response
        with(intent.getData(), redirectUri -> {
            if (redirectUri.toString().startsWith(Urls.OAUTH_REDIRECT))
                getLoginData(redirectUri);
        });
    }

    /**
     * get the login token from the response url
     *
     * @param uri the response url from MAL
     */
    private void getLoginData(@NonNull Uri uri) {
        if (!uri.toString().startsWith(Urls.OAUTH_REDIRECT))
            return;

        // make progress bar visible
        b.loginLoadingIndicator.setVisibility(View.VISIBLE);

        // get code response and state from uri
        String code = uri.getQueryParameter("code");
        String recState = uri.getQueryParameter("state");
        if (!nullOrEmpty(code) && recState.equals(oauthState)) {
            AuthService loginService = MalApiHelper.createService(AuthService.class);
            Call<Token> loginCall = loginService.getAccessToken(Secrets.MAL_CLIENT_ID, code, verifierCode, "authorization_code");
            loginCall.enqueue(new Callback<Token>() {
                @Override
                @EverythingIsNonNull
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        //get token from response
                        token = response.body();

                        // save the token to prefs and re- init with the new token
                        TenshiApp.INSTANCE.setTokenAndTryAuthInit(token);

                        // open the main activity
                        Intent i = new Intent(LoginActivity.this, MainActivity.class);
                        startActivityForResult(i, MainActivity.REQUEST_LOGIN);
                    } else {
                        Log.w("Tenshi", "Token is null");
                        Snackbar.make(b.loginLayout, "Error: Token is null", Snackbar.LENGTH_SHORT).show();
                    }
                }

                @Override
                @EverythingIsNonNull
                public void onFailure(Call<Token> call, Throwable t) {
                    Log.w("Tenshi", t.toString());
                    Snackbar.make(b.loginLayout, R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                }
            });

        } else if (!nullOrEmpty(uri.getQueryParameter("error"))) {
            b.loginLoadingIndicator.setVisibility(View.INVISIBLE);
            Snackbar.make(b.loginLayout, R.string.login_snack_login_error, Snackbar.LENGTH_LONG).show();
        }
    }
}
