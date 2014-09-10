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

package org.onepf.openpush.sample;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.onepf.openpush.OpenPushHelper;
import org.onepf.openpush.Options;
import org.onepf.openpush.gcm.GCMProvider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.Optional;

/**
 * @author Anton Rutkevich, Alexey Vitenko
 * @since 14.05.14
 */
public class PushSampleActivity extends Activity {

    private static final String WEB_SERVER_URL = "http://localhost:8080";
    public static final String GCM_SENDER_ID = "76325631570";
    private static final String TAG = "PushSampleActivity";

    @InjectView(R.id.registration_id)
    TextView mRegistrationIdView;

    @InjectView(R.id.registration_status)
    TextView mRegistrationStatusView;

    @InjectView(R.id.push_provider_name)
    TextView mProviderNameView;

    @InjectView(R.id.register_switch)
    Button mRegisterSwitchView;

    @Optional
    @InjectView(R.id.btn_copy_to_clipboard)
    Button mCopyToClipboardView;

    private BroadcastReceiver mOpenPushReceiver;
    private static OpenPushHelper mOpenPushHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mOpenPushHelper == null) {
            mOpenPushHelper = OpenPushHelper.getInstance(PushSampleActivity.this);
            mOpenPushHelper.setListener(new LocalBroadcastListener(this));
            Options.Builder builder = new Options.Builder();
            builder.addProviders(new GCMProvider(PushSampleActivity.this, GCM_SENDER_ID));
            mOpenPushHelper.init(builder.build());
        }

        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        if (mOpenPushHelper.getState() == OpenPushHelper.STATE_RUNNING) {
            if (mOpenPushReceiver == null) {
                mOpenPushReceiver = new OpenPushEventReceiver();
            }
            registerReceiver(mOpenPushReceiver);
            switchToRegisteredState(mOpenPushHelper.getCurrentProviderName(),
                    mOpenPushHelper.getCurrentProviderRegistrationId());
        } else {
            switchToUnregisteredState();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mOpenPushReceiver != null) {
            registerReceiver(mOpenPushReceiver);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mOpenPushReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mOpenPushReceiver);
        }
    }

    private void registerReceiver(BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(LocalBroadcastListener.ACTION_REGISTERED);
        filter.addAction(LocalBroadcastListener.ACTION_UNREGISTERED);
        filter.addAction(LocalBroadcastListener.ACTION_MESSAGE);
        filter.addAction(LocalBroadcastListener.ACTION_REGISTRATION_ERROR);
        filter.addAction(LocalBroadcastListener.ACTION_NO_AVAILABLE_PROVIDER);
        filter.addAction(LocalBroadcastListener.ACTION_DELETED_MESSAGES);
        filter.addAction(LocalBroadcastListener.ACTION_HOST_APP_REMOVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @OnClick(R.id.register_switch)
    void onRegisterClick() {
        if (mOpenPushHelper.getState() == OpenPushHelper.STATE_RUNNING) {
            mOpenPushHelper.unregister();
        } else if (mOpenPushHelper.getState()
                == OpenPushHelper.STATE_NONE) {
            if (mOpenPushReceiver == null) {
                mOpenPushReceiver = new OpenPushEventReceiver();
            }
            registerReceiver(mOpenPushReceiver);
            mOpenPushHelper.register();
        }
    }

    @Optional
    @OnClick(R.id.btn_copy_to_clipboard)
    void setBtnCopyToClipboard() {
        Toast.makeText(PushSampleActivity.this,
                PushSampleActivity.this.getString(R.string.toast_registration_id_copied),
                Toast.LENGTH_LONG)
                .show();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                ClipData.newPlainText("Push registration token", mRegistrationIdView.getText())
        );
    }

    private void switchToRegisteredState(String providerName, String registrationId) {
        mRegistrationIdView.setText(Html.fromHtml(getString(R.string.registration_id_text, registrationId)));
        mProviderNameView.setText(Html.fromHtml(getString(R.string.push_provider_text, providerName)));
        mRegisterSwitchView.setText(Html.fromHtml(getString(R.string.unregister)));
        mRegistrationStatusView.setText(Html.fromHtml(getString(R.string.registered_status)));
        mCopyToClipboardView.setVisibility(View.VISIBLE);
    }

    private void switchToUnregisteredState() {
        mRegistrationIdView.setText(null);
        mProviderNameView.setText(Html.fromHtml(getString(R.string.push_provider_text, "None")));
        mRegisterSwitchView.setText(Html.fromHtml(getString(R.string.register)));
        mRegistrationStatusView.setText(Html.fromHtml(getString(R.string.unregistered_status)));
        mCopyToClipboardView.setVisibility(View.GONE);

        if (mOpenPushReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mOpenPushReceiver);
            mOpenPushReceiver = null;
        }
    }

    public class OpenPushEventReceiver extends OpenPushBaseReceiver {

        public OpenPushEventReceiver() {
        }

        @Override
        public void onRegistered(@NotNull String providerName, @Nullable String registrationId) {
            Log.i(TAG, String.format("onRegistered(providerName = %s, registrationId = %s)"
                    , providerName, registrationId));
            switchToRegisteredState(providerName, registrationId);

            // You start the registration process by calling register().
            // When the registration ID is ready, OpenPushHelper calls onRegistered() on
            // your app. Transmit the passed-in registration ID to your server, so your
            // server can send messages to this app instance. onRegistered() is also
            // called if your registration ID is rotated or changed for any reason; your
            // app should pass the new registration ID to your server if this occurs.
            // Your server needs to be able to handle a registration ID up to 1536 characters
            // in length.

            // The following is an example of sending the registration ID to your
            // server via a header key/value pair over HTTP.
            sendRegistrationDataToServer(providerName, registrationId);
        }

        private void sendRegistrationDataToServer(String providerName, String registrationId) {
            try {
                URL url = new URL(WEB_SERVER_URL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setDoInput(true);
                con.setUseCaches(false);
                con.setRequestMethod("POST");
                con.setRequestProperty("RegistrationId", registrationId);
                con.setRequestProperty("ProviderName", providerName);
                con.getResponseCode();
            } catch (IOException e) {
                Log.e(TAG, "Can't send registration data to server.", e);
            }
        }

        @Override
        public void onUnregistered(@NotNull String providerName, @Nullable String oldRegistrationId) {
            Log.i(TAG, String.format("onUnregistered(providerName = %s, oldRegistrationId = %s)"
                    , providerName, oldRegistrationId));
            switchToUnregisteredState();
        }
    }
}