// ITenshiContentAdapter.aidl
package io.github.shadow578.tenshi.content.aidl;

import android.net.Uri;

/**
* AIDL interface class for Tenshi Content Adapters,
* Version 1
*/
interface ITenshiContentAdapter {
        /**
        * get the internal name of this adapter.
        * Can be human- readable or a randomized string, as long as it's unique.
        */
        String getName();

        /**
        * get the display name for this adapter
        */
        String getDisplayName();

        /**
         * query a video stream URI for a anime and episode.
         * if this anime is not found or no uri can be found for some other reason, return null
         *
         * @param malID the anime's id on MAL
         * @param enTitle the english title of the anime (from MAL)
         * @param jpTitle the japanese title of the anime (from MAL)
         * @param episode the episode number to get the stream url of
         * @return the url for streaming
         */
        Uri getStreamUri(int malID, String enTitle, String jpTitle, int episode);
}