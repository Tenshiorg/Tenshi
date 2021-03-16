package io.github.shadow578.tenshi.content;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.content.aidl.ITenshiContentAdapter;
import io.github.shadow578.tenshi.lang.Consumer;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;
import static io.github.shadow578.tenshi.content.Constants.*;

/**
 * Handles binding to ITenshiContentAdapter services and provides a async wrapper to the service
 */
public class ContentAdapter implements ServiceConnection {
    /**
     * the bound service info
     */
    @NonNull
    private final ServiceInfo service;

    /**
     * the unique name of this adapter
     */
    @NonNull
    private final String uniqueName;

    /**
     * the display name of this adapter
     */
    @NonNull
    private final String displayName;

    /**
     * the AIDL service connection. null if not yet connected or disconnected
     */
    @Nullable
    private ITenshiContentAdapter adapter;

    /**
     * lock for adapter
     */
    private final Object ADAPTER_LOCK = new Object();

    /**
     * flag to disconnect if the service disconnected.
     * Note that this flag only indicates that the service disconnected, not that the service connect in the first place.
     * for that, check adapter is not null
     */
    private boolean didDisconnect = false;

    private ContentAdapter(@NonNull ServiceInfo svc, @NonNull String uName, @NonNull String dName) {
        service = svc;
        uniqueName = uName;
        displayName = dName;
    }

    /**
     * create a new Content Adapter from a given service that has the {@link Constants#ACTION_TENSHI_CONTENT}
     * Requires the service info to have metadata
     *
     * @param svc the service to create the adapter from
     * @return the adapter instance, or null if creation failed
     */
    @Nullable
    public static ContentAdapter fromServiceInfo(@NonNull ServiceInfo svc) {
        // get metadata
        final Bundle meta = svc.metaData;

        // we have no metadata, dont bind
        if (isNull(meta))
            return null;

        // get unique and display name from meta
        final String uniqueName = meta.getString(META_UNIQUE_NAME, null);
        final String displayName = meta.getString(META_DISPLAY_NAME, uniqueName);

        // abort if unique name not found
        if (nullOrEmpty(uniqueName)) {
            Log.e("TenshiCP", fmt("service %s does not have a unique name\n" +
                    "if you're developing this adapter, make sure you added %s as metadata of your service.", svc.name, META_UNIQUE_NAME));
            return null;
        }

        // log a warning if no display name found
        // but fallback to the unique name)
        if (uniqueName.equalsIgnoreCase(displayName))
            Log.w("TenshiCP", fmt("content adapter %s does not define a display name (or it's equal to the unique name)! \n " +
                    "If you're developing this adapter, please consider adding %s metadata to your adapter's manifest.", uniqueName, META_DISPLAY_NAME));

        // create the content adapter instance and return
        return new ContentAdapter(svc, uniqueName, displayName);
    }
    
    /**
     * bind the service of this content adapter. do this before calling {@link ContentAdapter#getStreamUri(int, String, String, int, Consumer)}
     * 
     * @param ctx the context to bind from
     */
    public void bind(@NonNull Context ctx) {
        ctx.bindService(getServiceIntent(), this, Context.BIND_AUTO_CREATE);
    }

    /**
     * unbind the service of this content adatper. If not bound, nothing will happen
     * @param ctx the context to unbind from
     */
    public void unbind(@NonNull Context ctx) {
        ctx.stopService(getServiceIntent());
    }

    /**
     * get the unique name of this adapter
     * @return the unique name of this adapter
     */
    @NonNull
    public String getUniqueName(){
        return uniqueName;
    }

    /**
     * get the display name of this adapter
     * @return the display name of this adapter
     */
    @NonNull
    public String getDisplayName(){
        return displayName;
    }

    /**
     * get a intent for the service
     * @return the intent for the service
     */
    private Intent getServiceIntent(){
        final Intent i = new Intent(ACTION_TENSHI_CONTENT);
        i.addCategory(CATEGORY_TENSHI_CONTENT);
        i.setComponent(new ComponentName(service.packageName, service.name));
        return i;
    }

    //region ServiceConnection
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.i("TenshiCP", fmt("Content Adapter Service %s connected", name.getClassName()));
        synchronized (ADAPTER_LOCK) {
            adapter = ITenshiContentAdapter.Stub.asInterface(service);
            didDisconnect = false;
            ADAPTER_LOCK.notifyAll();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.w("TenshiCP", fmt("Content Adapter Service %s connected", name.getClassName()));
        synchronized (ADAPTER_LOCK) {
            adapter = null;
            didDisconnect = true;
            ADAPTER_LOCK.notifyAll();
        }
    }
    //endregion

    //region ITenshiContentAdapter wrapper

    /**
     * query a video stream URI for a anime and episode.
     * if this anime is not found or no uri can be found for some other reason, return null
     *
     * @param malID    the anime's id on MAL
     * @param enTitle  the english title of the anime (from MAL)
     * @param jpTitle  the japanese title of the anime (from MAL)
     * @param episode  the episode number to get the stream url of
     * @param callback the callback called as soon as the service answered. The result may be null if the service died or answered null.
     */
    public void getStreamUri(int malID, @NonNull String enTitle, @NonNull String jpTitle, int episode, @NonNull Consumer<Uri> callback) {
        async(() -> {
            try {
                synchronized (ADAPTER_LOCK) {
                    if (waitUntilServiceConnected())
                        return adapter.getStreamUri(malID, enTitle, jpTitle, episode);
                    else
                        return null;
                }
            } catch (RemoteException e) {
                return null;
            }
        }, callback);
    }

    /**
     * wait forever until the service is connected
     *
     * @return is the service now connected?
     */
    private boolean waitUntilServiceConnected() {
        // check if service disconnected (wont connect again)
        if (didDisconnect)
            return false;

        // check if already connected
        if (notNull(adapter))
            return true;

        // wait for the adapter to change
        try {
            ADAPTER_LOCK.wait();
        } catch (InterruptedException e) {
            // wait failed, not connected
            return false;
        }

        // connected if not disconnected and not null
        return !didDisconnect && notNull(adapter);
    }
    //endregion
}
