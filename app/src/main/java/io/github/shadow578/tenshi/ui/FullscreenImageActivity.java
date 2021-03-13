package io.github.shadow578.tenshi.ui;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.igreenwood.loupe.Loupe;

import org.jetbrains.annotations.NotNull;

import io.github.shadow578.tenshi.databinding.ActivityFullPosterBinding;
import io.github.shadow578.tenshi.util.GlideHelper;
import kotlin.Unit;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

public class FullscreenImageActivity extends TenshiActivity {
    /**
     * Extra to tell the activity what image to view
     */
    public static final String EXTRA_IMAGE_URL = "imageUrl";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityFullPosterBinding b = ActivityFullPosterBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        // show loading indicator
        b.posterLoadingIndicator.show();

        // get image url to load
        final String imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        if (nullOrEmpty(imageUrl)) {
            finish();
            return;
        }

        // load image into loupe
        GlideHelper.glide(this, imageUrl)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        b.animeMainPoster.setImageDrawable(resource);
                        createLoupe(b.animeMainPoster, b.animeMainPosterContainer);
                        b.posterLoadingIndicator.hide();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    /**
     * initialize a Loupe for the given image and container
     * @param imageView the image view to init loupe on
     * @param container the container view
     */
    private void createLoupe(@NonNull ImageView imageView, @NonNull ViewGroup container) {
        Loupe.Companion.create(imageView, container, loupe -> {
            loupe.setOnViewTranslateListener(new Loupe.OnViewTranslateListener() {
                @Override
                public void onStart(@NotNull ImageView imageView) {

                }

                @Override
                public void onViewTranslate(@NotNull ImageView imageView, float v) {

                }

                @Override
                public void onDismiss(@NotNull ImageView imageView) {
                    finish();
                }

                @Override
                public void onRestore(@NotNull ImageView imageView) {

                }
            });

            return Unit.INSTANCE;
        });
    }
}
