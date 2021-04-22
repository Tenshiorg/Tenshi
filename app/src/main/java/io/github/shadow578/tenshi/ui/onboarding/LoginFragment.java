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
import io.github.shadow578.tenshi.mal.AuthHelper;
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

    /**
     * use code_challenge_method=plain? or S256?
     *
     * MAL currently only supports plain, but for the future, we are safe
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean USE_PLAIN_CODE_CHALLENGE = true;

    @Nullable
    private Consumer<Boolean> loginListener;

    private FragmentLoginBinding b;
    private Token token;
    private AuthHelper.PKCECodes pkce;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentLoginBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // hide loading indicator
        b.loginLoadingIndicator.setVisibility(View.INVISIBLE);

        // set listener on login button
        b.loginBtn.setOnClickListener(view -> openLoginPage());
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
     * open the OAUTH login page in a custom tab
     */
    private void openLoginPage() {
        // generate pcke codes
        initPCKECodes();

        // build login url
        final String loginUri = concat(Urls.OAUTH, "authorize?",
                "response_type=code",
                "&client_id=", BuildConfig.MAL_CLIENT_ID,
                "&state=", pkce.state,
                "&redirect_uri=", urlEncode(BuildConfig.MAL_OAUTH_REDIRECT_URL),
                "&code_challenge=", pkce.challenge,
                "&code_challenge_method=", pkce.challengeMethod,
                "&force_logout=1");

        // open login form
        CustomTabsHelper.openInCustomTab(requireActivity(), loginUri);
    }

    /**
     * get the login token from the response url
     *
     * @param uri the response url from MAL
     */
    private void getLoginData(@NonNull Uri uri) {
        // check redirect url is valid
        if (!uri.toString().startsWith(BuildConfig.MAL_OAUTH_REDIRECT_URL))
            return;

        // make progress bar visible
        b.loginLoadingIndicator.setVisibility(View.VISIBLE);

        // get code response and state from uri
        // and send token request
        String code = uri.getQueryParameter("code");
        String state = uri.getQueryParameter("state");
        if (!nullOrEmpty(code) && pkce.state.equals(state)) {
            MalApiHelper.createService(AuthService.class)
                    .getAccessToken(BuildConfig.MAL_CLIENT_ID, code, pkce.verifier, "authorization_code", BuildConfig.MAL_OAUTH_REDIRECT_URL)
                    .enqueue(new Callback<Token>() {
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

    /**
     * initialize the {@see pckeCodes} variable
     */
    private void initPCKECodes() {
        if (USE_PLAIN_CODE_CHALLENGE)
            pkce = AuthHelper.generatePlain();
        else
            pkce = AuthHelper.generateS256();
    }
}
