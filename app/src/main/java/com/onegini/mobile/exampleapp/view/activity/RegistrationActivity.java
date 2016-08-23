package com.onegini.mobile.exampleapp.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.onegini.mobile.android.sdk.client.OneginiClient;
import com.onegini.mobile.android.sdk.handlers.OneginiRegistrationHandler;
import com.onegini.mobile.android.sdk.handlers.error.OneginiRegistrationError;
import com.onegini.mobile.android.sdk.model.entity.UserProfile;
import com.onegini.mobile.exampleapp.Constants;
import com.onegini.mobile.exampleapp.OneginiSDK;
import com.onegini.mobile.exampleapp.R;
import com.onegini.mobile.exampleapp.model.User;
import com.onegini.mobile.exampleapp.storage.UserStorage;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RegistrationActivity extends Activity {

  @SuppressWarnings({ "unused", "WeakerAccess" })
  @Bind(R.id.name_edit_text)
  EditText nameEditText;
  @SuppressWarnings({ "unused", "WeakerAccess" })
  @Bind(R.id.create_profile_button)
  Button createProfileButton;
  @SuppressWarnings({ "unused", "WeakerAccess" })
  @Bind(R.id.progress_bar_register)
  ProgressBar progressBar;
  @SuppressWarnings({ "unused", "WeakerAccess" })
  @Bind(R.id.layout_register_content)
  LinearLayout layoutRegisterContent;
  @SuppressWarnings({ "unused", "WeakerAccess" })
  @Bind(R.id.user_profile_debug)
  TextView userProfileDebugText;

  private UserProfile registeredProfile;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_registration);
    ButterKnife.bind(this);

    setupUserInterface();
    registerUser();
  }

  private void setupUserInterface() {
    createProfileButton.setEnabled(false);
    layoutRegisterContent.setVisibility(View.GONE);
    progressBar.setVisibility(View.VISIBLE);
    userProfileDebugText.setText("");
    nameEditText.addTextChangedListener(new ProfileNameChangeListener());
  }

  @Override
  public void onNewIntent(final Intent intent) {
    super.onNewIntent(intent);
    handleRedirection(intent.getData());
  }

  private void handleRedirection(final Uri uri) {
    final OneginiClient client = OneginiSDK.getOneginiClient(getApplicationContext());
    if (uri != null && client.getConfigModel().getRedirectUri().startsWith(uri.getScheme())) {
      client.getUserClient().handleAuthorizationCallback(uri);
    }
  }

  private void registerUser() {
    final OneginiClient oneginiClient = OneginiSDK.getOneginiClient(this);
    oneginiClient.getUserClient().registerUser(Constants.DEFAULT_SCOPES, new OneginiRegistrationHandler() {

      @Override
      public void onSuccess(final UserProfile userProfile) {
        PinActivity.setRemainingFailedAttempts(0);
        registeredProfile = userProfile;
        userProfileDebugText.setText(userProfile.getProfileId());
        askForProfileName();
      }

      @Override
      public void onError(final OneginiRegistrationError oneginiRegistrationError) {
        handleRegistrationErrors(oneginiRegistrationError);
      }
    });
  }

  private void handleRegistrationErrors(final OneginiRegistrationError oneginiRegistrationError) {
    final int errorType = oneginiRegistrationError.getErrorType();
    switch (errorType) {
      case OneginiRegistrationError.ACTION_CANCELED:
        showToast("Registration was cancelled");
        break;
      case OneginiRegistrationError.NETWORK_CONNECTIVITY_PROBLEM:
        showToast("No internet connection.");
        break;
      case OneginiRegistrationError.OUTDATED_APP:
        showToast("Please update application in order to use.");
        break;
      case OneginiRegistrationError.GENERAL_ERROR:
        break;
      default:
        // General error handling for other, less relevant errors
        handleGeneralError(oneginiRegistrationError);
        break;
    }
  }

  private void handleGeneralError(final OneginiRegistrationError oneginiRegistrationError) {
    final StringBuilder stringBuilder = new StringBuilder("General error: ");
    stringBuilder.append(oneginiRegistrationError.getErrorDescription());

    final Exception exception = oneginiRegistrationError.getException();
    if (exception != null) {
      stringBuilder.append(" Check logcat for more details.");
      exception.printStackTrace();
    }

    showToast(stringBuilder.toString());
  }

  private void askForProfileName() {
    progressBar.setVisibility(View.GONE);
    layoutRegisterContent.setVisibility(View.VISIBLE);
  }

  @SuppressWarnings("unused")
  @OnClick(R.id.create_profile_button)
  public void onCreateProfileClick() {
    storeUserProfile();
    startDashboardActivity();
  }

  private void showToast(final String message) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
  }

  private void storeUserProfile() {
    final String name = nameEditText.getText().toString().trim();
    final User user = new User(registeredProfile, name);
    final UserStorage userStorage = new UserStorage(this);
    userStorage.saveUser(user);
  }

  private void startDashboardActivity() {
    final Intent intent = new Intent(this, DashboardActivity.class);
    startActivity(intent);
    finish();
  }

  private class ProfileNameChangeListener implements TextWatcher {

    @Override
    public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

    }

    @Override
    public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {

    }

    @Override
    public void afterTextChanged(final Editable s) {
      final String name = s.toString().trim();
      if (name.isEmpty()) {
        createProfileButton.setEnabled(false);
      } else {
        createProfileButton.setEnabled(true);
      }
    }
  }
}
