package io.github.shadow578.tenshi.ui.oobe;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.databinding.FragmentOnlinecheckBinding;
import io.github.shadow578.tenshi.mal.MalService;
import io.github.shadow578.tenshi.mal.Urls;
import io.github.shadow578.tenshi.mal.model.User;
import io.github.shadow578.tenshi.util.Util;
import io.github.shadow578.tenshi.util.converter.RetrofitEnumConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.internal.EverythingIsNonNull;

/**
 * does a simple online check for the device with a (unauthenticated) test request to MAL apis
 * to ensure the API works.
 * <p>
 * Errors are shown to the user, with a option to retry the test.
 * calls OnSuccess or OnFail listeners accordingly
 */
public class OnlineCheckFragment extends OnboardingFragment {

    /**
     * experimental api check
     */
    private static final boolean SKIP_API_CHECK = false;

    private FragmentOnlinecheckBinding b;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        b = FragmentOnlinecheckBinding.inflate(inflater, container, false);
        return b.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // setup retry button
        b.retryBtn.setOnClickListener(v1 -> checkConnection());

        // check connection now
        checkConnection();
    }

    /**
     * check the connection using {@link Util#getConnectionType(Context)} and {@link MalService} dummy call
     * calls {@link #checkOk()} or {@link #checkFail()}
     */
    private void checkConnection() {
        // prepare ui
        b.loadingIndicator.setVisibility(View.VISIBLE);
        b.retryBtn.setVisibility(View.GONE);
        b.noConnectionImage.setVisibility(View.GONE);
        b.progressLabel.setText(R.string.oobe_con_check_in_progress);

        // check device is connected to internet
        if (Util.getConnectionType(requireContext()) == Util.ConnectionType.None) {
            checkFail();
            return;
        }

        if(SKIP_API_CHECK)
        {
            checkOk();
            return;
        }

        // device is connected, check if MAL api is reachable
        // init retrofit
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Urls.API)
                .addConverterFactory(new RetrofitEnumConverterFactory())
                .addConverterFactory(GsonConverterFactory.create(TenshiApp.getGson()))
                .build();

        // create service and make dummy request
        final MalService mal = retrofit.create(MalService.class);
        mal.getCurrentUser("").enqueue(new Callback<User>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<User> call, Response<User> response) {
                // we didn't supply a auth token, so we expect a 403 Unauthorized
                // if we get that, all is ok
                if (response.code() == 403)
                    checkOk();
                else {
                    Log.e("Tenshi", "Connection Check unexpected return code: " + response.code());
                    checkFail();
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("Tenshi", "Connection Check failed: " + t.toString());
                t.printStackTrace();
                checkFail();
            }
        });
    }

    /**
     * called by {@link #checkConnection()} if the check is successfull
     */
    private void checkOk() {
        //b.loadingIndicator.setVisibility(View.GONE);
        invokeSuccessListener();
    }

    /**
     * called by {@link #checkConnection()} if the check failed
     */
    private void checkFail() {
        b.loadingIndicator.setVisibility(View.GONE);
        b.noConnectionImage.setVisibility(View.VISIBLE);
        b.retryBtn.setVisibility(View.VISIBLE);
        b.progressLabel.setText(R.string.oobe_con_check_failed);
        invokeFailListener();
    }
}
