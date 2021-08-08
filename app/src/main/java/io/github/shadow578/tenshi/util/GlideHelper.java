package io.github.shadow578.tenshi.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import io.github.shadow578.tenshi.GlideApp;
import io.github.shadow578.tenshi.R;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * helper class for Glide, to ensure all images are handled the same
 */
public class GlideHelper {

    /**
     * prepare glide to load the given image url
     *
     * @param context the context to work in
     * @param imgUrl  the image to load
     * @return the request builder, setup for loading (just call .into() on it)
     */
    @NonNull
    public static RequestBuilder<Drawable> glide(@NonNull Context context, @Nullable String imgUrl) {
        return glide(context, imgUrl, R.drawable.ic_splash);
    }

    /**
     * prepare glide to load the given image url
     *
     * @param context       the context to work in
     * @param imgUrl        the image to load
     * @param placeholderId the placeholder image (also error image)
     * @return the request builder, setup for loading (just call .into() on it)
     */
    @NonNull
    public static RequestBuilder<Drawable> glide(@NonNull Context context, @Nullable String imgUrl, @DrawableRes int placeholderId) {
        if (nullOrEmpty(imgUrl)) {
            //empty image url, use placeholder
            return Glide.with(context)
                    .load(placeholderId);
        } else {
            return GlideApp.with(context)
                    .load(imgUrl)
                    .placeholder(placeholderId)
                    .error(placeholderId);
            //TODO crossfade seems to be broken, see https://github.com/bumptech/glide/issues/363
            //.transition(new DrawableTransitionOptions().crossFade());
        }
    }
}
