package com.onegini.mobile.exampleapp.network.client;

import android.content.Context;
import com.google.gson.GsonBuilder;
import com.onegini.mobile.android.sdk.client.OneginiClient;
import com.onegini.mobile.exampleapp.OneginiSDK;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

public class SecuredClient {

  private static final RestAdapter.LogLevel LOG_LEVEL = RestAdapter.LogLevel.FULL;

  private static final GsonConverter gsonConverter = new GsonConverter(new GsonBuilder().disableHtmlEscaping().create());

  public static <T> T prepareSecuredClient(final Class<T> clazz, final Context context) {
    final OneginiClient oneginiClient = OneginiSDK.getOneginiClient(context);
    final RestAdapter restAdapter = new RestAdapter.Builder()
        .setClient(oneginiClient.getResourceRetrofitClient())
        .setEndpoint(oneginiClient.getConfigModel().getResourceBaseUrl())
        .setLogLevel(LOG_LEVEL)
        .setConverter(gsonConverter)
        .build();
    return restAdapter.create(clazz);
  }
}
