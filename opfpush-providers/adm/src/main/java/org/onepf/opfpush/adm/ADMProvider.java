/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfpush.adm;

import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.amazon.device.messaging.development.ADMManifest;

import org.onepf.opfpush.BasePushProvider;
import org.onepf.opfpush.util.ManifestUtils;
import org.onepf.opfpush.util.ReceiverUtils;
import org.onepf.opfutils.OPFLog;

import static android.Manifest.permission.GET_ACCOUNTS;
import static com.amazon.device.messaging.ADMConstants.LowLevel.ACTION_APP_REGISTRATION_EVENT;
import static com.amazon.device.messaging.ADMConstants.LowLevel.ACTION_RECEIVE_ADM_MESSAGE;
import static com.amazon.device.messaging.development.ADMManifest.PERMISSION_RECEIVE_MESSAGES;
import static org.onepf.opfpush.adm.ADMConstants.AMAZON_MANUFACTURER;
import static org.onepf.opfpush.adm.ADMConstants.KINDLE_STORE_APP_PACKAGE;
import static org.onepf.opfpush.adm.ADMConstants.PROVIDER_NAME;
import static org.onepf.opfpush.adm.ADMConstants.RECEIVE_MESSAGE_PERMISSION_SUFFIX;

/**
 * Amazon Device Messaging push provider implementation.
 *
 * @author Kirill Rozov
 * @author Roman Savin
 * @see <a href="https://developer.amazon.com/appsandservices/apis/engage/device-messaging">Amazon Device Messaging</a>
 * @since 06.09.14
 */
public class ADMProvider extends BasePushProvider {

    @NonNull
    private ADMDelegate adm;

    private PreferencesProvider preferencesProvider;

    public ADMProvider(@NonNull final Context context) {
        super(context, PROVIDER_NAME, KINDLE_STORE_APP_PACKAGE);
        adm = new ADMDelegate(context);
        preferencesProvider = PreferencesProvider.getInstance(getContext());
    }

    @Override
    public void register() {
        OPFLog.methodD();
        OPFLog.d("Start register ADMProvider.");
        adm.startRegister();
    }

    @Override
    public void checkManifest() {
        OPFLog.methodD();
        super.checkManifest();
        final Context context = getContext();
        ADMManifest.checkManifestAuthoredProperly(context);
        context.enforceCallingOrSelfPermission(PERMISSION_RECEIVE_MESSAGES,
                ManifestUtils.getSecurityExceptionMessage(PERMISSION_RECEIVE_MESSAGES));
        context.enforceCallingOrSelfPermission(GET_ACCOUNTS,
                ManifestUtils.getSecurityExceptionMessage(GET_ACCOUNTS));
        final String admMessagePermission = context.getPackageName() + RECEIVE_MESSAGE_PERMISSION_SUFFIX;
        context.enforceCallingOrSelfPermission(admMessagePermission,
                ManifestUtils.getSecurityExceptionMessage(admMessagePermission));

        ManifestUtils.checkService(context, new ComponentName(context, ADMService.class));
        
        final Intent registrationBroadcastIntent = new Intent(ACTION_APP_REGISTRATION_EVENT);
        final Intent receiveBroadcastIntent = new Intent(ACTION_RECEIVE_ADM_MESSAGE);
        final Intent loginChangedBroadcastIntent = new Intent(AccountManager.LOGIN_ACCOUNTS_CHANGED_ACTION);
        ReceiverUtils.checkReceiver(context, registrationBroadcastIntent,
                ADMReceiver.class.getName(), ADMManifest.PERMISSION_SEND_MESSAGES);
        ReceiverUtils.checkReceiver(context, receiveBroadcastIntent,
                ADMReceiver.class.getName(), ADMManifest.PERMISSION_SEND_MESSAGES);
        ReceiverUtils.checkReceiver(context, loginChangedBroadcastIntent,
                LoginAccountsChangedReceiver.class.getName(), null);
    }

    @Override
    public void onRegistrationInvalid() {
        //nothing
    }

    @Override
    public void onUnavailable() {
        //nothing
    }

    @Override
    public boolean isRegistered() {
        OPFLog.methodD();
        return !TextUtils.isEmpty(getRegistrationId());
    }

    @Override
    public void unregister() {
        OPFLog.methodD();
        OPFLog.i("Start unregister ADMProvider.");
        adm.startUnregister();
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable()
                && adm.isSupported()
                && Build.MANUFACTURER.equals(AMAZON_MANUFACTURER);
    }

    @Override
    @Nullable
    public String getRegistrationId() {
        OPFLog.methodD();
        if (!TextUtils.isEmpty(adm.getRegistrationId())) {
            OPFLog.d("ADM registration id is not empty");
            return adm.getRegistrationId();
        }

        return preferencesProvider.getRegistrationId();
    }

    @NonNull
    @Override
    public String toString() {
        return PROVIDER_NAME;
    }
}
