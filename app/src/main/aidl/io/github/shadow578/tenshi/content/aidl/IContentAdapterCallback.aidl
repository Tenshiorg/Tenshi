// IContentAdapterCallback.aidl
package io.github.shadow578.tenshi.content.aidl;

import android.net.Uri;

/**
* AIDL interface class for Tenshi Content Adapter callback
* Version 1 (Keep this in sync with io.github.shadow578.tenshi.content.Constants#TARGET_API_VERSION)
*/
interface IContentAdapterCallback {

    /**
    * Called when IContentAdapter#requestStreamUri finished.
    * Contains the found stream uri, if found
    *
    * @param streamUri the stream uri, or null if not found.
    * @param persistentStorage persistent storage for this adatper, per anime
    */
    void streamUriResult(in String streamUri, in String persistentStorage);
}