package io.github.shadow578.tenshi.util;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.*;


/**
 * Helper class for loading / saving to shared preferences
 */
@SuppressWarnings({"unused", "RedundantSuppression"})
public class TenshiPrefs {
    /**
     * Configuration Keys
     * <p>
     * Keep in mind that these are also used in settings screen, but are accessed by hardcoded string.
     * So if you rename one in here, you have to adjust the settings screen manually
     */
    public enum Key {
        /**
         * used to store the auth token between launches
         */
        AuthToken,

        /**
         * the theme override
         */
        Theme,

        /**
         * the section in MainActivity to open on start
         */
        StartupSection,

        /**
         * show NSFW media?
         */
        NSFW,

        /**
         * the MAL id of the currently logged user
         */
        UserID,

        /**
         * the sort mode of the library
         */
        LibrarySortMode,

        /**
         * the last selected library category (restored on launch)
         */
        LastLibraryCategory,

        /**
         * are debug options enabled in Settings?
         */
        ShowDeveloperOptions,

        /**
         * was oobe finished already?
         */
        OOBEFinished,

        /**
         * did the user finish / skip the main activity tutorial?
         */
        MainTutorialFinished,

        /**
         * did the user finish / skip the anime details activity tutorial not in the user library?
         */
        AnimeDetailsNoLibTutorialFinished,

        /**
         * did the user finish / skip the anime details activity tutorial for anime in the user library?
         */
        AnimeDetailsInLibTutorialFinished,

        /**
         * did the user finish / skip the search activity tutorial?
         */
        SearchTutorialFinished
    }

    /**
     * Theme overrides, possible values for {@link Key#Theme}
     */
    public enum Theme {
        /**
         * follow the system theme
         */
        FollowSystem,

        /**
         * force use light theme
         */
        Light,

        /**
         * force use dark theme
         */
        Dark,

        /**
         * force use amoled theme
         */
        Amoled
    }

    /**
     * generally available GSON instance for serialization
     */
    private static final Gson gson = new Gson();

    /**
     * the shared prefs.
     * This should never be null, as .init() is called right in TenshiApp.
     * However, it can technically be null, so no @NonNull annotation for you.
     * <p>
     * Just imagine there was a @NonNull around here
     */
    private static SharedPreferences prefs;

    /**
     * initialize the preferences. call before using any other method
     *
     * @param ctx the context to work in
     */
    public static void init(@NonNull Context ctx) {
        if (prefs == null)
            prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    /**
     * clear all preferences
     */
    public static void clear() {
        prefs.edit()
                .clear()
                .apply();
    }

    /**
     * clear a specific preference
     *
     * @param key the preference to clear
     */
    public static void clear(@NonNull Key key) {
        prefs.edit()
                .remove(getKeyName(key))
                .apply();
    }

    /**
     * get the raw name of a key
     *
     * @param key the key to get the name of
     * @return the preference name
     */
    @NonNull
    public static String getKeyName(@NonNull Key key) {
        return key.name();
    }

    /**
     * check if a key exists
     *
     * @param key the key to check
     * @return does the key exist?
     */
    public static boolean doesKeyExist(@NonNull Key key) {
        return prefs.contains(getKeyName(key));
    }

    //region set/get interface

    /**
     * set a int value
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static void setInt(@NonNull Key key, int value) {
        prefs.edit()
                .putInt(getKeyName(key), value)
                .apply();
    }

    /**
     * get a  value from prefs
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    public static int getInt(@NonNull Key key, int def) {
        return doesKeyExist(key) ? prefs.getInt(getKeyName(key), def) : def;
    }

    /**
     * set a bool value
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static void setBool(@NonNull Key key, boolean value) {
        prefs.edit()
                .putBoolean(getKeyName(key), value)
                .apply();
    }

    /**
     * get a int value from prefs
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    public static boolean getBool(@NonNull Key key, boolean def) {
        return doesKeyExist(key) ? prefs.getBoolean(getKeyName(key), def) : def;
    }

    /**
     * set a float value
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static void setFloat(@NonNull Key key, float value) {
        prefs.edit()
                .putFloat(getKeyName(key), value)
                .apply();
    }

    /**
     * get a float value from prefs
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    public static float getFloat(@NonNull Key key, float def) {
        return doesKeyExist(key) ? prefs.getFloat(getKeyName(key), def) : def;
    }

    /**
     * set a long value
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static void setLong(@NonNull Key key, long value) {
        prefs.edit()
                .putLong(getKeyName(key), value)
                .apply();
    }

    /**
     * get a long value from prefs
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    public static long getLong(@NonNull Key key, long def) {
        return doesKeyExist(key) ? prefs.getLong(getKeyName(key), def) : def;
    }

    /**
     * set a string value
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static void setString(@NonNull Key key, @NonNull String value) {
        prefs.edit()
                .putString(getKeyName(key), value)
                .apply();
    }

    /**
     * get a string value from prefs
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    @NonNull
    public static String getString(@NonNull Key key, @NonNull String def) {
        return doesKeyExist(key) ? prefs.getString(getKeyName(key), def) : def;
    }

    /**
     * set a enum value.
     * Uses {@link EnumHelper} under the hood, so supports @SerializedName
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static <T extends Enum<T>> void setEnum(@NonNull Key key, @NonNull T value) {
        final String val = EnumHelper.valueOf(value);
        if(notNull(val))
            setString(key, val);
    }

    /**
     * get a enum value from prefs.
     * Uses {@link EnumHelper} under the hood, so supports @SerializedName
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    @NonNull
    public static <T extends Enum<T>> T getEnum(@NonNull Key key, @NonNull Class<T> type, @NonNull T def) {
        return EnumHelper.parseEnum(type, getString(key, ""), def, true);
    }

    /**
     * write a object to prefs, serialized using Gson
     * @param key the key of the value to set
     * @param value the value to set
     */
    public static <T> void setObject(@NonNull Key key, @NonNull T value) {
        setString(key, gson.toJson(value));
    }

    /**
     * get a object from prefs, deserialized using Gson
     * @param key the key of the value to get
     * @param def the default value, if the key is not found
     * @return the value loaded from prefs, or def
     */
    @Nullable
    public static <T> T getObject(@NonNull Key key, @NonNull Class<T> type, @Nullable T def) {
        // get json data
        final String json = getString(key, "");
        if (nullOrWhitespace(json))
            return def;

        // deserialize json into object
        final T obj = gson.fromJson(json, type);
        return notNull(obj) ? obj : def;
    }
    //endregion
}