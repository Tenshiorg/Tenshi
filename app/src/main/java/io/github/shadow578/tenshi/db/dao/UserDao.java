package io.github.shadow578.tenshi.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.github.shadow578.tenshi.mal.model.User;

/**
 * DAO for {@link User}
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
@Dao
public abstract class UserDao {

    /**
     * get a user by id
     *
     * @param userId the user id to get
     * @return the user
     */
    @Query("SELECT * FROM users WHERE user_id = :userId")
    public abstract User getUserById(int userId);

    /**
     * insert a user into the database.
     * this will overwrite existing values {@link OnConflictStrategy#REPLACE}
     *
     * @param user the user to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertOrUpdateUser(User... user);

    /**
     * delete one or more users from the database
     * delete one or more users from the database
     *
     * @param users the users to delete
     */
    @Delete
    public abstract void deleteUser(User... users);
}
