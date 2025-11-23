package com.talktopc.sdk.examples;

import com.talktopc.sdk.VoiceSDK;
import com.talktopc.sdk.VoiceSDKConfig;
import com.talktopc.sdk.AudioFormat;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Simple chat example
 * 
 * Demonstrates basic usage of the SDK for receiving PCM audio.
 */
public class SimpleChatExample {
    private static final Logger logger = LoggerFactory.getLogger(SimpleChatExample.class);

    public static void main(String[] args) {
        VoiceSDKConfig config = new VoiceSDKConfig();
        config.setWebsocketUrl("wss://speech.talktopc.com/ws/conv?agentId=xxx&appId=yyy");
        config.setAgentId("agent_123");
        config.setAppId("your_app_id");
        config.setOutputEncoding("pcm");  // PCM for general use
        config.setOutputSampleRate(16000);
        config.setOutputBitDepth(16);
        config.setOutputChannels(1);
        config.setProtocolVersion(2);

        VoiceSDK sdk = new VoiceSDK(config);

        sdk.onFormatNegotiated(format -> {
            logger.info("Format negotiated: {}", format);
        });

        sdk.onAudioData(audioData -> {
            // Save or process audio
            logger.info("Received {} bytes of audio", audioData.length);
        });

        sdk.onMessage(message -> {
            String type = message.has("t") ? message.get("t").getAsString() : null;
            if ("agent_response".equals(type)) {
                logger.info("Agent: {}", message.get("agent_response"));
            }
        });

        CompletableFuture<Void> connectFuture = sdk.connect();
        connectFuture.thenRun(() -> {
            logger.info("Connected!");
            
            // Send a text message
            JsonObject msg = new JsonObject();
            msg.addProperty("t", "user_message");
            msg.addProperty("text", "Hello!");
            sdk.sendMessage(msg);
        }).exceptionally(error -> {
            logger.error("Connection failed", error);
            return null;
        });

        // Keep running
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            logger.info("Shutting down...");
            sdk.disconnect();
        }
    }
}

