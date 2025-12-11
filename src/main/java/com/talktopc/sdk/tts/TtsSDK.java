package com.talktopc.sdk.tts;

import com.talktopc.sdk.common.SDKConfig;
import com.talktopc.sdk.tts.client.TtsRestClient;
import com.talktopc.sdk.tts.client.TtsStreamClient;
import com.talktopc.sdk.tts.models.TTSRequest;
import com.talktopc.sdk.tts.models.TTSResponse;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * TalkToPC TTS SDK - Text-to-Speech Module
 * 
 * Simple Java SDK for text-to-speech conversion using TalkToPC's TTS API.
 * 
 * Features:
 * - Simple TTS (complete audio file)
 * - Streaming TTS (real-time chunks)
 * - Multiple voice support
 * - Automatic authentication
 * 
 * Example usage:
 * <pre>
 * TtsSDK sdk = TtsSDK.builder()
 *     .apiKey("your-api-key")
 *     .baseUrl("https://api.talktopc.com")
 *     .build();
 * 
 * // Simple TTS
 * byte[] audio = sdk.textToSpeech("Hello world", "mamre");
 * 
 * // Streaming TTS
 * sdk.textToSpeechStream("Hello world", "mamre", chunk -> {
 *     phoneSystem.playAudio(chunk);
 * });
 * </pre>
 */
public class TtsSDK {
    
    private final TtsRestClient restClient;
    private final TtsStreamClient streamClient;
    private final com.talktopc.sdk.tts.client.TtsWebSocketClient websocketClient;
    private final SDKConfig config;
    
    /**
     * Private constructor - use builder() to create instances
     */
    private TtsSDK(SDKConfig config) {
        this.config = config;
        this.restClient = new TtsRestClient(config);
        this.streamClient = new TtsStreamClient(config);
        this.websocketClient = new com.talktopc.sdk.tts.client.TtsWebSocketClient(config);
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
     * Simple text-to-speech conversion (blocking)
     * Returns complete audio file as byte array
     * 
     * @param text Text to synthesize
     * @param voiceId Voice ID (e.g., "mamre", "en-US-female")
     * @return Complete audio data as byte array
     * @throws com.talktopc.sdk.tts.exception.TtsException if synthesis fails
     */
    public byte[] textToSpeech(String text, String voiceId) {
        return textToSpeech(TTSRequest.builder()
            .text(text)
            .voiceId(voiceId)
            .build());
    }
    
    /**
     * Simple text-to-speech conversion with speed control (blocking)
     * 
     * @param text Text to synthesize
     * @param voiceId Voice ID
     * @param speed Voice speed (0.5 to 2.0, default 1.0)
     * @return Complete audio data as byte array
     */
    public byte[] textToSpeech(String text, String voiceId, double speed) {
        return textToSpeech(TTSRequest.builder()
            .text(text)
            .voiceId(voiceId)
            .speed(speed)
            .build());
    }
    
    /**
     * Text-to-speech with full request configuration (blocking)
     * 
     * @param request TTS request configuration
     * @return Complete audio data as byte array
     */
    public byte[] textToSpeech(TTSRequest request) {
        TTSResponse response = restClient.synthesize(request);
        return response.getAudio();
    }
    
    /**
     * Get full TTS response with metadata (blocking)
     * 
     * @param request TTS request configuration
     * @return TTS response with audio and metadata
     */
    public TTSResponse synthesize(TTSRequest request) {
        return restClient.synthesize(request);
    }
    
    /**
     * Streaming text-to-speech (non-blocking)
     * Audio chunks are delivered to the handler as they're generated
     * 
     * @param text Text to synthesize
     * @param voiceId Voice ID
     * @param chunkHandler Handler for audio chunks
     */
    public void textToSpeechStream(String text, String voiceId, Consumer<byte[]> chunkHandler) {
        textToSpeechStream(TTSRequest.builder()
            .text(text)
            .voiceId(voiceId)
            .build(), chunkHandler);
    }
    
    /**
     * Streaming text-to-speech with speed control (non-blocking)
     * 
     * @param text Text to synthesize
     * @param voiceId Voice ID
     * @param speed Voice speed (0.5 to 2.0)
     * @param chunkHandler Handler for audio chunks
     */
    public void textToSpeechStream(String text, String voiceId, double speed, Consumer<byte[]> chunkHandler) {
        textToSpeechStream(TTSRequest.builder()
            .text(text)
            .voiceId(voiceId)
            .speed(speed)
            .build(), chunkHandler);
    }
    
    /**
     * Streaming text-to-speech with full configuration (non-blocking)
     * 
     * @param request TTS request configuration
     * @param chunkHandler Handler for audio chunks
     */
    public void textToSpeechStream(TTSRequest request, Consumer<byte[]> chunkHandler) {
        streamClient.stream(request, chunkHandler);
    }
    
    /**
     * Streaming text-to-speech with completion callback (non-blocking)
     * 
     * @param request TTS request configuration
     * @param chunkHandler Handler for audio chunks
     * @param onComplete Callback when streaming completes (receives metadata)
     * @param onError Error handler
     */
    public void textToSpeechStream(
            TTSRequest request,
            Consumer<byte[]> chunkHandler,
            Consumer<StreamMetadata> onComplete,
            Consumer<Throwable> onError) {
        streamClient.stream(request, chunkHandler, onComplete, onError);
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
     * Get WebSocket client for advanced usage
     * 
     * @return WebSocket client instance
     */
    public com.talktopc.sdk.tts.client.TtsWebSocketClient getWebSocketClient() {
        return websocketClient;
    }
    
    /**
     * Text-to-speech using WebSocket (lower latency, bidirectional)
     * 
     * Connects to: /api/v1/tts/stream/{voiceId}
     * 
     * @param request TTS request configuration
     * @param chunkHandler Handler for audio chunks (received as binary)
     * @param onComplete Completion handler (receives metadata)
     * @param onError Error handler
     * @return CompletableFuture that completes when connected and request sent
     */
    public CompletableFuture<Void> textToSpeechWebSocket(
            TTSRequest request,
            Consumer<byte[]> chunkHandler,
            Consumer<StreamMetadata> onComplete,
            Consumer<Throwable> onError) {
        
        websocketClient.onAudioChunk(chunkHandler);
        websocketClient.onComplete(metadata -> {
            if (onComplete != null) {
                StreamMetadata sdkMetadata = new StreamMetadata(
                    metadata.getConversationId(),
                    metadata.getTotalChunks(),
                    metadata.getTotalBytes(),
                    metadata.getDurationMs(),
                    metadata.getCreditsUsed()
                );
                onComplete.accept(sdkMetadata);
            }
        });
        websocketClient.onError(onError != null ? onError : Throwable::printStackTrace);
        
        return websocketClient.connectAndSynthesize(request);
    }
    
    /**
     * Text-to-speech using WebSocket (simplified version)
     * 
     * @param text Text to synthesize
     * @param voiceId Voice ID
     * @param chunkHandler Handler for audio chunks
     * @return CompletableFuture that completes when connected and request sent
     */
    public CompletableFuture<Void> textToSpeechWebSocket(
            String text,
            String voiceId,
            Consumer<byte[]> chunkHandler) {
        
        TTSRequest request = TTSRequest.builder()
            .text(text)
            .voiceId(voiceId)
            .build();
        
        return textToSpeechWebSocket(request, chunkHandler, null, null);
    }
    
    /**
     * Stream metadata returned on completion
     */
    public static class StreamMetadata {
        private final String conversationId;
        private final long totalChunks;
        private final long totalBytes;
        private final long durationMs;
        private final double creditsUsed;
        
        public StreamMetadata(String conversationId, long totalChunks, long totalBytes, 
                            long durationMs, double creditsUsed) {
            this.conversationId = conversationId;
            this.totalChunks = totalChunks;
            this.totalBytes = totalBytes;
            this.durationMs = durationMs;
            this.creditsUsed = creditsUsed;
        }
        
        public String getConversationId() { return conversationId; }
        public long getTotalChunks() { return totalChunks; }
        public long getTotalBytes() { return totalBytes; }
        public long getDurationMs() { return durationMs; }
        public double getCreditsUsed() { return creditsUsed; }
        
        @Override
        public String toString() {
            return String.format("StreamMetadata{conversationId='%s', chunks=%d, bytes=%d, duration=%dms, credits=%.2f}",
                conversationId, totalChunks, totalBytes, durationMs, creditsUsed);
        }
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
         * @return TtsSDK instance
         * @throws IllegalArgumentException if API key is missing
         */
        public TtsSDK build() {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalArgumentException("API key is required");
            }
            
            SDKConfig config = new SDKConfig(apiKey, baseUrl, connectTimeout, readTimeout);
            return new TtsSDK(config);
        }
    }
}
