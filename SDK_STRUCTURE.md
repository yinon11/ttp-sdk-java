# SDK Structure Documentation

This document describes the organization of the TalkToPC Java SDK.

## Package Structure

The SDK is organized into clear modules:

```
com.talktopc.sdk/
├── common/          # Shared utilities and configuration
│   ├── SDKConfig.java      # API credentials and connection settings
│   └── AudioFormat.java    # Audio format specification
│
├── tts/            # Text-to-Speech SDK Module
│   ├── TtsSDK.java         # Main TTS SDK entry point
│   ├── client/
│   │   ├── TtsRestClient.java      # REST client for complete audio
│   │   └── TtsStreamClient.java     # Streaming client (SSE)
│   ├── models/
│   │   ├── TTSRequest.java         # TTS request configuration
│   │   └── TTSResponse.java        # TTS response with audio and metadata
│   ├── exception/
│   │   └── TtsException.java       # TTS-specific exceptions
│   └── examples/
│       ├── TtsExamples.java        # Basic TTS examples
│       └── PhoneSystemExamples.java # Phone system integration examples
│
├── agent/          # Agent SDK Module (for injecting/overriding agents)
│   ├── AgentSDK.java       # Main Agent SDK entry point (placeholder)
│   ├── models/
│   │   └── AgentConfig.java        # Agent configuration (placeholder)
│   └── exception/
│       └── AgentException.java     # Agent-specific exceptions
│
└── [deprecated]    # Backward compatibility classes
    ├── VoiceSDK.java               # Deprecated - use TtsSDK instead
    ├── models/
    │   ├── TTSRequest.java         # Deprecated - use tts.models.TTSRequest
    │   └── TTSResponse.java        # Deprecated - use tts.models.TTSResponse
    ├── exception/
    │   └── TtsException.java       # Deprecated - use tts.exception.TtsException
    └── config/
        └── SDKConfig.java          # Deprecated - use common.SDKConfig
```

## Module Overview

### Common Module (`com.talktopc.sdk.common`)

Shared utilities used across all SDK modules:

- **SDKConfig**: API credentials, base URL, timeouts
- **AudioFormat**: Audio format specification (container, encoding, sample rate, etc.)

### TTS SDK Module (`com.talktopc.sdk.tts`)

Complete Text-to-Speech SDK for converting text to audio:

- **TtsSDK**: Main entry point with builder pattern
- **Features**:
  - Simple TTS (complete audio file)
  - Streaming TTS (real-time chunks via Server-Sent Events)
  - Multiple voice support
  - Audio format configuration (PCM, PCMU, PCMA, WAV)
  - Speed control
  - Phone system presets

**Usage Example:**
```java
import com.talktopc.sdk.tts.TtsSDK;
import com.talktopc.sdk.tts.models.TTSRequest;

TtsSDK sdk = TtsSDK.builder()
    .apiKey("your-api-key")
    .baseUrl("https://api.talktopc.com")
    .build();

// Simple TTS
byte[] audio = sdk.textToSpeech("Hello world", "mamre");

// Streaming TTS
sdk.textToSpeechStream("Hello world", "mamre", chunk -> {
    phoneSystem.playAudio(chunk);
});
```

### Agent SDK Module (`com.talktopc.sdk.agent`)

SDK for injecting or overriding agents in TalkToPC conversations.

**Status**: Placeholder structure - implementation pending based on requirements.

**Planned Features**:
- Inject custom agent logic
- Override agent responses
- Customize agent behavior

**Usage Example (planned):**
```java
import com.talktopc.sdk.agent.AgentSDK;
import com.talktopc.sdk.agent.models.AgentConfig;

AgentSDK sdk = AgentSDK.builder()
    .apiKey("your-api-key")
    .build();

// Inject custom agent (to be implemented)
sdk.injectAgent(agentId, customAgentConfig);
```

## Backward Compatibility

The SDK maintains backward compatibility with the old package structure:

- `com.talktopc.sdk.VoiceSDK` → delegates to `com.talktopc.sdk.tts.TtsSDK`
- `com.talktopc.sdk.models.TTSRequest` → wrapper for `com.talktopc.sdk.tts.models.TTSRequest`
- `com.talktopc.sdk.models.TTSResponse` → wrapper for `com.talktopc.sdk.tts.models.TTSResponse`
- `com.talktopc.sdk.exception.TtsException` → extends `com.talktopc.sdk.tts.exception.TtsException`
- `com.talktopc.sdk.config.SDKConfig` → extends `com.talktopc.sdk.common.SDKConfig`

All backward compatibility classes are marked with `@Deprecated` and will delegate to the new module classes.

## Migration Guide

### Migrating from VoiceSDK to TtsSDK

**Old Code:**
```java
import com.talktopc.sdk.VoiceSDK;
import com.talktopc.sdk.models.TTSRequest;

VoiceSDK sdk = VoiceSDK.builder()
    .apiKey("your-api-key")
    .build();

byte[] audio = sdk.textToSpeech("Hello", "mamre");
```

**New Code:**
```java
import com.talktopc.sdk.tts.TtsSDK;
import com.talktopc.sdk.tts.models.TTSRequest;

TtsSDK sdk = TtsSDK.builder()
    .apiKey("your-api-key")
    .build();

byte[] audio = sdk.textToSpeech("Hello", "mamre");
```

The API is identical, only the package names change.

## Examples

Examples are located in:
- `src/main/java/com/talktopc/sdk/tts/examples/` - TTS SDK examples
- `examples/` - Root-level examples (may reference different SDKs)

## Future Enhancements

1. **Agent SDK Implementation**: Complete the Agent SDK module based on requirements
2. **WebSocket SDK**: If needed, create a separate WebSocket-based SDK module
3. **STT SDK**: If needed, create a Speech-to-Text SDK module

## Notes

- All SDK modules share the same `SDKConfig` from the common package
- Each module has its own exception types for better error handling
- The TTS SDK is production-ready and fully implemented
- The Agent SDK is a placeholder structure ready for implementation
