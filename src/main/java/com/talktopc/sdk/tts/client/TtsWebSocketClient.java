package com.talktopc.sdk.tts.client;

import com.talktopc.sdk.common.SDKConfig;
import com.talktopc.sdk.tts.exception.TtsException;
import com.talktopc.sdk.tts.models.TTSRequest;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * WebSocket client for TTS streaming
 * 
 * Connects to WebSocket endpoint: /api/v1/tts/stream/{voiceId}
 * 
 * Protocol:
 * - Connect to WebSocket with voiceId in URL path
 * - Send text and configuration as JSON message
 * - Receive audio chunks as binary messages
 * - Receive metadata/completion as JSON text messages
 * 
 * Example usage:
 * <pre>
 * TtsWebSocketClient client = new TtsWebSocketClient(config);
 * 
 * client.onAudioChunk(chunk -> {
 *     System.out.println("Received audio chunk: " + chunk.length + " bytes");
 * });
 * 
 * client.onComplete(metadata -> {
 *     System.out.println("Stream completed: " + metadata);
 * });
 * 
 * TTSRequest request = TTSRequest.builder()
 *     .text("Hello world")
 *     .voiceId("mamre")
 *     .build();
 * 
 * client.connectAndSynthesize(request);
 * </pre>
 */
@ClientEndpoint
public class TtsWebSocketClient {
    
    private final SDKConfig config;
    private final Gson gson = new Gson();
    private Session session;
    private Consumer<byte[]> audioChunkHandler;
    private Consumer<StreamMetadata> completionHandler;
    private Consumer<Throwable> errorHandler;
    private String currentVoiceId;
    
    public TtsWebSocketClient(SDKConfig config) {
        this.config = config;
    }
    
    /**
     * Set handler for audio chunks
     */
    public TtsWebSocketClient onAudioChunk(Consumer<byte[]> handler) {
        this.audioChunkHandler = handler;
        return this;
    }
    
    /**
     * Set handler for stream completion
     */
    public TtsWebSocketClient onComplete(Consumer<StreamMetadata> handler) {
        this.completionHandler = handler;
        return this;
    }
    
    /**
     * Set handler for errors
     */
    public TtsWebSocketClient onError(Consumer<Throwable> handler) {
        this.errorHandler = handler;
        return this;
    }
    
    /**
     * Connect to WebSocket endpoint with voiceId in URL path
     * 
     * @param voiceId Voice ID to use (part of URL path)
     * @return CompletableFuture that completes when connected
     */
    public CompletableFuture<Void> connect(String voiceId) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        this.currentVoiceId = voiceId;
        
        try {
            // Build WebSocket URL: /api/v1/tts/stream/{voiceId}
            String baseUrl = config.getBaseUrl()
                .replace("https://", "wss://")
                .replace("http://", "ws://");
            
            // Remove trailing slash if present
            if (baseUrl.endsWith("/")) {
                baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
            }
            
            String wsUrl = baseUrl + "/api/v1/tts/stream/" + voiceId;
            
            // Add authorization header if API key is provided
            ClientEndpointConfig.Builder configBuilder = ClientEndpointConfig.Builder.create();
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                configBuilder.configurator(new ClientEndpointConfig.Configurator() {
                    @Override
                    public void beforeRequest(Map<String, List<String>> headers) {
                        headers.put("Authorization", Collections.singletonList(config.getAuthHeader()));
                    }
                });
            }
            
            URI uri = URI.create(wsUrl);
            
            // Use Tyrus WebSocket client (already in dependencies)
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            
            container.connectToServer(new Endpoint() {
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    TtsWebSocketClient.this.session = session;
                    
                    // Add message handler for text messages (metadata, completion, errors)
                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                            handleTextMessage(message);
                        }
                    });
                    
                    // Add message handler for binary messages (audio chunks)
                    session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
                        @Override
                        public void onMessage(ByteBuffer message) {
                            handleBinaryMessage(message);
                        }
                    });
                    
                    future.complete(null);
                }
                
                @Override
                public void onError(Session session, Throwable throwable) {
                    if (errorHandler != null) {
                        errorHandler.accept(throwable);
                    }
                    future.completeExceptionally(throwable);
                }
                
                @Override
                public void onClose(Session session, CloseReason closeReason) {
                    // Handle close
                    if (closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
                        if (errorHandler != null) {
                            errorHandler.accept(new TtsException("WebSocket closed: " + closeReason.getReasonPhrase()));
                        }
                    }
                }
            }, configBuilder.build(), uri);
            
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    /**
     * Connect and synthesize in one call (convenience method)
     * 
     * @param request TTS request with voiceId and text
     * @return CompletableFuture that completes when connected and request sent
     */
    public CompletableFuture<Void> connectAndSynthesize(TTSRequest request) {
        return connect(request.getVoiceId()).thenRun(() -> {
            synthesize(request);
        });
    }
    
    /**
     * Send TTS synthesis request via WebSocket
     * 
     * Note: voiceId is already in the URL path, so we only send text and configuration
     */
    public void synthesize(TTSRequest request) {
        if (session == null || !session.isOpen()) {
            throw new IllegalStateException("WebSocket not connected");
        }
        
        // Verify voiceId matches the one used in connection
        if (!request.getVoiceId().equals(currentVoiceId)) {
            throw new IllegalArgumentException("VoiceId mismatch. Connected with: " + currentVoiceId + 
                ", but request has: " + request.getVoiceId());
        }
        
        try {
            // Build request message (voiceId is in URL, so we only send text and config)
            JsonObject message = new JsonObject();
            message.addProperty("text", request.getText());
            
            // Add speed if specified
            if (request.getSpeed() != null && request.getSpeed() != 1.0) {
                JsonObject voiceSettings = new JsonObject();
                voiceSettings.addProperty("speed", request.getSpeed());
                message.add("voiceSettings", voiceSettings);
            }
            
            // Add format configuration
            if (request.getOutputContainer() != null) {
                message.addProperty("outputContainer", request.getOutputContainer());
            }
            if (request.getOutputEncoding() != null) {
                message.addProperty("outputEncoding", request.getOutputEncoding());
            }
            if (request.getOutputSampleRate() != null) {
                message.addProperty("outputSampleRate", request.getOutputSampleRate());
            }
            if (request.getOutputBitDepth() != null) {
                message.addProperty("outputBitDepth", request.getOutputBitDepth());
            }
            if (request.getOutputChannels() != null) {
                message.addProperty("outputChannels", request.getOutputChannels());
            }
            if (request.getOutputFrameDurationMs() != null) {
                message.addProperty("outputFrameDurationMs", request.getOutputFrameDurationMs());
            }
            
            // Send message
            session.getBasicRemote().sendText(gson.toJson(message));
            
        } catch (IOException e) {
            throw new TtsException("Failed to send WebSocket message", e);
        }
    }
    
    /**
     * Handle text messages (JSON metadata, events, etc.)
     */
    private void handleTextMessage(String message) {
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : null;
            
            if ("audio_chunk".equals(type)) {
                // Audio chunk as base64 in JSON
                if (json.has("chunk") && audioChunkHandler != null) {
                    String chunkBase64 = json.get("chunk").getAsString();
                    byte[] audioChunk = Base64.getDecoder().decode(chunkBase64);
                    audioChunkHandler.accept(audioChunk);
                }
                
            } else if ("done".equals(type)) {
                // Stream completed
                if (completionHandler != null) {
                    StreamMetadata metadata = new StreamMetadata(
                        json.has("conversationId") ? json.get("conversationId").getAsString() : null,
                        json.has("totalChunks") ? json.get("totalChunks").getAsLong() : 0,
                        json.has("totalBytes") ? json.get("totalBytes").getAsLong() : 0,
                        json.has("durationMs") ? json.get("durationMs").getAsLong() : 0,
                        json.has("creditsUsed") ? json.get("creditsUsed").getAsDouble() : 0.0
                    );
                    completionHandler.accept(metadata);
                }
                
            } else if ("error".equals(type)) {
                // Error occurred
                String errorMsg = json.has("error") ? json.get("error").getAsString() : "Unknown error";
                TtsException exception = new TtsException(errorMsg);
                if (errorHandler != null) {
                    errorHandler.accept(exception);
                } else {
                    throw exception;
                }
            }
            
        } catch (Exception e) {
            if (errorHandler != null) {
                errorHandler.accept(e);
            }
        }
    }
    
    /**
     * Handle binary messages (raw audio data)
     */
    private void handleBinaryMessage(ByteBuffer buffer) {
        if (audioChunkHandler != null) {
            byte[] audioChunk = new byte[buffer.remaining()];
            buffer.get(audioChunk);
            audioChunkHandler.accept(audioChunk);
        }
    }
    
    /**
     * Disconnect from WebSocket
     */
    public void disconnect() {
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    /**
     * Check if connected
     */
    public boolean isConnected() {
        return session != null && session.isOpen();
    }
    
    /**
     * Stream metadata
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
}
