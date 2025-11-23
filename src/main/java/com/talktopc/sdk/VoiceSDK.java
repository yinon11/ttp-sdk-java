package com.talktopc.sdk;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * VoiceSDK - Backend SDK for TTP Agent WebSocket API
 * 
 * Key Features:
 * - Format negotiation (v2 protocol)
 * - Audio streaming with pass-through (no decoding)
 * - Support for PCMU/PCMA for phone systems
 * 
 * Usage:
 * <pre>
 * VoiceSDKConfig config = new VoiceSDKConfig();
 * config.setWebsocketUrl("wss://speech.talktopc.com/ws/conv?agentId=xxx&appId=yyy");
 * config.setOutputEncoding("pcmu");  // For phone systems
 * config.setOutputSampleRate(8000);
 * 
 * VoiceSDK sdk = new VoiceSDK(config);
 * sdk.onAudioData(audioData -> {
 *     // Forward raw PCMU audio to phone system
 * });
 * sdk.connect();
 * </pre>
 */
@ClientEndpoint
public class VoiceSDK {
    private static final Logger logger = LoggerFactory.getLogger(VoiceSDK.class);
    private static final Gson gson = new Gson();

    private final VoiceSDKConfig config;
    private Session session;
    private AudioFormat negotiatedOutputFormat;
    private boolean isConnected = false;
    private CompletableFuture<Void> connectFuture;

    // Event listeners
    private final List<Consumer<byte[]>> audioDataListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<AudioFormat>> formatNegotiatedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<JsonObject>> messageListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> connectedListeners = new CopyOnWriteArrayList<>();
    private final List<Runnable> disconnectedListeners = new CopyOnWriteArrayList<>();
    private final List<Consumer<Throwable>> errorListeners = new CopyOnWriteArrayList<>();

    public VoiceSDK(VoiceSDKConfig config) {
        this.config = config;
    }

    /**
     * Connect to WebSocket server
     * @return CompletableFuture that completes when connected
     */
    public CompletableFuture<Void> connect() {
        if (connectFuture != null && !connectFuture.isDone()) {
            logger.warn("Connection already in progress");
            return connectFuture;
        }

        connectFuture = new CompletableFuture<>();

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create(config.getWebsocketUrl());
            
            logger.info("Connecting to: {}", uri);
            
            container.connectToServer(this, uri);
            
        } catch (Exception e) {
            logger.error("Failed to connect", e);
            connectFuture.completeExceptionally(e);
            notifyError(e);
        }

        return connectFuture;
    }

    @OnOpen
    public void onOpen(Session session) {
        logger.info("WebSocket connected");
        this.session = session;
        this.isConnected = true;
        
        // Complete connection future
        if (connectFuture != null && !connectFuture.isDone()) {
            connectFuture.complete(null);
        }
        
        // Send hello message with format negotiation
        sendHelloMessage();
        
        // Notify listeners
        connectedListeners.forEach(Runnable::run);
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        logger.info("WebSocket closed: {} - {}", closeReason.getCloseCode(), closeReason.getReasonPhrase());
        this.isConnected = false;
        this.session = null;
        this.connectFuture = null;
        
        // Notify listeners
        disconnectedListeners.forEach(Runnable::run);
        
        // Auto-reconnect if enabled
        if (config.isAutoReconnect() && closeReason.getCloseCode() != CloseReason.CloseCodes.NORMAL_CLOSURE) {
            logger.info("Auto-reconnecting in 3 seconds...");
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    connect();
                } catch (Exception e) {
                    logger.error("Auto-reconnect failed", e);
                }
            }).start();
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        logger.error("WebSocket error", error);
        notifyError(error);
    }

    @OnMessage
    public void onMessage(String message) {
        // Text message (JSON)
        try {
            JsonObject json = gson.fromJson(message, JsonObject.class);
            String type = json.has("t") ? json.get("t").getAsString() : null;
            
            logger.debug("Received message: {}", type);
            
            if ("hello_ack".equals(type)) {
                handleHelloAck(json);
            }
            
            // Notify message listeners
            messageListeners.forEach(listener -> listener.accept(json));
            
        } catch (Exception e) {
            logger.error("Error parsing message", e);
            notifyError(e);
        }
    }

    @OnMessage
    public void onMessage(ByteBuffer buffer) {
        // Binary message (audio data)
        byte[] audioData = new byte[buffer.remaining()];
        buffer.get(audioData);
        
        logger.debug("Received audio: {} bytes, format: {}", 
                    audioData.length, negotiatedOutputFormat);
        
        // ✅ KEY: Pass through raw audio (NO decoding!)
        // Frontend SDK would decode PCMU/PCMA here, but backend doesn't
        notifyAudioData(audioData);
    }

    /**
     * Send hello message with format negotiation
     */
    private void sendHelloMessage() {
        JsonObject hello = new JsonObject();
        hello.addProperty("t", "hello");
        hello.addProperty("v", config.getProtocolVersion());
        
        // Input format
        JsonObject inputFormat = new JsonObject();
        inputFormat.addProperty("encoding", config.getInputEncoding());
        inputFormat.addProperty("sampleRate", config.getInputSampleRate());
        inputFormat.addProperty("channels", config.getInputChannels());
        inputFormat.addProperty("bitDepth", config.getInputBitDepth());
        hello.add("inputFormat", inputFormat);
        
        // Requested output format
        JsonObject requestedOutputFormat = new JsonObject();
        requestedOutputFormat.addProperty("encoding", config.getOutputEncoding());
        requestedOutputFormat.addProperty("sampleRate", config.getOutputSampleRate());
        requestedOutputFormat.addProperty("channels", config.getOutputChannels());
        requestedOutputFormat.addProperty("bitDepth", config.getOutputBitDepth());
        requestedOutputFormat.addProperty("container", config.getOutputContainer());
        hello.add("requestedOutputFormat", requestedOutputFormat);
        
        hello.addProperty("outputFrameDurationMs", config.getOutputFrameDurationMs());
        
        sendMessage(hello.toString());
        
        logger.info("Sent hello message: {}", hello);
    }

    /**
     * Handle hello_ack message
     */
    private void handleHelloAck(JsonObject message) {
        if (message.has("outputAudioFormat")) {
            JsonObject formatJson = message.getAsJsonObject("outputAudioFormat");
            
            negotiatedOutputFormat = new AudioFormat(
                formatJson.get("container").getAsString(),
                formatJson.get("encoding").getAsString(),
                formatJson.get("sampleRate").getAsInt(),
                formatJson.get("bitDepth").getAsInt(),
                formatJson.get("channels").getAsInt()
            );
            
            logger.info("Format negotiated: {}", negotiatedOutputFormat);
            
            // Notify listeners
            formatNegotiatedListeners.forEach(listener -> 
                listener.accept(negotiatedOutputFormat));
        }
    }

    /**
     * Send audio data to server
     */
    public void sendAudio(byte[] audioData) {
        if (!isConnected || session == null) {
            logger.warn("Cannot send audio - not connected");
            return;
        }
        
        try {
            session.getBasicRemote().sendBinary(ByteBuffer.wrap(audioData));
        } catch (IOException e) {
            logger.error("Error sending audio", e);
            notifyError(e);
        }
    }

    /**
     * Send text message to server
     */
    public void sendMessage(String message) {
        if (!isConnected || session == null) {
            logger.warn("Cannot send message - not connected");
            return;
        }
        
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            logger.error("Error sending message", e);
            notifyError(e);
        }
    }

    /**
     * Send JSON message to server
     */
    public void sendMessage(JsonObject message) {
        sendMessage(gson.toJson(message));
    }

    /**
     * Disconnect from server
     */
    public void disconnect() {
        if (session != null) {
            try {
                session.close();
            } catch (IOException e) {
                logger.error("Error closing session", e);
            }
        }
    }

    // Event listener registration

    /**
     * Listen for audio data (raw PCMU/PCMA bytes, NOT decoded!)
     */
    public void onAudioData(Consumer<byte[]> listener) {
        audioDataListeners.add(listener);
    }

    /**
     * Listen for audio data with format info
     */
    public void onAudioDataWithFormat(AudioDataWithFormatListener listener) {
        audioDataListeners.add(data -> {
            listener.onAudioData(data, negotiatedOutputFormat);
        });
    }

    /**
     * Listen for format negotiation
     */
    public void onFormatNegotiated(Consumer<AudioFormat> listener) {
        formatNegotiatedListeners.add(listener);
    }

    /**
     * Listen for text messages
     */
    public void onMessage(Consumer<JsonObject> listener) {
        messageListeners.add(listener);
    }

    /**
     * Listen for connection events
     */
    public void onConnected(Runnable listener) {
        connectedListeners.add(listener);
    }

    /**
     * Listen for disconnection events
     */
    public void onDisconnected(Runnable listener) {
        disconnectedListeners.add(listener);
    }

    /**
     * Listen for errors
     */
    public void onError(Consumer<Throwable> listener) {
        errorListeners.add(listener);
    }

    // Private notification methods

    private void notifyAudioData(byte[] audioData) {
        audioDataListeners.forEach(listener -> {
            try {
                listener.accept(audioData);
            } catch (Exception e) {
                logger.error("Error in audio data listener", e);
            }
        });
    }

    private void notifyError(Throwable error) {
        errorListeners.forEach(listener -> {
            try {
                listener.accept(error);
            } catch (Exception e) {
                logger.error("Error in error listener", e);
            }
        });
    }

    // Getters

    public boolean isConnected() {
        return isConnected;
    }

    public AudioFormat getNegotiatedOutputFormat() {
        return negotiatedOutputFormat;
    }

    public VoiceSDKConfig getConfig() {
        return config;
    }

    /**
     * Helper interface for audio data with format
     */
    @FunctionalInterface
    public interface AudioDataWithFormatListener {
        void onAudioData(byte[] audioData, AudioFormat format);
    }
}

