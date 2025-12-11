package com.talktopc.sdk.config;

/**
 * SDK Configuration
 * 
 * @deprecated This class is maintained for backward compatibility.
 * Please use {@link com.talktopc.sdk.common.SDKConfig} instead.
 * 
 * This class extends the common SDKConfig for backward compatibility.
 */
@Deprecated
public class SDKConfig extends com.talktopc.sdk.common.SDKConfig {
    public SDKConfig(String apiKey, String baseUrl, int connectTimeout, int readTimeout) {
        super(apiKey, baseUrl, connectTimeout, readTimeout);
    }
}
