package io.github.shadow578.tenshi.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.TenshiApp;
import io.github.shadow578.tenshi.adapter.TraceResultsAdapter;
import io.github.shadow578.tenshi.databinding.ActivityImageSearchBinding;
import io.github.shadow578.tenshi.trace.TraceAPI;
import io.github.shadow578.tenshi.trace.model.QuotaInfo;
import io.github.shadow578.tenshi.trace.model.TraceResponse;
import io.github.shadow578.tenshi.trace.model.TraceResult;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.ui.TenshiActivity;
import io.github.shadow578.tenshi.util.TenshiPrefs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.internal.EverythingIsNonNull;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.fmt;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrWhitespace;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.where;
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

        // set select image button handler
        b.selectImage.setOnClickListener((v) -> openImageSelector());

        // check if the activity was started from the share target
        final Intent i = getIntent();
        if (notNull(i) && Intent.ACTION_SEND.equals(i.getAction())
                && !nullOrWhitespace(i.getType()) && i.getType().startsWith("image/"))
            handleSharedImage(i);
        else {
            // do the following only if not a share intent. IDK why, but just .hide()ing the loading indicator on every
            // launch breaks it :|
            // hide loading indicator
            b.loadingIndicator.hide();

            // only show "no results" text initially
            b.noResultText.setVisibility(View.VISIBLE);
            b.resultsGroup.setVisibility(View.GONE);
        }
    }

    /**
     * handle a shared image
     *
     * @param i the share intent
     */
    private void handleSharedImage(@NonNull Intent i) {
        // get image uri
        final Uri imgUri = i.getParcelableExtra(Intent.EXTRA_STREAM);
        if (isNull(imgUri)) {
            Snackbar.make(b.getRoot(), R.string.trace_snack_share_fail, Snackbar.LENGTH_SHORT).show();
            return;
        }

        // load bitmap and start search
        final Bitmap bmp = loadBitmap(imgUri);
        if (notNull(bmp))
            doImageSearch(bmp);
        else
            Snackbar.make(b.getRoot(), R.string.trace_snack_share_fail, Snackbar.LENGTH_SHORT).show();
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
        // show loading and hide other ui elements
        b.loadingIndicator.show();
        b.noResultText.setVisibility(View.GONE);
        b.resultsGroup.setVisibility(View.GONE);
        b.selectImage.setEnabled(false);

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
                        b.noResultText.setVisibility(View.VISIBLE);
                        b.selectImage.setEnabled(true);
                        Snackbar.make(b.getRoot(), R.string.trace_snack_no_quota, Snackbar.LENGTH_SHORT).show();
                        return;
                    }

                    // show warning when low on quota (80% used)
                    if (quota.quotaUsed > (quota.quotaTotal * 0.8))
                        Snackbar.make(b.getRoot(), R.string.trace_snack_low_quota, Snackbar.LENGTH_SHORT).show();

                    searchImpl(bmp);
                } else {
                    Snackbar.make(b.getRoot(), R.string.trace_snack_cannot_connect, Snackbar.LENGTH_SHORT).show();
                    b.noResultText.setVisibility(View.VISIBLE);
                    b.selectImage.setEnabled(true);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<QuotaInfo> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                b.loadingIndicator.hide();
                b.noResultText.setVisibility(View.VISIBLE);
                b.selectImage.setEnabled(true);
                Snackbar.make(b.getRoot(), R.string.trace_snack_cannot_connect, Snackbar.LENGTH_SHORT).show();
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
                TraceResponse traceResponse = response.body();
                if (response.isSuccessful() && notNull(traceResponse) && nullOrWhitespace(traceResponse.errorMessage)) {
                    showResults(traceResponse);
                } else {
                    // if we don't have a response with error message, try to get it from the error body
                    if (isNull(traceResponse) || nullOrWhitespace(traceResponse.errorMessage)) {
                        try {
                            if (notNull(response.errorBody()))
                                traceResponse = TenshiApp.getGson().fromJson(response.errorBody().string(), TraceResponse.class);
                        } catch (IOException | JsonSyntaxException ignored) {
                            // either errorBody().string() failed or the error body wasn't correct json, show a generic error
                            Snackbar.make(b.getRoot(), R.string.trace_snack_cannot_connect, Snackbar.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // show error with message if possible, fallback to generic error
                    if (notNull(traceResponse) && !nullOrWhitespace(traceResponse.errorMessage))
                        Snackbar.make(b.getRoot(), fmt(ImageSearchActivity.this, R.string.trace_snack_api_error_fmt, traceResponse.errorMessage), Snackbar.LENGTH_SHORT).show();
                    else
                        Snackbar.make(b.getRoot(), R.string.trace_snack_cannot_connect, Snackbar.LENGTH_SHORT).show();

                    b.noResultText.setVisibility(View.VISIBLE);
                    b.selectImage.setEnabled(true);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<TraceResponse> call, Throwable t) {
                Log.e("Tenshi", t.toString());
                b.loadingIndicator.hide();
                b.noResultText.setVisibility(View.VISIBLE);
                b.selectImage.setEnabled(true);
                Snackbar.make(b.getRoot(), R.string.trace_snack_cannot_connect, Snackbar.LENGTH_SHORT).show();
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
            // have results, add them according to the NSFW setting
            final boolean showNSFW = TenshiPrefs.getBool(TenshiPrefs.Key.NSFW, false);
            results.addAll(where(response.results, (i) ->
                    isNull(i.anilistInfo) || !i.anilistInfo.isAdult || showNSFW));

            // search results by match similarity, descending
            // normally, trace.moe should return the results in order, but better make sure
            Collections.sort(results, (a, b) -> -Double.compare(a.similarity, b.similarity));

            // hide "no results" text, show results group
            b.resultsGroup.setVisibility(View.VISIBLE);
            b.noResultText.setVisibility(View.GONE);

            // update frames searched text
            if (notNull(response.totalFramesSearched))
                b.totalFramesSearched.setText(fmt(this, R.string.trace_frames_searched_fmt, fmt(response.totalFramesSearched)));
        } else {
            // no search results, show "no results" text, hide results group
            b.resultsGroup.setVisibility(View.GONE);
            b.noResultText.setVisibility(View.VISIBLE);
        }

        // notify data changed
        resultsAdapter.notifyDataSetChanged();

        // re- enable image select button
        b.selectImage.setEnabled(true);
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
            final Bitmap bmp = loadBitmap(data.getData());
            if (notNull(bmp))
                onImageSelectorResult(bmp);
        }
    }

    /**
     * load a bitmap from a uri
     *
     * @param uri the uri to load from
     * @return the bitmap loaded. null if load failed
     */
    @Nullable
    private Bitmap loadBitmap(@NonNull Uri uri) {
        try (final InputStream in = getContentResolver().openInputStream(uri)) {
            final Bitmap bmp = BitmapFactory.decodeStream(in);
            if (notNull(bmp))
                return bmp;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
