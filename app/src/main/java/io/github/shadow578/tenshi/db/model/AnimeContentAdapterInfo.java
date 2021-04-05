package io.github.shadow578.tenshi.db.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import io.github.shadow578.tenshi.extensionslib.content.ContentAdapterWrapper;

/**
 * contains info about content adapters in relation to anime
 */
@Entity(tableName = "anime_content_adapters",
        primaryKeys = {
                "anime_id",
                "unique_name"
        })
public class AnimeContentAdapterInfo {

    /**
     * {@link io.github.shadow578.tenshi.mal.model.Anime#animeId} value for this entry
     */
    @ColumnInfo(name = "anime_id")
    public int animeId;

    /**
     * {@link ContentAdapterWrapper#getUniqueName()} value for this entry
     */
    @NonNull
    @ColumnInfo(name = "unique_name")
    public String uniqueName = "";

    /**
     * persistent storage value for the adapter
     */
    @ColumnInfo(name = "persistent_storage")
    public String persistentStorage;

    /**
     * is this adapter the selected adapter for the anime?
     */
    @ColumnInfo(name = "is_selected")
    public boolean isSelected;

    public AnimeContentAdapterInfo(int animeId, @NonNull String uniqueName, String persistentStorage, boolean isSelected) {
        this.animeId = animeId;
        this.uniqueName = uniqueName;
        this.persistentStorage = persistentStorage;
        this.isSelected = isSelected;
    }
}
