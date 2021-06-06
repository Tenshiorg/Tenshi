package io.github.shadow578.tenshi.ui.search;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.github.shadow578.tenshi.R;
import io.github.shadow578.tenshi.adapter.AnimeListAdapter;
import io.github.shadow578.tenshi.adapter.TraceResultsAdapter;
import io.github.shadow578.tenshi.databinding.ActivityImageSearchBinding;
import io.github.shadow578.tenshi.trace.model.AnilistInfo;
import io.github.shadow578.tenshi.trace.model.AnilistTitles;
import io.github.shadow578.tenshi.trace.model.TraceResult;
import io.github.shadow578.tenshi.ui.AnimeDetailsActivity;
import io.github.shadow578.tenshi.ui.TenshiActivity;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.with;

/**
 * anime reverse image search activity (trace.moe search)
 */
public class ImageSearchActivity extends TenshiActivity {

    /**
     * request for the user to select a image, in {@link #openImageSelector()}
     */
    private final int REQUEST_SELECT_IMAGE = 74;

    private ActivityImageSearchBinding b;
    private TraceResultsAdapter resultsAdapter;
    private ArrayList<TraceResult> results = new ArrayList<>();

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

        // set select image button handler
        b.selectImage.setOnClickListener((v) -> openImageSelector());

        // TODO handle share intent

        // TODO dummy data for display
        TraceResult a = new TraceResult();
        a.anilistInfo = new AnilistInfo();
        a.anilistInfo.titles = new AnilistTitles();
        a.anilistInfo.titles.englishTitle = "test A";
        a.episode = 5;
        a.sceneStartSeconds = 10;
        a.sceneEndSeconds = 15;
        a.previewImageUrl = "https://media.trace.moe/image/10079/%5BSumiSora%5D%5BHoshizora%20e%20Kakaru%20Hashi%5D%5BBDrip%5D%5B04%5D%5BBIG5%5D%5B720P%5D.mp4?t=1272.5&token=yroMGLXtDpylWEFG7OWqPcd4zUQ";
        a.similarity = .5;
        results.add(a);
        resultsAdapter.notifyDataSetChanged();
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

        b.devImgView.setImageBitmap(bmp);

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
