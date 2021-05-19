package io.github.shadow578.tenshi.notifications.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.io.File;

/**
 * database to store (pending) scheduled notifications in
 */
@Database(entities = {
        ScheduledNotificationInfo.class
}, version = 1)
public abstract class NotificationsDB extends RoomDatabase {

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
    public static NotificationsDB create(@NonNull Context ctx) {
        return Room.databaseBuilder(ctx, NotificationsDB.class, DB_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    /**
     * get the absolute path to the database file
     *
     * @param ctx the context to work in
     * @return the path to the database file
     */
    @SuppressWarnings("unused")
    public static File getDatabasePath(@NonNull Context ctx) {
        return ctx.getDatabasePath(DB_NAME);
    }

    /**
     * @return the notifications database DAO
     */
    public abstract NotificationsDAO notificationsDB();
}
