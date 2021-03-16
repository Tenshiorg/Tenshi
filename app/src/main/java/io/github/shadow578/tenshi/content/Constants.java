package io.github.shadow578.tenshi.content;

/**
 * constants used for ContentAdapters (stuff defined in Manifest)
 */
public final class Constants {
    /**
     * intent action for content adapters
     */
    public static final String ACTION_TENSHI_CONTENT = "io.github.shadow578.tenshi.content.ADAPTER";

    /**
     * intent category for content adapters
     */
    public static final String CATEGORY_TENSHI_CONTENT = ACTION_TENSHI_CONTENT;

    /**
     * metadata key for content adapter unique name String
     */
    public static final String META_UNIQUE_NAME = "io.github.shadow578.tenshi.content.UNIQUE_NAME";

    /**
     * metadata key for content adapter display name string
     */
    public static final String META_DISPLAY_NAME = "io.github.shadow578.tenshi.content.DISPLAY_NAME";

    /**
     * metadata key for content adapter version int
     */
    public static final String META_ADAPTER_API_VERSION = "io.github.shadow578.tenshi.content.ADAPTER_VERSION";

    /**
     * target for META_ADAPTER_API_VERSION.
     * for a service to be bound, it has to have this or a higher version
     */
    public static final int TARGET_API_VERSION = 1;
}
