/*
 * Copyright (c) 2016-2017 Onegini B.V.
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

package com.onegini.mobile.exampleapp.network.fcm;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.onegini.mobile.exampleapp.BuildConfig;
import com.onegini.mobile.exampleapp.OneginiSDK;
import com.onegini.mobile.exampleapp.R;
import com.onegini.mobile.exampleapp.storage.FCMStorage;
import com.onegini.mobile.sdk.android.client.UserClient;
import com.onegini.mobile.sdk.android.handlers.OneginiMobileAuthWithPushEnrollmentHandler;

public class FCMRegistrationService {

  private static final String TAG = "FCMRegistrationService";

  private final Context context;
  private final FCMStorage storage;

  private OneginiMobileAuthWithPushEnrollmentHandler enrollmentHandler;

  public FCMRegistrationService(final Context context) {
    this.context = context;
    storage = new FCMStorage(context);
  }

  public void registerFCMService(final OneginiMobileAuthWithPushEnrollmentHandler handler) {
    enrollmentHandler = handler;
    final String regid = getRegistrationId();
    if (regid.isEmpty()) {
      register();
    } else {
      enrollForMobileAuthentication(regid);
    }
  }

  /**
   * Gets the current registration ID for application on FCM service. If result is empty, the app needs to register.
   *
   * @return registration ID, or empty string if there is no existing registration ID.
   */
  private String getRegistrationId() {
    final String registrationId = storage.getRegistrationId();
    if (registrationId == null || registrationId.isEmpty()) {
      Log.i(TAG, "Registration not found.");
      return "";
    }

    // Check if app was updated; if so, it must clear the registration ID since the existing regID is not guaranteed to work with the new app version.
    final int registeredVersion = storage.getAppVersion();
    final int currentVersion = BuildConfig.VERSION_CODE;
    if (registeredVersion != currentVersion) {
      Log.i(TAG, "App version changed.");
      return "";
    }
    return registrationId;
  }

  /**
   * Registers the application with FCM servers. Stores the registration ID and app versionCode in the application's shared preferences.
   */
  private void register() {
    FirebaseApp.initializeApp(context);
    String fcmRefreshToken = FirebaseInstanceId.getInstance().getToken();
    if (fcmRefreshToken == null) {
      Toast.makeText(context, context.getString(R.string.push_token_is_null_error_message), Toast.LENGTH_LONG).show();
    } else {
      enrollForMobileAuthentication(fcmRefreshToken);
      storeRegisteredId(fcmRefreshToken);
    }
  }

  private void storeRegisteredId(final String regid) {
    storage.setRegistrationId(regid);
    storage.setAppVersion(BuildConfig.VERSION_CODE);
    storage.save();
  }

  private void enrollForMobileAuthentication(final String regId) {
    final UserClient userClient = OneginiSDK.getOneginiClient(context).getUserClient();
    userClient.enrollUserForMobileAuthWithPush(regId, enrollmentHandler);
  }
}
