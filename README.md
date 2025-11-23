# TTP Agent SDK - Java

Backend SDK for the TTP Agent WebSocket API. Designed for server-side applications that need to forward audio to phone systems or process audio without browser dependencies.

## Key Features

- ✅ **Format Negotiation** - Protocol v2 with automatic format negotiation
- ✅ **Raw Audio Pass-through** - Receive PCMU/PCMA audio without decoding (perfect for phone systems)
- ✅ **WebSocket Communication** - Full WebSocket API support
- ✅ **Event-Driven** - Clean event listener API
- ✅ **Auto-Reconnect** - Automatic reconnection on disconnect

## Installation

### Maven

```xml
<dependency>
    <groupId>com.talktopc</groupId>
    <artifactId>ttp-agent-sdk-java</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
dependencies {
    implementation 'com.talktopc:ttp-agent-sdk-java:1.0.0'
}
```

## Quick Start

### Phone System Integration (PCMU)

```java
import com.talktopc.sdk.VoiceSDK;
import com.talktopc.sdk.VoiceSDKConfig;
import com.talktopc.sdk.AudioFormat;

// Configure for phone system
VoiceSDKConfig config = new VoiceSDKConfig();
config.setWebsocketUrl("wss://speech.talktopc.com/ws/conv?agentId=xxx&appId=yyy");
config.setOutputEncoding("pcmu");      // Phone systems use PCMU
config.setOutputSampleRate(8000);       // Phone standard
config.setOutputChannels(1);
config.setProtocolVersion(2);

VoiceSDK sdk = new VoiceSDK(config);

// Listen for raw PCMU audio (NOT decoded!)
sdk.onAudioDataWithFormat((audioData, format) -> {
    // Forward directly to phone system - no conversion needed!
    phoneSystem.sendAudio(audioData);
});

sdk.connect();
```

### General Use (PCM)

```java
VoiceSDKConfig config = new VoiceSDKConfig();
config.setWebsocketUrl("wss://speech.talktopc.com/ws/conv?agentId=xxx&appId=yyy");
config.setOutputEncoding("pcm");
config.setOutputSampleRate(16000);
config.setOutputChannels(1);

VoiceSDK sdk = new VoiceSDK(config);

sdk.onAudioData(audioData -> {
    // Process PCM audio
    processAudio(audioData);
});

sdk.onFormatNegotiated(format -> {
    System.out.println("Format: " + format);
});

sdk.connect();
```

## Configuration

### VoiceSDKConfig Options

| Option | Default | Description |
|--------|---------|-------------|
| `websocketUrl` | *required* | WebSocket URL with agentId and appId |
| `outputEncoding` | `"pcm"` | `"pcm"`, `"pcmu"`, or `"pcma"` |
| `outputSampleRate` | `16000` | Sample rate in Hz (8000 for phones, 16000/44100 for general) |
| `outputBitDepth` | `16` | Bit depth (16 or 24) |
| `outputChannels` | `1` | Channel count (1 = mono) |
| `outputContainer` | `"raw"` | `"raw"` or `"wav"` |
| `protocolVersion` | `2` | Protocol version (use 2) |
| `autoReconnect` | `true` | Auto-reconnect on disconnect |

## API Reference

### VoiceSDK

#### Methods

- `CompletableFuture<Void> connect()` - Connect to WebSocket server
- `void disconnect()` - Disconnect from server
- `void sendAudio(byte[] audioData)` - Send audio data to server
- `void sendMessage(String message)` - Send text message
- `void sendMessage(JsonObject message)` - Send JSON message
- `boolean isConnected()` - Check connection status
- `AudioFormat getNegotiatedOutputFormat()` - Get negotiated format

#### Event Listeners

- `onAudioData(Consumer<byte[]> listener)` - Raw audio data
- `onAudioDataWithFormat(AudioDataWithFormatListener listener)` - Audio with format info
- `onFormatNegotiated(Consumer<AudioFormat> listener)` - Format negotiation complete
- `onMessage(Consumer<JsonObject> listener)` - Text messages
- `onConnected(Runnable listener)` - Connection established
- `onDisconnected(Runnable listener)` - Connection closed
- `onError(Consumer<Throwable> listener)` - Error occurred

## Examples

See the `examples/` directory:

- `PhoneForwardingExample.java` - Forward PCMU audio to phone system
- `SimpleChatExample.java` - Basic chat with PCM audio

## Key Differences from Frontend SDK

| Feature | Frontend SDK | Backend SDK (Java) |
|---------|-------------|-------------------|
| **Audio Decoding** | Decodes PCMU/PCMA to PCM | Pass-through raw audio |
| **Use Case** | Browser playback | Phone systems, server processing |
| **Environment** | Browser (JavaScript) | Server (JVM) |
| **Format Conversion** | Converts to browser format | No conversion (pass-through) |

## Phone System Integration

The backend SDK is designed for phone system integration:

1. **Request PCMU/PCMA** - Configure `outputEncoding` to `"pcmu"` or `"pcma"`
2. **Receive Raw Audio** - Audio arrives as raw PCMU/PCMA bytes
3. **Forward Directly** - Send directly to phone system without conversion

```java
// Request PCMU
config.setOutputEncoding("pcmu");
config.setOutputSampleRate(8000);

// Receive raw PCMU bytes
sdk.onAudioData(pcmuBytes -> {
    // Forward to phone - no conversion!
    phoneSystem.sendAudio(pcmuBytes);
});
```

## Protocol v2 Format Negotiation

The SDK automatically handles format negotiation:

1. **Send Hello** - SDK sends format request in `hello` message
2. **Receive Ack** - Server responds with `hello_ack` and negotiated format
3. **Audio Streaming** - Audio arrives in negotiated format

The negotiated format is available via `getNegotiatedOutputFormat()` or the `onFormatNegotiated` event.

## Requirements

- Java 11 or higher
- Maven or Gradle
- WebSocket connection to TTP Agent API

## Dependencies

- Tyrus WebSocket Client (JSR-356)
- Gson (JSON processing)
- SLF4J (Logging)

## License

MIT License

## Support

- Documentation: https://cdn.talktopc.com/
- Issues: https://github.com/yinon11/ttp-sdk-front/issues

