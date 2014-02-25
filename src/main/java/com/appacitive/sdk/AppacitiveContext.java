package com.appacitive.sdk;

import com.appacitive.sdk.infra.APContainer;
import com.appacitive.sdk.infra.JavaPlatform;
import com.appacitive.sdk.model.Environment;
import com.appacitive.sdk.model.Platform;

import java.io.Serializable;

/**
 * Created by sathley.
 */
public class AppacitiveContext implements Serializable {

    private static final Double[] currentLocation = new Double[2];
    private static String loggedInUserToken;
    private static String apiKey;
    private static String environment;
    private static boolean isInitialized = false;

    public static final String getLoggedInUserToken() {
        return loggedInUserToken;
    }

    public static final void setLoggedInUserToken(String userToken) {
        AppacitiveContext.loggedInUserToken = userToken;
    }

    public static final void initialize(String apiKey, Environment environment, Platform platform) {
        AppacitiveContext.apiKey = apiKey;
        AppacitiveContext.environment = environment.name();
        if (platform == null)
            APContainer.registerAll(new JavaPlatform().getRegistrations());

        else
            APContainer.registerAll(platform.getRegistrations());
        ExecutorServiceWrapper.init();
        isInitialized = true;
    }

    public static final void logout() {
        setLoggedInUserToken(null);
    }

    public static final boolean isInitialized() {
        return isInitialized;
    }

    public static void shutdown() {
        ExecutorServiceWrapper.shutdown();
    }

    public static final Double[] getCurrentLocation() {
        return currentLocation;
    }

    public static final void setCurrentLocation(Double latitude, Double longitude) {
        AppacitiveContext.currentLocation[0] = latitude;
        AppacitiveContext.currentLocation[1] = longitude;
    }

    public static String getApiKey() {
        return apiKey;
    }

    public static String getEnvironment() {
        return environment;
    }
}