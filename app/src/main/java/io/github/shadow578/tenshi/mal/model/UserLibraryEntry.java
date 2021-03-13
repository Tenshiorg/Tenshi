package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import io.github.shadow578.tenshi.mal.Data;

/**
 * a {@link Anime} in a Users library
 */
@Data
public final class UserLibraryEntry {
    /**
     * the anime this entry is for
     */
    @NonNull
    @SerializedName("node")
    public Anime anime = new Anime();

    /**
     * the status of the anime
     */
    @Nullable
    @SerializedName("list_status")
    public LibraryStatus libraryStatus;
}
