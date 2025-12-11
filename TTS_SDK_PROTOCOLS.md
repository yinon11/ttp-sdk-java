# TTS SDK Protocols Documentation

## Current Implementation

The TTS SDK currently supports **two protocols**:

### 1. REST API (`TtsRestClient`)
- **Endpoint**: `POST /api/v1/tts/synthesize`
- **Use Case**: Get complete audio file in one request
- **Response**: Complete audio file as base64-encoded JSON
- **Best For**: Simple use cases, file generation, non-real-time applications

**Example:**
```java
TtsSDK sdk = TtsSDK.builder().apiKey("key").build();
byte[] audio = sdk.textToSpeech("Hello world", "mamre");
```

### 2. Server-Sent Events / SSE (`TtsStreamClient`)
- **Endpoint**: `POST /api/v1/tts/stream`
- **Use Case**: Stream audio chunks in real-time
- **Protocol**: HTTP with `Accept: text/event-stream` header
- **Response**: SSE stream with audio chunks as base64-encoded JSON
- **Best For**: Real-time streaming, phone systems, low-latency applications

**Example:**
```java
TtsSDK sdk = TtsSDK.builder().apiKey("key").build();
sdk.textToSpeechStream("Hello world", "mamre", chunk -> {
    phoneSystem.playAudio(chunk);
});
```

## WebSocket Support

### Status: **✅ Implemented**

WebSocket support is now available for TTS streaming!

**Endpoint**: `/api/v1/tts/stream/{voiceId}`

**Features**:
- VoiceId is part of the URL path
- Text and configuration sent as JSON message
- Audio chunks received as binary messages (no base64 encoding)
- Metadata/completion received as JSON text messages
- Lower latency than SSE
- Bidirectional communication

### Why WebSocket?

WebSocket would provide:
- **Lower latency** - Persistent connection, no HTTP overhead
- **Bidirectional** - Can send requests and receive responses on same connection
- **Better for real-time** - Ideal for phone systems, live streaming
- **Binary support** - Can send raw audio bytes directly (no base64 encoding)

### Current Workaround

For real-time streaming, use **SSE** (`TtsStreamClient`):
- Lower latency than REST
- Good enough for most use cases
- Already implemented and working

## Protocol Comparison

| Feature | REST | SSE | WebSocket |
|---------|------|-----|-----------|
| **Latency** | High | Medium | Low |
| **Connection** | Request/Response | One-way stream | Bidirectional |
| **Audio Format** | Base64 JSON | Base64 JSON (SSE) | Binary (raw bytes) |
| **Use Case** | Complete files | Real-time streaming | Real-time + bidirectional |
| **Implementation** | ✅ Complete | ✅ Complete | ✅ Complete |

## Examples

### REST Example
See: `TtsExamples.java` - `example1_SimpleTts()`

### SSE Example  
See: `TtsExamples.java` - `example4_StreamingTts()`

### WebSocket Example
See: `TtsWebSocketExample.java` - Complete working example

**Simple usage:**
```java
TtsSDK sdk = TtsSDK.builder().apiKey("key").build();

sdk.textToSpeechWebSocket("Hello world", "mamre", chunk -> {
    System.out.println("Received: " + chunk.length + " bytes");
});
```

**Advanced usage:**
```java
TTSRequest request = TTSRequest.builder()
    .text("Hello world")
    .voiceId("mamre")
    .phoneSystem()
    .build();

sdk.textToSpeechWebSocket(request,
    chunk -> processAudio(chunk),
    metadata -> System.out.println("Done: " + metadata),
    error -> System.err.println("Error: " + error)
);
```

## Recommendations

1. **For simple TTS**: Use REST (`textToSpeech()`)
2. **For streaming**: Use SSE (`textToSpeechStream()`) or WebSocket (`textToSpeechWebSocket()`)
3. **For lowest latency**: Use WebSocket (`textToSpeechWebSocket()`) - best for phone systems
4. **For bidirectional needs**: Use WebSocket - can send multiple requests on same connection

## WebSocket Protocol Details

**Endpoint**: `/api/v1/tts/stream/{voiceId}`

**Connection**:
- URL: `wss://api.talktopc.com/api/v1/tts/stream/{voiceId}`
- Authorization: Bearer token in header
- Protocol: WebSocket (ws:// or wss://)

**Request Message** (JSON text):
```json
{
  "text": "Hello world",
  "voiceSettings": {
    "speed": 1.0
  },
  "outputContainer": "raw",
  "outputEncoding": "pcmu",
  "outputSampleRate": 8000,
  "outputBitDepth": 16,
  "outputChannels": 1,
  "outputFrameDurationMs": 20
}
```

**Response Messages**:
- **Binary messages**: Raw audio chunks (byte arrays)
- **Text messages**: JSON with metadata, completion, or errors
  - `{"type": "done", "conversationId": "...", "totalChunks": 10, ...}`
  - `{"type": "error", "error": "Error message"}`

## Notes

- The `VoiceSDKConfig` class with `websocketUrl` is for a **different SDK** (conversation/agent SDK), not TTS
- The examples `SimpleChatExample` and `PhoneForwardingExample` are for the conversation SDK, not TTS SDK
- TTS SDK now supports: REST + SSE + **WebSocket** ✅
- WebSocket is recommended for phone systems and low-latency applications
