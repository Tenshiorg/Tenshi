package io.github.shadow578.tenshi.notifications.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;

/**
 * database to store information about sent notifications
 */
@Database(entities = {
        SentNotificationInfo.class
}, version = 3)
public abstract class SentNotificationsDB extends RoomDatabase {
    /**
     * database name
     */
    public static final String DB_NAME = "notify_db";

    /**
     * create a new instance of the database
     *
     * @param ctx the context to work in
     * @return the database instance
     */
    public static SentNotificationsDB create(@NonNull Context ctx) {
        return Room.databaseBuilder(ctx, SentNotificationsDB.class, DB_NAME)
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
     * @return dao for accessing sent notification info
     */
    public abstract SentNotificationsDAO notificationsDB();
}
