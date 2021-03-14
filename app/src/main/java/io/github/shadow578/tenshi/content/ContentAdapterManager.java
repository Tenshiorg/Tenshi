package io.github.shadow578.tenshi.content;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.github.shadow578.tenshi.lang.LanguageUtils.fmt;
import static io.github.shadow578.tenshi.lang.LanguageUtils.isNull;
import static io.github.shadow578.tenshi.lang.LanguageUtils.notNull;

/**
 * Discovers and Manages {@link ContentAdapter} connections
 */
public class ContentAdapterManager {

    /**
     * intent action for content adapters
     */
    public static final String ACTION_TENSHI_CONTENT = "io.github.shadow578.tenshi.TENSHI_CONTENT_ADAPTER";

    /**
     * intent category for content adapters
     */
    public static final String CATEGORY_TENSHI_CONTENT = ACTION_TENSHI_CONTENT;

    /**
     * metadata to content adapter version int
     */
    public static final String META_ADAPTER_API_VERSION = "io.github.shadow578.tenshi.TENSHI_CONTENT_ADAPTER_VERSION";

    /**
     * target for META_ADAPTER_API_VERSION.
     * for a service to be bound, it has to have this or a higher version
     */
    public static final int TARGET_ADAPTER_API = 1;

    @NonNull
    private final Context ctx;

    private final ArrayList<ContentAdapter> contentAdapters = new ArrayList<>();

    /**
     * initialize the content adapter manager
     *
     * @param ctx the context to work in
     */
    public ContentAdapterManager(@NonNull Context ctx) {
        this.ctx = ctx;
    }

    /**
     * how many content adapters are available and bound?
     *
     * @return the number of content adapters
     */
    public int getAdapterCount() {
        return contentAdapters.size();
    }

    /**
     * get a read- only list of all discovered adapters
     *
     * @return the list of adapters
     */
    @NonNull
    public List<ContentAdapter> getAdapters() {
        return Collections.unmodifiableList(contentAdapters);
    }


    //region discovery

    /**
     * discover and bind to all found content adapters
     */
    public void discoverContentAdapters() {
        // get the package manager
        final PackageManager pm = ctx.getPackageManager();

        // prepare intent for query
        final Intent contentAdapterQuery = new Intent(ACTION_TENSHI_CONTENT);
        contentAdapterQuery.addCategory(CATEGORY_TENSHI_CONTENT);

        // query all possible adapter services
        final List<ResolveInfo> resolvedAdapters = pm.queryIntentServices(contentAdapterQuery, PackageManager.MATCH_ALL);

        // add all found services to list of content adapters
        for (ResolveInfo resolvedAdapter : resolvedAdapters)
            if (notNull(resolvedAdapter.serviceInfo) && resolvedAdapter.serviceInfo.exported) {
                // query metadata
                final ServiceInfo serviceWithMeta = getWithFlags(pm, resolvedAdapter.serviceInfo, PackageManager.GET_META_DATA);

                if (notNull(serviceWithMeta)
                        && shouldBindService(serviceWithMeta))
                    bindService(serviceWithMeta);
            }
    }

    /**
     * get the service with the given flags
     *
     * @param pm    the package manager to use
     * @param svc   the service to get
     * @param flags flags for getServiceInfo();
     * @return the service, or null if failed
     */
    @Nullable
    private ServiceInfo getWithFlags(@NonNull PackageManager pm, @NonNull ServiceInfo svc, @SuppressWarnings("SameParameterValue") int flags) {
        try {
            return pm.getServiceInfo(new ComponentName(svc.packageName, svc.name), flags);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    /**
     * should we attempt to bind the service?
     * Check META_ADAPTER_API_VERSION matches
     *
     * @param adapterService the service to bind
     * @return should we bind the service?
     */
    private boolean shouldBindService(@NonNull ServiceInfo adapterService) {
        // get metadata
        final Bundle meta = adapterService.metaData;

        // we have not metadata, dont bind
        if (isNull(meta))
            return false;

        // we have meta, get META_ADAPTER_API_VERSION
        int apiVer = meta.getInt(META_ADAPTER_API_VERSION, -1);

        // only bind if api requirement is met
        if (apiVer >= TARGET_ADAPTER_API)
            return true;
        else {
            Log.w("TenshiCP", fmt("Content adapter %s is outdated (found: %d ; target: %d)", adapterService.name, apiVer, TARGET_ADAPTER_API));
            return false;
        }
    }

    /**
     * bind to a content adapter service and add to contentAdapters list
     *
     * @param adapterService the service to bind
     */
    private void bindService(@NonNull ServiceInfo adapterService) {
        Log.i("TenshiCP", fmt("Binding Content Adapter %s", adapterService.name));

        // create ContentAdapter instance and add to list
        final ContentAdapter adapter = new ContentAdapter(adapterService);
        contentAdapters.add(adapter);

        // bind the service
        final Intent svcBindIntent = new Intent(ACTION_TENSHI_CONTENT);
        svcBindIntent.addCategory(CATEGORY_TENSHI_CONTENT);
        svcBindIntent.setComponent(new ComponentName(adapterService.packageName, adapterService.name));

        ctx.bindService(svcBindIntent, adapter, Context.BIND_AUTO_CREATE);
    }
    //endregion
}
