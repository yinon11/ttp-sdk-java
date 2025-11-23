package com.talktopc.sdk.examples;

import com.talktopc.sdk.VoiceSDK;
import com.talktopc.sdk.VoiceSDKConfig;
import com.talktopc.sdk.AudioFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Example: Forward TTP audio to phone system (PCMU)
 * 
 * This demonstrates the key use case for the backend SDK:
 * receiving raw PCMU/PCMA audio and forwarding it to phone systems
 * without any decoding/conversion.
 */
public class PhoneForwardingExample {
    private static final Logger logger = LoggerFactory.getLogger(PhoneForwardingExample.class);

    public static void main(String[] args) {
        // Configure SDK for phone system (PCMU, 8kHz)
        VoiceSDKConfig config = new VoiceSDKConfig();
        config.setWebsocketUrl("wss://speech.talktopc.com/ws/conv?agentId=xxx&appId=yyy");
        config.setAgentId("agent_123");
        config.setAppId("your_app_id");
        
        // Request PCMU for phone systems
        config.setOutputContainer("raw");
        config.setOutputEncoding("pcmu");      // ← Phone systems use PCMU
        config.setOutputSampleRate(8000);       // ← Phone standard
        config.setOutputBitDepth(16);
        config.setOutputChannels(1);
        config.setProtocolVersion(2);

        VoiceSDK sdk = new VoiceSDK(config);

        // Listen for format negotiation
        sdk.onFormatNegotiated(format -> {
            logger.info("Format negotiated: {}", format);
            // Should be: AudioFormat{container='raw', encoding='pcmu', sampleRate=8000, ...}
        });

        // ✅ KEY: Listen for raw PCMU audio (NOT decoded!)
        sdk.onAudioDataWithFormat((audioData, format) -> {
            logger.info("Received {} bytes of {} audio", 
                       audioData.length, format.getEncoding());
            
            // Forward directly to phone system (raw PCMU, no conversion!)
            forwardToPhoneSystem(audioData);
        });

        // Listen for messages
        sdk.onMessage(message -> {
            String type = message.has("t") ? message.get("t").getAsString() : null;
            if ("agent_response".equals(type)) {
                logger.info("Agent: {}", message.get("agent_response"));
            }
        });

        // Connect
        CompletableFuture<Void> connectFuture = sdk.connect();
        connectFuture.thenRun(() -> {
            logger.info("Connected! Ready to forward audio.");
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

    private static void forwardToPhoneSystem(byte[] pcmuAudio) {
        // Your phone system integration here
        // pcmuAudio is raw PCMU bytes, ready to send to phone!
        logger.debug("Forwarding {} bytes to phone system", pcmuAudio.length);
        // phoneSystem.sendAudio(pcmuAudio);
    }
}

