package io.github.shadow578.tenshi.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import io.github.shadow578.tenshi.db.dao.AnimeContentAdapterDao;
import io.github.shadow578.tenshi.db.dao.AnimeDao;
import io.github.shadow578.tenshi.db.dao.CleanupDao;
import io.github.shadow578.tenshi.db.dao.LastAccessInfoDao;
import io.github.shadow578.tenshi.db.dao.UserDao;
import io.github.shadow578.tenshi.db.model.AnimeContentAdapterInfo;
import io.github.shadow578.tenshi.db.model.AnimeXGenreCrossReference;
import io.github.shadow578.tenshi.db.model.AnimeXStudioCrossReference;
import io.github.shadow578.tenshi.db.model.AnimeXThemeCrossReference;
import io.github.shadow578.tenshi.db.model.LastAccessInfo;
import io.github.shadow578.tenshi.db.model.RecommendedMediaRelation;
import io.github.shadow578.tenshi.db.model.RelatedMediaRelation;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.Genre;
import io.github.shadow578.tenshi.mal.model.Studio;
import io.github.shadow578.tenshi.mal.model.Theme;
import io.github.shadow578.tenshi.mal.model.User;
import io.github.shadow578.tenshi.util.DateHelper;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.nullOrEmpty;

/**
 * database to store anime and user information for offline use
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
        RecommendedMediaRelation.class,
        User.class,
        LastAccessInfo.class,
        AnimeContentAdapterInfo.class
}, version = 4)
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
                .build();
    }

    /**
     * get the absolute path to the database file
     *
     * @param ctx the context to work in
     * @return the path to the database file
     */
    public static File getDatabasePath(@NonNull Context ctx) {
        return ctx.getDatabasePath(DB_NAME);
    }

    /**
     * remove unused database entries using the last access time.
     * excludes anime in the user's library
     *
     * @return the number of items that were removed
     */
    public int cleanupDatabase() {
        // get target age
        // = one month old entries
        final LocalDateTime killAge = DateHelper.getLocalTime().minusMonths(1);

        // find all anime and users that were last accessed before the target time
        final List<LastAccessInfo> allToKill = accessDB().getBefore(killAge);
        if (!nullOrEmpty(allToKill))
            for (LastAccessInfo toKill : allToKill)
                if (toKill.isAnime) {
                    // anime to kill, remove using DAO function
                    cleanupDB().deleteAnimeById(toKill.id);
                    accessDB().deleteAccessFor(toKill.id);
                } else {
                    // user to kill, just remove from both dbs
                    cleanupDB().deleteUserById(toKill.id);
                    accessDB().deleteAccessFor(toKill.id);
                }

        // figure out how many entities were removed
        return isNull(allToKill) ? 0 : allToKill.size();
    }

    /**
     * @return the anime and library database DAO
     */
    public abstract AnimeDao animeDB();

    /**
     * @return the user database DAO
     */
    public abstract UserDao userDB();

    /**
     * @return the entity access DAO
     */
    public abstract LastAccessInfoDao accessDB();

    /**
     * @return the cleanup functions DAO
     */
    protected abstract CleanupDao cleanupDB();

    /**
     * @return the content adapter database DAO
     */
    public abstract AnimeContentAdapterDao contentAdapterDB();
}
