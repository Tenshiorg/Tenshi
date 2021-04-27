package io.github.shadow578.tenshi.ui.oobe;

import androidx.annotation.Nullable;

import io.github.shadow578.tenshi.mal.model.User;

/**
 * objects and variables that are shared between different OOBE steps
 */
public final class SharedOOBEProperties {

    /**
     * loaded user object, after login
     */
    @Nullable
    public User user;
}
