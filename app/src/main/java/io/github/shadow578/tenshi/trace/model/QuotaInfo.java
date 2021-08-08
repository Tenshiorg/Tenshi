package io.github.shadow578.tenshi.trace.model;

import com.google.gson.annotations.SerializedName;

/**
 * trace.moe quota info
 */
public final class QuotaInfo {

    /**
     * id on trace.moe (IP address when guest, mail when user)
     */
    @SerializedName("id")
    public String id;

    /**
     * priority in the search queue (0= lowest)
     */
    @SerializedName("priority")
    public int priority;

    /**
     * number of parallel search request we can make
     */
    @SerializedName("concurrency")
    public int concurrency;

    /**
     * search quota in this month
     */
    @SerializedName("quota")
    public int quotaTotal;

    /**
     * search quota used this month, out of {@link #quotaTotal}
     */
    @SerializedName("quotaUsed")
    public int quotaUsed;
}
