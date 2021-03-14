package io.github.shadow578.tenshi.content;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.content.aidl.ITenshiContentAdapter;
import io.github.shadow578.tenshi.lang.Consumer;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

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

    public ContentAdapter(@NonNull ServiceInfo svc) {
        service = svc;
    }

    /**
     * get the name of the underlying ServiceInfo
     *
     * @return the name of the ServiceInfo
     */
    @Nullable
    public String getServiceName() {
        return service.name;
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
     * get the internal name of this adapter.
     * Can be human- readable or a randomized string, as long as it's unique.
     *
     * @param callback the callback called as soon as the service answered. The result may be null if the service died or answered null.
     */
    public void getName(@NonNull Consumer<String> callback) {
        async(() -> {
            try {
                synchronized (ADAPTER_LOCK) {
                    if (waitUntilServiceConnected())
                        return adapter.getName();
                    else
                        return null;
                }
            } catch (RemoteException e) {
                return null;
            }
        }, callback);
    }

    /**
     * get the display name for this adapter
     *
     * @param callback the callback called as soon as the service answered. The result may be null if the service died or answered null.
     */
    public void getDisplayName(@NonNull Consumer<String> callback) {
        async(() -> {
            try {
                synchronized (ADAPTER_LOCK) {
                    if (waitUntilServiceConnected())
                        return adapter.getDisplayName();
                    else
                        return null;
                }
            } catch (RemoteException e) {
                return null;
            }
        }, callback);
    }

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