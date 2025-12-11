package com.talktopc.sdk.agent;

import com.talktopc.sdk.common.SDKConfig;

/**
 * TalkToPC Agent SDK - Agent Injection/Override Module
 * 
 * SDK for injecting or overriding agents in TalkToPC conversations.
 * This module allows you to customize agent behavior, responses, and logic.
 * 
 * Example usage:
 * <pre>
 * AgentSDK sdk = AgentSDK.builder()
 *     .apiKey("your-api-key")
 *     .baseUrl("https://api.talktopc.com")
 *     .build();
 * 
 * // Inject custom agent logic
 * sdk.injectAgent(agentId, customAgentConfig);
 * </pre>
 * 
 * Note: This is a placeholder structure. Implementation will be added based on requirements.
 */
public class AgentSDK {
    
    private final SDKConfig config;
    
    /**
     * Private constructor - use builder() to create instances
     */
    private AgentSDK(SDKConfig config) {
        this.config = config;
    }
    
    /**
     * Create a new SDK builder
     * 
     * @return SDK builder
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * Get SDK configuration
     * 
     * @return SDK configuration
     */
    public SDKConfig getConfig() {
        return config;
    }
    
    /**
     * SDK Builder
     */
    public static class Builder {
        private String apiKey;
        private String baseUrl = "https://api.talktopc.com";
        private int connectTimeout = 30000; // 30 seconds
        private int readTimeout = 60000; // 60 seconds
        
        /**
         * Set API key (required)
         * 
         * @param apiKey Your TalkToPC API key
         * @return Builder
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }
        
        /**
         * Set base URL (optional)
         * Default: https://api.talktopc.com
         * 
         * @param baseUrl Base URL of TalkToPC API
         * @return Builder
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }
        
        /**
         * Set connection timeout (optional)
         * Default: 30 seconds
         * 
         * @param timeout Timeout in milliseconds
         * @return Builder
         */
        public Builder connectTimeout(int timeout) {
            this.connectTimeout = timeout;
            return this;
        }
        
        /**
         * Set read timeout (optional)
         * Default: 60 seconds
         * 
         * @param timeout Timeout in milliseconds
         * @return Builder
         */
        public Builder readTimeout(int timeout) {
            this.readTimeout = timeout;
            return this;
        }
        
        /**
         * Build the SDK instance
         * 
         * @return AgentSDK instance
         * @throws IllegalArgumentException if API key is missing
         */
        public AgentSDK build() {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key is required");
            }
            
            SDKConfig config = new SDKConfig(apiKey, baseUrl, connectTimeout, readTimeout);
            return new AgentSDK(config);
        }
    }
}
