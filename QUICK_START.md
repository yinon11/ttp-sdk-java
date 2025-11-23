# Quick Start Guide - Java SDK

## Prerequisites

- Java 11 or higher
- Maven 3.6+

## Build from Source

```bash
cd java-sdk
mvn clean install
```

## Run Examples

### Phone Forwarding Example

```bash
# Edit examples/PhoneForwardingExample.java with your credentials
mvn exec:java -Dexec.mainClass="com.talktopc.sdk.examples.PhoneForwardingExample" \
  -Dexec.classpathScope=compile
```

### Simple Chat Example

```bash
# Edit examples/SimpleChatExample.java with your credentials
mvn exec:java -Dexec.mainClass="com.talktopc.sdk.examples.SimpleChatExample" \
  -Dexec.classpathScope=compile
```

## Basic Usage

1. **Create Configuration**

```java
VoiceSDKConfig config = new VoiceSDKConfig();
config.setWebsocketUrl("wss://speech.talktopc.com/ws/conv?agentId=YOUR_AGENT_ID&appId=YOUR_APP_ID");
config.setOutputEncoding("pcmu");  // or "pcm", "pcma"
config.setOutputSampleRate(8000);
```

2. **Create SDK Instance**

```java
VoiceSDK sdk = new VoiceSDK(config);
```

3. **Set Up Listeners**

```java
sdk.onFormatNegotiated(format -> {
    System.out.println("Format: " + format);
});

sdk.onAudioData(audioData -> {
    // Process audio
    System.out.println("Received " + audioData.length + " bytes");
});
```

4. **Connect**

```java
sdk.connect().thenRun(() -> {
    System.out.println("Connected!");
});
```

## Next Steps

- See `README.md` for full API documentation
- Check `examples/` directory for complete examples
- Read `JAVA_BACKEND_SDK_IMPLEMENTATION.md` for architecture details

