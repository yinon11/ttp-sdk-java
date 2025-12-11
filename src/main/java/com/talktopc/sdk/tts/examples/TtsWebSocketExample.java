package com.talktopc.sdk.tts.examples;

import com.talktopc.sdk.tts.TtsSDK;
import com.talktopc.sdk.tts.models.TTSRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Example: TTS using WebSocket
 * 
 * This example demonstrates how to use WebSocket for TTS streaming.
 * 
 * WebSocket endpoint: /api/v1/tts/stream/{voiceId}
 * 
 * Advantages over SSE:
 * - Lower latency (persistent connection)
 * - Bidirectional communication
 * - Binary audio support (no base64 encoding overhead)
 * 
 * Usage:
 * <pre>
 * TtsSDK sdk = TtsSDK.builder()
 *     .apiKey("your-api-key")
 *     .build();
 * 
 * // Simple usage
 * sdk.textToSpeechWebSocket("Hello world", "mamre", chunk -> {
 *     System.out.println("Received: " + chunk.length + " bytes");
 * });
 * 
 * // Advanced usage with callbacks
 * TTSRequest request = TTSRequest.builder()
 *     .text("Hello world")
 *     .voiceId("mamre")
 *     .phoneSystem()
 *     .build();
 * 
 * sdk.textToSpeechWebSocket(request, 
 *     chunk -> processAudio(chunk),
 *     metadata -> System.out.println("Done: " + metadata),
 *     error -> System.err.println("Error: " + error)
 * );
 * </pre>
 */
public class TtsWebSocketExample {
    private static final Logger logger = LoggerFactory.getLogger(TtsWebSocketExample.class);
    
    public static void main(String[] args) {
        String apiKey = System.getenv("TALKTOPC_API_KEY");
        if (apiKey == null) {
            System.err.println("Please set TALKTOPC_API_KEY environment variable");
            System.exit(1);
        }
        
        // Initialize SDK
        TtsSDK sdk = TtsSDK.builder()
            .apiKey(apiKey)
            .baseUrl("https://api.talktopc.com")
            .build();
        
        // Run examples
        example1_SimpleWebSocket(sdk);
        example2_WebSocketWithCallbacks(sdk);
        example3_WebSocketPhoneSystem(sdk);
    }
    
    /**
     * Example 1: Simple WebSocket usage
     */
    private static void example1_SimpleWebSocket(TtsSDK sdk) {
        System.out.println("\n=== Example 1: Simple WebSocket TTS ===");
        
        CompletableFuture<Void> future = sdk.textToSpeechWebSocket(
            "Hello world, this is a simple WebSocket TTS example",
            "mamre",
            audioChunk -> {
                System.out.println("   📦 Received chunk: " + audioChunk.length + " bytes");
                // Process audio chunk (e.g., send to phone system, play audio, etc.)
            }
        );
        
        future.thenRun(() -> {
            System.out.println("✅ Connected and request sent");
        }).exceptionally(error -> {
            System.err.println("❌ Error: " + error.getMessage());
            return null;
        });
        
        // Wait for completion
        try {
            Thread.sleep(10000); // Wait 10 seconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Example 2: WebSocket with completion and error callbacks
     */
    private static void example2_WebSocketWithCallbacks(TtsSDK sdk) {
        System.out.println("\n=== Example 2: WebSocket with Callbacks ===");
        
        TTSRequest request = TTSRequest.builder()
            .text("This is a WebSocket example with full callbacks")
            .voiceId("mamre")
            .speed(1.0)
            .build();
        
        CompletableFuture<Void> future = sdk.textToSpeechWebSocket(
            request,
            audioChunk -> {
                // Handle each audio chunk
                System.out.println("   📦 Chunk: " + audioChunk.length + " bytes");
            },
            metadata -> {
                // Handle completion
                System.out.println("✅ Stream completed:");
                System.out.println("   Total chunks: " + metadata.getTotalChunks());
                System.out.println("   Total bytes: " + metadata.getTotalBytes());
                System.out.println("   Duration: " + metadata.getDurationMs() + " ms");
                System.out.println("   Credits: " + metadata.getCreditsUsed());
            },
            error -> {
                // Handle errors
                System.err.println("❌ Stream error: " + error.getMessage());
                error.printStackTrace();
            }
        );
        
        future.thenRun(() -> {
            System.out.println("✅ Connected and request sent");
        }).exceptionally(error -> {
            System.err.println("❌ Connection error: " + error.getMessage());
            return null;
        });
        
        // Wait for completion
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Example 3: WebSocket for phone system (PCMU @ 8kHz)
     */
    private static void example3_WebSocketPhoneSystem(TtsSDK sdk) {
        System.out.println("\n=== Example 3: WebSocket for Phone System ===");
        
        TTSRequest request = TTSRequest.builder()
            .text("Hello caller, thank you for calling. How can I help you today?")
            .voiceId("en-US-female")
            .phoneSystem()  // PCMU @ 8kHz, 20ms frames
            .build();
        
        System.out.println("📞 Connecting to WebSocket for phone system audio...");
        
        CompletableFuture<Void> future = sdk.textToSpeechWebSocket(
            request,
            audioChunk -> {
                // audioChunk is PCMU @ 8kHz, ready to send to phone system
                System.out.println("   📦 PCMU chunk: " + audioChunk.length + " bytes");
                // phoneConnection.sendAudio(audioChunk);
            },
            metadata -> {
                System.out.println("✅ Phone call audio completed:");
                System.out.println("   Total chunks: " + metadata.getTotalChunks());
                System.out.println("   Total bytes: " + metadata.getTotalBytes());
            },
            error -> {
                System.err.println("❌ Error: " + error.getMessage());
            }
        );
        
        future.thenRun(() -> {
            System.out.println("✅ Connected and streaming phone audio");
        }).exceptionally(error -> {
            System.err.println("❌ Connection error: " + error.getMessage());
            return null;
        });
        
        // Wait for completion
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
