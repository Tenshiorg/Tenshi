package io.github.shadow578.tenshi.db.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.where;

/**
 * cleanup DAO
 */
@Dao
public abstract class CleanupDao {

    /**
     * deletes a anime and all cross references using its id
     *
     * @param id the anime id to delete
     */
    @Transaction
    public void deleteAnimeById(int id) {
        // remove the anime itself
        _deleteAnimeById(id);

        // get all cross references (genres, studios, themes) of the anime
        List<Integer> genresToKill = _getGenresOf(id);
        List<Integer> studiosToKill = _getStudiosOf(id);
        List<Integer> themesToKill = _getThemesOf(id);

        // remove all cross references of the anime
        _deleteGenreRefsOf(id);
        _deleteStudioRefsOf(id);
        _deleteThemeRefsOf(id);

        // filter out all cross references that are still referenced
        genresToKill = where(genresToKill, genreId -> _countGenreRefs(genreId) <= 0);
        studiosToKill = where(studiosToKill, studioId -> _countStudioRefs(studioId) <= 0);
        themesToKill = where(themesToKill, themeId -> _countThemeRefs(themeId) <= 0);

        // remove all cross references that are no longer referenced
        _deleteGenreById(genresToKill);
        _deleteStudioById(studiosToKill);
        _deleteThemeById(themesToKill);

        // remove content adapter info for this anime
        _deleteContentAdapterInfoById(id);
    }

    /**
     * delete the user with matching id
     *
     * @param id the user id to delete
     */
    @Query("DELETE FROM users WHERE user_id = :id")
    public abstract void deleteUserById(int id);

    /**
     * delete the anime with matching id
     *
     * @param id the anime ids to delete
     */
    @Query("DELETE FROM anime WHERE anime_id = :id ")
    protected abstract void _deleteAnimeById(int id);

    /**
     * delete multiple genres by id
     *
     * @param ids the ids to delete
     */
    @Query("DELETE FROM genres WHERE genre_id IN (:ids)")
    protected abstract void _deleteGenreById(List<Integer> ids);

    /**
     * delete multiple studios by id
     *
     * @param ids the ids to delete
     */
    @Query("DELETE FROM studios WHERE studio_id IN (:ids)")
    protected abstract void _deleteStudioById(List<Integer> ids);

    /**
     * delete multiple themes by id
     *
     * @param ids the ids to delete
     */
    @Query("DELETE FROM themes WHERE theme_id IN (:ids)")
    protected abstract void _deleteThemeById(List<Integer> ids);

    /**
     * delete all content adapter info for a anime with id
     *
     * @param animeId the anime id to remove info of
     */
    @Query("DELETE FROM anime_content_adapters WHERE anime_id = :animeId")
    protected abstract void _deleteContentAdapterInfoById(int animeId);

    // region get refs

    /**
     * get all genre ids of an anime
     *
     * @param id the anime id to get the genres of
     * @return the genre ids
     */
    @Query("SELECT genre_id FROM anime_genre_ref WHERE anime_id = :id")
    protected abstract List<Integer> _getGenresOf(int id);

    /**
     * get all studio ids of an anime
     *
     * @param id the anime id to get the studios of
     * @return the studio ids
     */
    @Query("SELECT studio_id FROM anime_studio_ref WHERE anime_id = :id")
    protected abstract List<Integer> _getStudiosOf(int id);

    /**
     * get all theme ids of an anime
     *
     * @param id the anime id to get the themes of
     * @return the theme ids
     */
    @Query("SELECT theme_id FROM anime_theme_ref WHERE anime_id = :id")
    protected abstract List<Integer> _getThemesOf(int id);
    //endregion

    // region ref count

    /**
     * count all genre references
     *
     * @param genreId the genre id
     * @return how many references exist to the given genre
     */
    @Query("SELECT COUNT(genre_id) FROM anime_genre_ref WHERE genre_id = :genreId")
    protected abstract int _countGenreRefs(int genreId);

    /**
     * count all studio references
     *
     * @param studioId the studio id
     * @return how many references exist to the given studio
     */
    @Query("SELECT COUNT(studio_id) FROM anime_studio_ref WHERE studio_id = :studioId")
    protected abstract int _countStudioRefs(int studioId);

    /**
     * count all theme references
     *
     * @param themeId the theme id
     * @return how many references exist to the given theme
     */
    @Query("SELECT COUNT(theme_id) FROM anime_theme_ref WHERE theme_id = :themeId")
    protected abstract int _countThemeRefs(int themeId);
    //endregion

    //region delete refs

    /**
     * delete all genre refs of a anime
     *
     * @param animeId the anime id
     */
    @Query("DELETE FROM anime_genre_ref WHERE anime_id = :animeId")
    protected abstract void _deleteGenreRefsOf(int animeId);

    /**
     * delete all studio refs of a anime
     *
     * @param animeId the anime id
     */
    @Query("DELETE FROM anime_studio_ref WHERE anime_id = :animeId")
    protected abstract void _deleteStudioRefsOf(int animeId);

    /**
     * delete all theme refs of a anime
     *
     * @param animeId the anime id
     */
    @Query("DELETE FROM anime_theme_ref WHERE anime_id = :animeId")
    protected abstract void _deleteThemeRefsOf(int animeId);
    //endregion

}
