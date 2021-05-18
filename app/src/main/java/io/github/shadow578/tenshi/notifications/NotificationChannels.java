package io.github.shadow578.tenshi.notifications;

import androidx.annotation.NonNull;

/**
 * Tenshi notification channels
 */
public enum NotificationChannels {

    /**
     * default notification channel. do not use unless testing
     */
    @Deprecated
    DEFAULT("io.github.shadow578.tenshi.notifications.DEFAULT"),

    /**
     * notification channel used by developer options
     */
    DEVELOPER("io.github.shadow578.tenshi.notifications.DEVELOPER_TEST");

    @NonNull
    private final String id;

    NotificationChannels(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return id;
    }
}
