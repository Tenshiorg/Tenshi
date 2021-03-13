package io.github.shadow578.tenshi.mal.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * a list of {@link UserLibraryEntry} with {@link Paging}
 */
public final class UserLibraryList {
    /**
     * the entries in the list
     */
    @NonNull
    @SerializedName("data")
    public List<UserLibraryEntry> items = new ArrayList<>();

    /**
     * pagination info
     */
    @Nullable
    public Paging paging;
}
