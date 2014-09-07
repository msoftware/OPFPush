package org.onepf.openpush.retrypolice;

/**
 * Created by krozov on 05.09.14.
 */
public interface RetryPolice {
    /**
     * Maximum number of try to register push provider.
     * Zero value means that will be no retry.
     */
    int tryCount();

    /**
     * Get delay before next attempt to register push provider.
     *
     * @param tryNumber Number of try. Always positive value, than no greater that {@link #tryCount()}.
     * @return Period in milliseconds to wait before next try ro register.
     */
    long getDelay(int tryNumber);
}