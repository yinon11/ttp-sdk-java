# SDK Reorganization Summary

## Overview

The TalkToPC Java SDK has been reorganized into clear, modular packages to separate different SDK functionalities and prepare for future expansion.

## Changes Made

### 1. New Package Structure

Created three main modules:

#### **Common Module** (`com.talktopc.sdk.common`)
- `SDKConfig.java` - Shared API configuration (credentials, URLs, timeouts)
- `AudioFormat.java` - Shared audio format specification

#### **TTS SDK Module** (`com.talktopc.sdk.tts`)
Complete Text-to-Speech SDK:
- `TtsSDK.java` - Main entry point (replaces old `VoiceSDK` for TTS)
- `client/TtsRestClient.java` - REST client for complete audio
- `client/TtsStreamClient.java` - Streaming client (SSE)
- `models/TTSRequest.java` - Request configuration
- `models/TTSResponse.java` - Response with audio and metadata
- `exception/TtsException.java` - TTS-specific exceptions
- `examples/TtsExamples.java` - Basic examples
- `examples/PhoneSystemExamples.java` - Phone system integration examples

#### **Agent SDK Module** (`com.talktopc.sdk.agent`)
Placeholder structure for agent injection/override functionality:
- `AgentSDK.java` - Main entry point (placeholder)
- `models/AgentConfig.java` - Agent configuration (placeholder)
- `exception/AgentException.java` - Agent-specific exceptions

### 2. Backward Compatibility

All old classes are maintained with `@Deprecated` annotations and delegate to new modules:

- `com.talktopc.sdk.VoiceSDK` → delegates to `TtsSDK`
- `com.talktopc.sdk.models.TTSRequest` → wrapper for `tts.models.TTSRequest`
- `com.talktopc.sdk.models.TTSResponse` → wrapper for `tts.models.TTSResponse`
- `com.talktopc.sdk.exception.TtsException` → extends `tts.exception.TtsException`
- `com.talktopc.sdk.config.SDKConfig` → extends `common.SDKConfig`
- `com.talktopc.sdk.AudioFormat` → extends `common.AudioFormat`

### 3. File Organization

**Moved:**
- TTS-related classes → `com.talktopc.sdk.tts.*`
- Shared config → `com.talktopc.sdk.common.*`

**Created:**
- Agent SDK structure → `com.talktopc.sdk.agent.*`
- New TTS examples → `com.talktopc.sdk.tts.examples.*`

**Removed:**
- Duplicate client files from old `client/` directory

## Migration Guide

### For TTS SDK Users

**Old Code:**
```java
import com.talktopc.sdk.VoiceSDK;
import com.talktopc.sdk.models.TTSRequest;

VoiceSDK sdk = VoiceSDK.builder()
    .apiKey("your-api-key")
    .build();
```

**New Code (Recommended):**
```java
import com.talktopc.sdk.tts.TtsSDK;
import com.talktopc.sdk.tts.models.TTSRequest;

TtsSDK sdk = TtsSDK.builder()
    .apiKey("your-api-key")
    .build();
```

**Note:** Old code still works due to backward compatibility, but you'll see deprecation warnings.

### For Agent SDK Users

The Agent SDK structure is ready for implementation. Current placeholder:

```java
import com.talktopc.sdk.agent.AgentSDK;

AgentSDK sdk = AgentSDK.builder()
    .apiKey("your-api-key")
    .build();

// Implementation pending based on requirements
```

## Benefits

1. **Clear Separation**: TTS and Agent SDKs are now clearly separated
2. **Scalability**: Easy to add new SDK modules (STT, WebSocket, etc.)
3. **Maintainability**: Each module is self-contained
4. **Backward Compatible**: Existing code continues to work
5. **Future Ready**: Agent SDK structure is prepared for implementation

## Next Steps

1. **Implement Agent SDK**: Add functionality for injecting/overriding agents based on requirements
2. **Update Documentation**: Update README.md with new package structure
3. **Add Tests**: Create tests for new module structure
4. **Consider Additional Modules**: 
   - STT SDK (Speech-to-Text)
   - WebSocket SDK (if needed for real-time conversations)

## File Structure

```
src/main/java/com/talktopc/sdk/
├── common/              # Shared utilities
│   ├── SDKConfig.java
│   └── AudioFormat.java
├── tts/                 # TTS SDK Module
│   ├── TtsSDK.java
│   ├── client/
│   ├── models/
│   ├── exception/
│   └── examples/
├── agent/               # Agent SDK Module (placeholder)
│   ├── AgentSDK.java
│   ├── models/
│   └── exception/
└── [deprecated]/        # Backward compatibility classes
    ├── VoiceSDK.java
    ├── models/
    ├── exception/
    └── config/
```

## Notes

- All backward compatibility classes delegate to new modules
- No breaking changes - existing code continues to work
- New code should use the new package structure
- See `SDK_STRUCTURE.md` for detailed documentation
