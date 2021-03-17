// IContentAdapter.aidl
package io.github.shadow578.tenshi.content.aidl;

import io.github.shadow578.tenshi.content.aidl.IContentAdapterCallback;

/**
* AIDL interface class for Tenshi Content Adapters,
* Version 1 (Keep this in sync with io.github.shadow578.tenshi.content.Constants#TARGET_API_VERSION)
*/
interface IContentAdapter {
        /**
         * query a video stream URI for a anime and episode.
         * if this anime is not found or no uri can be found for some other reason, return null
         *
         * @param malID the anime's id on MAL
         * @param enTitle the english title of the anime (from MAL)
         * @param jpTitle the japanese title of the anime (from MAL)
         * @param episode the episode number to get the stream url of
         * @param callback callback that is called when the stream uri was found
         * @return the url for streaming
         */
        void requestStreamUri(in int malID, in String enTitle, in String jpTitle, in int episode, IContentAdapterCallback callback);
}