/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfpush.gcm;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.SparseArray;

public abstract class WakefulBroadcastReceiver extends BroadcastReceiver {

    private static final String EXTRA_WAKE_LOCK_ID = "android.support.content.wakelockid";

    private static final SparseArray<PowerManager.WakeLock> WAKE_LOCK_SPARSE_ARRAY
            = new SparseArray<>();
    private static int mNextId = 1;

    /**
     * Do a {@link android.content.Context#startService(android.content.Intent)
     * Context.startService}, but holding a wake lock while the service starts.
     * This will modify the Intent to hold an extra identifying the wake lock;
     * when the service receives it in {@link android.app.Service#onStartCommand
     * Service.onStartCommand}, it should the Intent it receives there back to
     * {@link #completeWakefulIntent(android.content.Intent)} in order to release
     * the wake lock.
     *
     * @param context The Context in which it operate.
     * @param intent  The Intent with which to start the service, as per
     *                {@link android.content.Context#startService(android.content.Intent)
     *                Context.startService}.
     */
    public static ComponentName startWakefulService(Context context, Intent intent) {
        synchronized (WAKE_LOCK_SPARSE_ARRAY) {
            int id = mNextId;
            mNextId++;
            if (mNextId <= 0) {
                mNextId = 1;
            }

            intent.putExtra(EXTRA_WAKE_LOCK_ID, id);
            ComponentName comp = context.startService(intent);
            if (comp == null) {
                return null;
            }

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    "wake:" + comp.flattenToShortString());
            wl.setReferenceCounted(false);
            wl.acquire(60 * 1000);
            WAKE_LOCK_SPARSE_ARRAY.put(id, wl);
            return comp;
        }
    }

    /**
     * Finish the execution from a previous {@link #startWakefulService}.  Any wake lock
     * that was being held will now be released.
     *
     * @param intent The Intent as originally generated by {@link #startWakefulService}.
     * @return Returns true if the intent is associated with a wake lock that is
     * now released; returns false if there was no wake lock specified for it.
     */
    public static boolean completeWakefulIntent(Intent intent) {
        final int id = intent.getIntExtra(EXTRA_WAKE_LOCK_ID, 0);
        if (id == 0) {
            return false;
        }
        synchronized (WAKE_LOCK_SPARSE_ARRAY) {
            PowerManager.WakeLock wl = WAKE_LOCK_SPARSE_ARRAY.get(id);
            if (wl != null) {
                wl.release();
                WAKE_LOCK_SPARSE_ARRAY.remove(id);
                return true;
            }
            return true;
        }
    }
}
