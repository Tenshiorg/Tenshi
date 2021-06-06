package io.github.shadow578.tenshi.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.adapter.TraceResultsAdapter;
import io.github.shadow578.tenshi.databinding.ActivityImageSearchBinding;
import io.github.shadow578.tenshi.trace.TraceAPI;
import io.github.shadow578.tenshi.trace.model.QuotaInfo;
import io.github.shadow578.tenshi.trace.model.TraceResponse;
import io.github.shadow578.tenshi.trace.model.TraceResult;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.ui.TenshiActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * anime reverse image search activity (trace.moe search)
 */
public class ImageSearchActivity extends TenshiActivity {

    /**
     * request for the user to select a image, in {@link #openImageSelector()}
     */
    private final int REQUEST_SELECT_IMAGE = 74;

    /**
     * trace api instance
     */
    private final TraceAPI trace = new TraceAPI();

    private ActivityImageSearchBinding b;
    private TraceResultsAdapter resultsAdapter;
    private final ArrayList<TraceResult> results = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityImageSearchBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // check user is logged in, redirect to login and finish() if not
        requireUserAuthenticated();

        // notify user if offline
        showSnackbarIfOffline(b.getRoot());

        // setup status & actionbar
        setSupportActionBar(b.searchToolbar);
        with(getSupportActionBar(), sab -> {
            sab.setDisplayHomeAsUpEnabled(true);
            sab.setHomeButtonEnabled(true);
            sab.setDisplayShowTitleEnabled(false);
        });
        b.searchToolbar.setNavigationOnClickListener(v -> onBackPressed());

        // setup recycler view adapter
        resultsAdapter = new TraceResultsAdapter(
                this,
                results,
                (v, result) -> openDetails(result.anilistInfo.malId, v)
        );
        b.resultsRecycler.setAdapter(resultsAdapter);

        // hide loading indicator
        b.loadingIndicator.hide();

        // show "no results" text initially
        b.noResultText.setVisibility(View.VISIBLE);
        b.resultsRecycler.setVisibility(View.GONE);
        b.totalFramesSearched.setVisibility(View.GONE);

        // set select image button handler
        b.selectImage.setOnClickListener((v) -> openImageSelector());

        // TODO handle share intent
    }

    /**
     * make the user select a image, using the documents or gallery app.
     * {@link #onImageSelectorResult(Bitmap)} is called with the resulting image.
     * based on https://stackoverflow.com/a/5309217/13942493
     */
    private void openImageSelector() {
        final Intent getContent = new Intent(Intent.ACTION_GET_CONTENT);
        getContent.setType("image/*");

        final Intent pick = new Intent(Intent.ACTION_PICK);
        pick.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        final Intent chooser = Intent.createChooser(getContent, getString(R.string.trace_select_image_button));
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pick});

        startActivityForResult(chooser, REQUEST_SELECT_IMAGE);

    }

    /**
     * result callback for the image selector in {@link #openImageSelector()}
     *
     * @param bmp the selected image
     */
    private void onImageSelectorResult(@NonNull Bitmap bmp) {
        doImageSearch(bmp);
    }

    /**
     * start a image search. calls {@link #showResults(TraceResponse)} on successfull response, otherwise shows error to user
     *
     * @param bmp the image to search for. may be scaled by TraceAPI
     */
    private void doImageSearch(@NonNull Bitmap bmp) {
        // show loading
        b.loadingIndicator.show();
        b.noResultText.setVisibility(View.GONE);

        // check quota first
        trace.getService().getQuota().enqueue(new Callback<QuotaInfo>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<QuotaInfo> call, Response<QuotaInfo> response) {
                final QuotaInfo quota = response.body();
                if (response.isSuccessful() && notNull(quota)) {
                    // check quota and continue search
                    // show error when no quota left
                    if (quota.quotaUsed >= quota.quotaTotal) {
                        Snackbar.make(b.getRoot(), "no search quota left this month. try again next month (or with a different IP)", Snackbar.LENGTH_SHORT).show();//TODO hardcoded string
                        return;
                    }

                    // show warning when low on quota (80% used)
                    if (quota.quotaUsed > (quota.quotaTotal * 0.8))
                        Snackbar.make(b.getRoot(), "you used over 80% of your search quota this month", Snackbar.LENGTH_SHORT).show();//TODO hardcoded string

                    searchImpl(bmp);
                } else
                    Snackbar.make(b.getRoot(), "cannot connect to trace.moe", Snackbar.LENGTH_SHORT).show();//TODO hardcoded string
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<QuotaInfo> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                b.loadingIndicator.hide();
                Snackbar.make(b.getRoot(), "Cannot connect to trace.moe", Snackbar.LENGTH_SHORT).show(); //TODO hardcoded string
            }
        });
    }

    /**
     * the actual search call behind {@link #doImageSearch(Bitmap)}, after the quota check.
     * do not use this function directly, as it would bypass the quota check
     *
     * @param bmp the image to search for. may be scaled by TraceAPI
     */
    private void searchImpl(@NonNull Bitmap bmp) {
        trace.search(bmp, new Callback<TraceResponse>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<TraceResponse> call, Response<TraceResponse> response) {
                b.loadingIndicator.hide();
                final TraceResponse traceResponse = response.body();
                if (response.isSuccessful() && notNull(traceResponse) && nullOrWhitespace(traceResponse.errorMessage)) {
                    showResults(traceResponse);
                } else if (notNull(traceResponse) && !nullOrWhitespace(traceResponse.errorMessage))
                    Snackbar.make(b.getRoot(), "Trace.moe returned error: " + traceResponse.errorMessage, Snackbar.LENGTH_SHORT).show();//TODO hardcoded string
                else
                    Snackbar.make(b.getRoot(), "cannot connect to trace.moe", Snackbar.LENGTH_SHORT).show();//TODO hardcoded string
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<TraceResponse> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                b.loadingIndicator.hide();
                Snackbar.make(b.getRoot(), "Cannot connect to trace.moe", Snackbar.LENGTH_SHORT).show(); //TODO hardcoded string
            }
        }, true);
    }

    /**
     * show the results of a trace call
     *
     * @param response the response from trace.moe
     */
    private void showResults(@NonNull TraceResponse response) {
        // clear previous results
        results.clear();

        // check if there are any results
        if (!nullOrEmpty(response.results)) {
            // have results, add them
            results.addAll(response.results);

            // search results by match similarity, descending
            // normally, trace.moe should return the results in order, but better make sure
            Collections.sort(results, (a, b) -> -Double.compare(a.similarity, b.similarity));

            // hide "no results" text, show recycler
            b.resultsRecycler.setVisibility(View.VISIBLE);
            b.noResultText.setVisibility(View.GONE);

            // update frames searched text
            if (notNull(response.totalFramesSearched)) {
                b.totalFramesSearched.setText("Searched " + fmt(response.totalFramesSearched) + " Frames"); //TODO hardcoded string
                b.totalFramesSearched.setVisibility(View.VISIBLE);
            } else
                b.totalFramesSearched.setVisibility(View.GONE);
        } else {
            // no search results, show "no results" text, hide recycler and frame count
            b.resultsRecycler.setVisibility(View.GONE);
            b.totalFramesSearched.setVisibility(View.GONE);
            b.noResultText.setVisibility(View.VISIBLE);
        }

        // notify data changed
        resultsAdapter.notifyDataSetChanged();
    }

    /**
     * open the details for a anime
     *
     * @param animeId the id of the anime to open the details of
     * @param v       the view to use for the transition
     */
    private void openDetails(int animeId, @NonNull View v) {
        final ImageView poster = v.findViewById(R.id.image_preview);
        final ActivityOptionsCompat opt = ActivityOptionsCompat.makeSceneTransitionAnimation(this, poster, poster.getTransitionName());
        final Intent i = new Intent(this, AnimeDetailsActivity.class);
        i.putExtra(AnimeDetailsActivity.EXTRA_ANIME_ID, animeId);
        startActivity(i, opt.toBundle());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_IMAGE && resultCode == Activity.RESULT_OK && notNull(data)) {
            // request from openImageSelector, load image as bitmap and call onImageSelectorResult
            try (final InputStream in = getContentResolver().openInputStream(data.getData())) {
                final Bitmap bmp = BitmapFactory.decodeStream(in);
                if (notNull(bmp))
                    onImageSelectorResult(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
