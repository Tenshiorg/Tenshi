package io.github.shadow578.tenshi.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.github.shadow578.tenshi.db.model.GenreRelation;
import io.github.shadow578.tenshi.db.model.LastAccess;
import io.github.shadow578.tenshi.db.model.MediaRelation;
import io.github.shadow578.tenshi.db.model.StudioRelation;
import io.github.shadow578.tenshi.db.model.ThemeRelation;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.Genre;
import io.github.shadow578.tenshi.mal.model.Studio;
import io.github.shadow578.tenshi.mal.model.Theme;
import io.github.shadow578.tenshi.mal.model.User;

/**
 * Tenshi's database to store MAL information offline
 */
@TypeConverters({
        TenshiTypeConverters.class
})
@Database(entities = {
        Anime.class,
        Genre.class,
        GenreRelation.class,
        Studio.class,
        StudioRelation.class,
        Theme.class,
        ThemeRelation.class,
        MediaRelation.class,
        User.class,
        LastAccess.class
}, version = 1)
public abstract class TenshiDB extends RoomDatabase {

    /**
     * create a new instance of the database
     *
     * @param ctx the context to work in
     * @return the database instance
     */
    public static TenshiDB create(@NonNull Context ctx) {
        return Room.databaseBuilder(ctx, TenshiDB.class, "tenshi-db")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries() //TODO main thread queries
                .build();
    }

}
