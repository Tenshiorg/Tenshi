package io.github.shadow578.tenshi.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.github.shadow578.tenshi.db.dao.AnimeDao;
import io.github.shadow578.tenshi.db.dao.UserDao;
import io.github.shadow578.tenshi.db.model.AnimeXGenreCrossReference;
import io.github.shadow578.tenshi.db.model.AnimeXStudioCrossReference;
import io.github.shadow578.tenshi.db.model.AnimeXThemeCrossReference;
import io.github.shadow578.tenshi.db.model.LastAccess;
import io.github.shadow578.tenshi.db.model.RelatedMediaRelation;
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
        AnimeXGenreCrossReference.class,
        Studio.class,
        AnimeXStudioCrossReference.class,
        Theme.class,
        AnimeXThemeCrossReference.class,
        RelatedMediaRelation.class,
        User.class,
        LastAccess.class //TODO implementation missing
}, version = 1)
public abstract class TenshiDB extends RoomDatabase {

    /**
     * database name
     */
    public static final String DB_NAME = "tenshi_db";

    /**
     * create a new instance of the database
     *
     * @param ctx the context to work in
     * @return the database instance
     */
    public static TenshiDB create(@NonNull Context ctx) {
        return Room.databaseBuilder(ctx, TenshiDB.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries() //TODO main thread queries
                .build();
    }

    /**
     * get the absolute path to the database file
     *
     * @param ctx the context to work in
     * @return the path to the database file
     */
    public static String getDatabasePath(@NonNull Context ctx) {
        return ctx.getDatabasePath(DB_NAME).getAbsolutePath();
    }

    /**
     * @return the anime and library database DAO
     */
    public abstract AnimeDao animeDB();

    /**
     * @return the user database DAO
     */
    public abstract UserDao userDB();
}
