package io.github.shadow578.tenshi.db.dao;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import io.github.shadow578.tenshi.db.model.AnimeXGenreCrossReference;
import io.github.shadow578.tenshi.db.model.AnimeXStudioCrossReference;
import io.github.shadow578.tenshi.db.model.AnimeXThemeCrossReference;
import io.github.shadow578.tenshi.db.model.MediaRelation;
import io.github.shadow578.tenshi.mal.model.Anime;
import io.github.shadow578.tenshi.mal.model.Genre;
import io.github.shadow578.tenshi.mal.model.RelatedMedia;
import io.github.shadow578.tenshi.mal.model.Studio;
import io.github.shadow578.tenshi.mal.model.Theme;
import io.github.shadow578.tenshi.mal.model.UserLibraryEntry;
import io.github.shadow578.tenshi.mal.model.type.LibraryEntryStatus;

import static io.github.shadow578.tenshi.db.DBUtil.merge;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.foreach;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.isNull;
import static io.github.shadow578.tenshi.extensionslib.lang.LanguageUtil.notNull;

/**
 * DAO for {@link Anime} and {@link UserLibraryEntry}
 */
@Dao
public abstract class AnimeDao {
    /**
     * insert OR update one or more anime in the database.
     * Also inserts / updates the genres, studios and themes, aswell as adding the required cross reference values.
     *
     * @param animeList the anime to insert or update
     */
    @Transaction
    public void insertAnime(@Nullable List<Anime> animeList) {
        foreach(animeList, anime -> {
            // insert or update the anime
            _mergeUpdateAnime(anime);

            // create and update cross references:
            //region genres
            final ArrayList<AnimeXGenreCrossReference> genreRefs = new ArrayList<>();
            foreach(anime.genres, genre -> {
                // insert or update
                _mergeUpdateGenre(genre);

                // create ref
                genreRefs.add(new AnimeXGenreCrossReference(anime.animeId, genre.id));
            });

            // insert refs
            _insertGenreReference(genreRefs);
            //endregion

            //region studios
            final ArrayList<AnimeXStudioCrossReference> studioRefs = new ArrayList<>();
            foreach(anime.studios, studio -> {
                // insert or update
                _mergeUpdateStudio(studio);

                // create ref
                studioRefs.add(new AnimeXStudioCrossReference(anime.animeId, studio.id));
            });

            // insert refs
            _insertStudioReference(studioRefs);
            //endregion

            //region themes
            final ArrayList<AnimeXThemeCrossReference> themeRefs = new ArrayList<>();
            foreach(anime.openingThemes, theme -> {
                // insert or update
                _mergeUpdateTheme(theme);

                // create ref
                themeRefs.add(new AnimeXThemeCrossReference(anime.animeId, theme.id, false));
            });
            foreach(anime.endingThemes, theme -> {
                // insert or update
                _mergeUpdateTheme(theme);

                // create ref
                themeRefs.add(new AnimeXThemeCrossReference(anime.animeId, theme.id, true));
            });

            // insert refs
            _insertThemeReference(themeRefs);
            //endregion

            // region related media
            final ArrayList<MediaRelation> relations = new ArrayList<>();
            foreach(anime.relatedAnime, related -> {
                // create relation
                relations.add(MediaRelation.fromRelatedMedia(anime.animeId, related, false));
            });
            foreach(anime.relatedManga, related -> {
                // create relation
                relations.add(MediaRelation.fromRelatedMedia(anime.animeId, related, true));
            });

            // insert relations
            _insertMediaRelations(relations);
            // endregion
        });
    }

    /**
     * get a anime by id.
     * Also gets the genres, studios and themes and sets them in the anime object
     *
     * @param animeId         the id of the anime
     * @param resolveCrossRef resolve cross references? if not, studios, themes, genres and related anime will be missing
     * @return the anime loaded
     */
    @Nullable
    @Transaction
    public Anime getAnime(int animeId, boolean resolveCrossRef) {
        // get the anime
        final Anime anime = _getAnimeById(animeId);

        // this anime is not in the db, no need to do anything more
        if (isNull(anime))
            return null;

        // get cross references
        if (resolveCrossRef)
            resolveCrossReferences(anime);
        return anime;
    }

    /**
     * get the entries in the user library from the database
     *
     * @param status the status to get. this supports {@link LibraryEntryStatus#All}
     * @return the library entries.
     */
    @NonNull
    @Transaction
    public List<UserLibraryEntry> getUserLibrary(@NonNull LibraryEntryStatus status) {
        // get all anime with that status
        final List<Anime> library = (status == LibraryEntryStatus.All) ? _getUserLibrary() : _getUserLibrary(status);

        // create UserLibraryEntries for all anime found
        final ArrayList<UserLibraryEntry> libraryEntries = new ArrayList<>();
        foreach(library, anime -> {
            // resolve cross references
            resolveCrossReferences(anime);

            // create and add entry
            final UserLibraryEntry entry = new UserLibraryEntry();
            entry.anime = anime;
            entry.libraryStatus = anime.userListStatus;

            libraryEntries.add(entry);
        });

        return libraryEntries;
    }

    /**
     * resolves cross references (genres, studios, themes) for a anime
     *
     * @param anime the anime to resolve cross references for
     */
    private void resolveCrossReferences(@NonNull Anime anime) {
        // region genres
        anime.genres = new ArrayList<>();
        foreach(_getGenreReferences(anime.animeId), ref -> {
            final Genre genre = _getGenreById(ref.genreId);
            if (notNull(genre))
                anime.genres.add(genre);
        });
        // endregion

        // region studios
        anime.studios = new ArrayList<>();
        foreach(_getStudioReferences(anime.animeId), ref -> {
            final Studio studio = _getStudioById(ref.studioId);
            if (notNull(studio))
                anime.studios.add(studio);
        });
        // endregion

        // region themes
        anime.openingThemes = new ArrayList<>();
        anime.endingThemes = new ArrayList<>();
        foreach(_getThemeReferences(anime.animeId), ref -> {
            final Theme theme = _getThemeById(ref.themeId);
            if (notNull(theme))
                if (ref.isEnding)
                    anime.endingThemes.add(theme);
                else
                    anime.openingThemes.add(theme);
        });
        // endregion

        // region relations
        anime.relatedAnime = new ArrayList<>();
        anime.relatedManga = new ArrayList<>();
        foreach(_getMediaRelations(anime.animeId), relation -> {
            final Anime child = _getAnimeById(relation.childId);
            if (notNull(child)) {
                // create related media instance
                final RelatedMedia related = new RelatedMedia();
                related.relatedAnime = child;
                related.relationType = relation.relationType;
                related.relationTypeFormatted = relation.relationTypeFormatted;

                // do NOT resolve cross references of the child
                // it is not needed for anything and would also cause problems because of recursion

                // add it to the right list
                if (relation.isManga)
                    anime.relatedManga.add(related);
                else
                    anime.relatedAnime.add(related);
            }
        });
        //endregion
    }

    //region mergeUpdate wrappers

    /**
     * inserts a anime in to the db or updates it using {@link io.github.shadow578.tenshi.db.DBUtil#merge(Class, Object, Object)}
     *
     * @param anime the anime to insert or update
     */
    protected void _mergeUpdateAnime(@NonNull Anime anime) {
        final Anime old = _getAnimeById(anime.animeId);
        if (isNull(old)) {
            // does not exist, just insert normally
            _insertAnime(anime);
            return;
        }

        // merge old and new object, then update in db
        anime = merge(Anime.class, old, anime);
        _updateAnime(anime);
    }

    /**
     * inserts a studio in to the db or updates it using {@link io.github.shadow578.tenshi.db.DBUtil#merge(Class, Object, Object)}
     *
     * @param studio the studio to insert or update
     */
    protected void _mergeUpdateStudio(@NonNull Studio studio) {
        final Studio old = _getStudioById(studio.id);
        if (isNull(old)) {
            // does not exist, just insert normally
            _insertStudio(studio);
            return;
        }

        // merge old and new object, then update in db
        studio = merge(Studio.class, old, studio);
        _updateStudio(studio);
    }

    /**
     * inserts a genre in to the db or updates it using {@link io.github.shadow578.tenshi.db.DBUtil#merge(Class, Object, Object)}
     *
     * @param genre the genre to insert or update
     */
    protected void _mergeUpdateGenre(@NonNull Genre genre) {
        final Genre old = _getGenreById(genre.id);
        if (isNull(old)) {
            // does not exist, just insert normally
            _insertGenre(genre);
            return;
        }

        // merge old and new object, then update in db
        genre = merge(Genre.class, old, genre);
        _updateGenre(genre);
    }

    /**
     * inserts a theme in to the db or updates it using {@link io.github.shadow578.tenshi.db.DBUtil#merge(Class, Object, Object)}
     *
     * @param theme the theme to insert or update
     */
    protected void _mergeUpdateTheme(@NonNull Theme theme) {
        final Theme old = _getThemeById(theme.id);
        if (isNull(old)) {
            // does not exist, just insert normally
            _insertTheme(theme);
            return;
        }

        // merge old and new object, then update in db
        theme = merge(Theme.class, old, theme);
        _updateTheme(theme);
    }
    //endregion

    // region direct functions to tables
    // region anime

    /**
     * get a anime by id
     *
     * @param animeId the anime id to get
     * @return the anime
     */
    @Query("SELECT * FROM anime WHERE anime_id = :animeId")
    protected abstract Anime _getAnimeById(int animeId);

    /**
     * get all anime that have a library status.
     *
     * @return all anime in the user library
     */
    @Query("SELECT * FROM anime WHERE `my_list_status.status` IS NOT NULL")
    protected abstract List<Anime> _getUserLibrary();

    /**
     * get all anime with the given library entry status.
     *
     * @param status the status to get
     * @return all anime with that status
     */
    @Query("SELECT * FROM anime WHERE `my_list_status.status` = :status")
    protected abstract List<Anime> _getUserLibrary(@NonNull LibraryEntryStatus status);

    /**
     * insert a anime into the database.
     * this does NOT overwrite existing values {@link OnConflictStrategy#IGNORE}
     *
     * @param anime the anime to insert
     * @return id of the inserted anime, or -1 if not inserted (already exists)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract int _insertAnime(@NonNull Anime anime);

    /**
     * update a anime entry
     *
     * @param anime the anime to update
     */
    @Update
    protected abstract void _updateAnime(@NonNull Anime anime);

    /**
     * delete one or more anime from the database
     *
     * @param anime the anime to delete
     */
    @Delete
    protected abstract void _deleteAnime(@NonNull Anime... anime);
    // endregion

    // region genre

    /**
     * get a genre by id
     *
     * @param genreId the genre id to get
     * @return the genre
     */
    @Query("SELECT * FROM genres WHERE genre_id = :genreId")
    protected abstract Genre _getGenreById(int genreId);

    /**
     * insert a genre into the database.
     * this does NOT overwrite existing values {@link OnConflictStrategy#IGNORE}
     *
     * @param genre the genre to insert
     * @return id of the inserted genre, or -1 if not inserted (already exists)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract int _insertGenre(@NonNull Genre genre);

    /**
     * update a genre entry
     *
     * @param genre the genre to update
     */
    @Update
    protected abstract void _updateGenre(@NonNull Genre genre);

    /**
     * delete one or more genres from the database
     *
     * @param genres the genres to delete
     */
    @Delete
    protected abstract void _deleteGenres(@NonNull Genre... genres);
    //endregion

    // region studio

    /**
     * get a studio by id
     *
     * @param studioId the studio id to get
     * @return the studio
     */
    @Query("SELECT * FROM studios WHERE studio_id = :studioId")
    protected abstract Studio _getStudioById(int studioId);

    /**
     * insert a studio into the database.
     * this does NOT overwrite existing values {@link OnConflictStrategy#IGNORE}
     *
     * @param studio the studio to insert
     * @return id of the inserted studio, or -1 if not inserted (already exists)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract int _insertStudio(@NonNull Studio studio);

    /**
     * update a studio entry
     *
     * @param studio the studio to update
     */
    @Update
    protected abstract void _updateStudio(@NonNull Studio studio);

    /**
     * delete one or more studios from the database
     *
     * @param studios the studios to delete
     */
    @Delete
    protected abstract void _deleteStudio(@NonNull Studio... studios);
    // endregion

    // region themes

    /**
     * get a theme by id
     *
     * @param themeId the theme id to get
     * @return the theme
     */
    @Query("SELECT * FROM themes WHERE theme_id = :themeId")
    protected abstract Theme _getThemeById(int themeId);

    /**
     * insert a theme into the database.
     * this does NOT overwrite existing values {@link OnConflictStrategy#IGNORE}
     *
     * @param theme the theme to insert
     * @return id of the inserted theme, or -1 if not inserted (already exists)
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract int _insertTheme(@NonNull Theme theme);

    /**
     * update a theme entry
     *
     * @param theme the theme to update
     */
    @Update
    protected abstract void _updateTheme(@NonNull Theme theme);

    /**
     * delete one or more themes from the database
     *
     * @param themes the theme to delete
     */
    @Delete
    protected abstract void _deleteTheme(@NonNull Theme... themes);
    // endregion

    // region Anime X Genre cross reference

    /**
     * get a list of all genre references for a anime id
     *
     * @param animeId the anime id
     * @return all genre references
     */
    @Query("SELECT * FROM anime_genre_ref WHERE anime_id = :animeId")
    protected abstract List<AnimeXGenreCrossReference> _getGenreReferences(int animeId);

    /**
     * insert a genre reference for a anime into the db.
     * if the reference already exists, it is removed
     *
     * @param ref the ref to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insertGenreReference(@NonNull List<AnimeXGenreCrossReference> ref);

    /**
     * delete a genre reference for a anime in the database
     *
     * @param ref the reference to delete
     */
    @Delete
    protected abstract void _deleteGenreReference(@NonNull List<AnimeXGenreCrossReference> ref);
    //endregion

    // region Anime X Studio cross reference

    /**
     * get a list of all studio references for a anime id
     *
     * @param animeId the anime id
     * @return all studio references
     */
    @Query("SELECT * FROM anime_studio_ref WHERE anime_id = :animeId")
    protected abstract List<AnimeXStudioCrossReference> _getStudioReferences(int animeId);

    /**
     * insert a studio reference for a anime into the db.
     * if the reference already exists, it is removed
     *
     * @param ref the ref to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insertStudioReference(@NonNull List<AnimeXStudioCrossReference> ref);

    /**
     * delete a studio reference for a anime in the database
     *
     * @param ref the reference to delete
     */
    @Delete
    protected abstract void _deleteStudioReference(@NonNull List<AnimeXStudioCrossReference> ref);
    //endregion

    // region Anime X Theme cross reference

    /**
     * get a list of all theme references for a anime id
     *
     * @param animeId the anime id
     * @return all genre references
     */
    @Query("SELECT * FROM anime_theme_ref WHERE anime_id = :animeId")
    protected abstract List<AnimeXThemeCrossReference> _getThemeReferences(int animeId);

    /**
     * insert a theme reference for a anime into the db.
     * if the reference already exists, it is replaced
     *
     * @param ref the ref to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insertThemeReference(@NonNull List<AnimeXThemeCrossReference> ref);

    /**
     * delete a theme reference for a anime in the database
     *
     * @param ref the reference to delete
     */
    @Delete
    protected abstract void _deleteThemeReference(@NonNull List<AnimeXThemeCrossReference> ref);
    //endregion

    // region MediaRelation

    /**
     * get all media relation with a given parent id
     *
     * @param parentId the parent id to get
     * @return the related medias
     */
    @Query("SELECT * FROM relations WHERE parent_id = :parentId")
    protected abstract List<MediaRelation> _getMediaRelations(int parentId);

    /**
     * insert media relations into the db.
     * if the relation already exists, it is replaced
     *
     * @param relations the relations to insert
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract void _insertMediaRelations(@NonNull List<MediaRelation> relations);

    /**
     * delete media relations from the db.
     *
     * @param relations the relations to delete
     */
    @Delete
    protected abstract void _deleteMediaRelations(@NonNull List<MediaRelation> relations);
    // endregion
    // endregion
}
