package io.github.shadow578.tenshi.ui.onboarding;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import io.github.shadow578.tenshi.BuildConfig;
import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.databinding.FragmentLoginBinding;
import io.github.shadow578.tenshi.extensionslib.lang.Consumer;
import io.github.shadow578.tenshi.mal.AuthService;
import io.github.shadow578.tenshi.mal.MalApiHelper;
import io.github.shadow578.tenshi.mal.Urls;
import io.github.shadow578.tenshi.mal.model.Token;
import io.github.shadow578.tenshi.ui.fragments.TenshiFragment;
import io.github.shadow578.tenshi.util.CustomTabsHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.concat;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

public class LoginFragment extends TenshiFragment {
    @Nullable
    private Consumer<Boolean> loginListener;

    private FragmentLoginBinding b;
    private String oauthState;
    private String verifierCode;
    private Token token;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentLoginBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // notify user if offline
        // TODO: maybe a bit more on the login activity...
        //showSnackbarIfOffline(b.getRoot());

        // hide loading indicator
        b.loginLoadingIndicator.setVisibility(View.INVISIBLE);

        // set listener on login button
        b.loginBtn.setOnClickListener(view -> {
            // generate OAUTH state
            oauthState = concat("TENSHI" + MalApiHelper.getRandomCode(128 - 6));

            // generate verifier code
            verifierCode = MalApiHelper.getRandomCode(128);

            // build login url
            final String loginUri = concat(Urls.OAUTH, "authorize?",
                    "response_type=code",
                    "&client_id=", BuildConfig.MAL_CLIENT_ID,
                    "&state=", oauthState,
                    "&redirect_uri=", urlEncode(BuildConfig.MAL_OAUTH_REDIRECT_URL),
                    "&code_challenge=", verifierCode,
                    "&code_challenge_method=plain");

            // open login form
            CustomTabsHelper.openInCustomTab(requireActivity(), loginUri);
        });
    }

    /**
     * call this from the host activity's onNewIntent
     *
     * @param intent the new intent
     */
    public void onNewIntent(Intent intent) {
        // check if this is the login response
        with(intent.getData(), redirectUri -> {
            if (redirectUri.toString().startsWith(BuildConfig.MAL_OAUTH_REDIRECT_URL))
                getLoginData(redirectUri);
        });
    }

    /**
     * set a listener for when login finished
     *
     * @param listener the listener to set
     */
    public void setLoginListener(@Nullable Consumer<Boolean> listener) {
        loginListener = listener;
    }

    /**
     * get the login token from the response url
     *
     * @param uri the response url from MAL
     */
    private void getLoginData(@NonNull Uri uri) {
        if (!uri.toString().startsWith(BuildConfig.MAL_OAUTH_REDIRECT_URL))
            return;

        // make progress bar visible
        b.loginLoadingIndicator.setVisibility(View.VISIBLE);

        // get code response and state from uri
        String code = uri.getQueryParameter("code");
        String recState = uri.getQueryParameter("state");
        if (!nullOrEmpty(code) && recState.equals(oauthState)) {
            final AuthService loginService = MalApiHelper.createService(AuthService.class);
            loginService.getAccessToken(BuildConfig.MAL_CLIENT_ID, code, verifierCode, "authorization_code", BuildConfig.MAL_OAUTH_REDIRECT_URL).enqueue(new Callback<Token>() {
                @Override
                @EverythingIsNonNull
                public void onResponse(Call<Token> call, Response<Token> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        //get token from response
                        token = response.body();

                        // save the token to prefs and re- init with the new token
                        TenshiApp.INSTANCE.setTokenAndTryAuthInit(token);

                        // call listener
                        with(loginListener, l -> l.invoke(true));
                    } else {
                        Log.w("Tenshi", "Token is null");
                        Snackbar.make(b.loginLayout, "Error: Token is null", Snackbar.LENGTH_SHORT).show();
                        with(loginListener, l -> l.invoke(false));
                    }
                }

                @Override
                @EverythingIsNonNull
                public void onFailure(Call<Token> call, Throwable t) {
                    Log.w("Tenshi", t.toString());
                    Snackbar.make(b.loginLayout, R.string.shared_snack_server_connect_error, Snackbar.LENGTH_SHORT).show();
                    with(loginListener, l -> l.invoke(false));
                }
            });

        } else if (!nullOrEmpty(uri.getQueryParameter("error"))) {
            b.loginLoadingIndicator.setVisibility(View.INVISIBLE);
            Snackbar.make(b.loginLayout, R.string.login_snack_login_error, Snackbar.LENGTH_LONG).show();
            with(loginListener, l -> l.invoke(false));
        }
    }

    /**
     * url- encode a string
     *
     * @param input the string to encode
     * @return the encoded string, or a empty string if encode failed
     */
    @NonNull
    private String urlEncode(@SuppressWarnings("SameParameterValue") @NonNull String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
