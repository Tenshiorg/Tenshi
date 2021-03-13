package io.github.shadow578.tenshi.mal;

/**
 * common MAL urls
 */
public  final class Urls {
    /**
     * base url of MAL
     */
    public static final String BASE = "https://myanimelist.net";

    /**
     * API base url
     */
    public static final String API ="https://api.myanimelist.net/v2/";

    /**
     * OAUTH base url
     */
    public static final String OAUTH = "https://myanimelist.net/v1/oauth2/";

    /**
     * OAUTH response redirect target used by Tenshi.
     * If you want to change this, you'll have to change it here,
     * in AndroidManifest (LoginActivity intent filter), and in your MAL client settings.
     */
    public static final String OAUTH_REDIRECT = "tenshiapp://tenshi.app.auth.mal/";
}
