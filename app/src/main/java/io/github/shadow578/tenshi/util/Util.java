package io.github.shadow578.tenshi.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;

import androidx.annotation.NonNull;

import static io.github.shadow578.tenshi.lang.LanguageUtils.*;

/**
 * general util class
 */
public class Util {
    public enum ConnectionType {
        /**
         * The device is not connected to the internet (airplane mode, ...)
         */
        None,

        /**
         * connected to mobile data (3G/4G/LTE/...)
         */
        Cellular,

        /**
         * connected to wifi
         */
        WiFi,

        /**
         * connected using a ethernet cable
         */
        Ethernet,

        /**
         * connected using a vpn connection
         */
        VPN
    }


    /**
     * get the connection type of the device
     *
     * @param ctx the context to work in
     * @return the connection type
     */
    @NonNull
    public static ConnectionType getConnectionType(@NonNull Context ctx) {
        // get connectivity manager
        final ConnectivityManager cm = cast(ctx.getSystemService(Context.CONNECTIVITY_SERVICE));
        if (notNull(cm)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // after marshmallow, use network capabilities
                final NetworkCapabilities cp = cm.getNetworkCapabilities(cm.getActiveNetwork());
                if (notNull(cp)) {
                    if (cp.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                        return ConnectionType.WiFi;
                    else if (cp.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                        return ConnectionType.Ethernet;
                    else if (cp.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                        return ConnectionType.Cellular;
                    else if (cp.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
                        return ConnectionType.VPN;
                }
            } else {
                // pre marshmallow, use network info
                final NetworkInfo net = cm.getActiveNetworkInfo();
                if (notNull(net)) {
                    if (net.getType() == ConnectivityManager.TYPE_WIFI)
                        return ConnectionType.WiFi;
                    else if (net.getType() == ConnectivityManager.TYPE_ETHERNET)
                        return ConnectionType.Ethernet;
                    else if (net.getType() == ConnectivityManager.TYPE_MOBILE)
                        return ConnectionType.Cellular;
                    else if (net.getType() == ConnectivityManager.TYPE_VPN)
                        return ConnectionType.VPN;
                }
            }
        }

        // could not detect, assume none
        return ConnectionType.None;
    }
}
