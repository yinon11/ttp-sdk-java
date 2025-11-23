package com.talktopc.sdk;

/**
 * Configuration for VoiceSDK
 * 
 * Contains all settings for WebSocket connection and audio format negotiation.
 */
public class VoiceSDKConfig {
    private String websocketUrl;
    private String agentId;
    private String appId;
    
    // Output format (what we want from server)
    private String outputContainer = "raw";
    private String outputEncoding = "pcm";
    private int outputSampleRate = 16000;
    private int outputBitDepth = 16;
    private int outputChannels = 1;
    private int outputFrameDurationMs = 600;
    
    // Input format (what we send to server)
    private String inputEncoding = "pcm";
    private int inputSampleRate = 16000;
    private int inputBitDepth = 16;
    private int inputChannels = 1;
    
    // Protocol version
    private int protocolVersion = 2;
    
    // Auto-reconnect
    private boolean autoReconnect = true;

    // Getters and setters
    public String getWebsocketUrl() { 
        return websocketUrl; 
    }
    
    public void setWebsocketUrl(String websocketUrl) { 
        this.websocketUrl = websocketUrl; 
    }

    public String getAgentId() { 
        return agentId; 
    }
    
    public void setAgentId(String agentId) { 
        this.agentId = agentId; 
    }

    public String getAppId() { 
        return appId; 
    }
    
    public void setAppId(String appId) { 
        this.appId = appId; 
    }

    public String getOutputContainer() { 
        return outputContainer; 
    }
    
    public void setOutputContainer(String outputContainer) { 
        this.outputContainer = outputContainer; 
    }

    public String getOutputEncoding() { 
        return outputEncoding; 
    }
    
    public void setOutputEncoding(String outputEncoding) { 
        this.outputEncoding = outputEncoding; 
    }

    public int getOutputSampleRate() { 
        return outputSampleRate; 
    }
    
    public void setOutputSampleRate(int outputSampleRate) { 
        this.outputSampleRate = outputSampleRate; 
    }

    public int getOutputBitDepth() { 
        return outputBitDepth; 
    }
    
    public void setOutputBitDepth(int outputBitDepth) { 
        this.outputBitDepth = outputBitDepth; 
    }

    public int getOutputChannels() { 
        return outputChannels; 
    }
    
    public void setOutputChannels(int outputChannels) { 
        this.outputChannels = outputChannels; 
    }

    public int getOutputFrameDurationMs() { 
        return outputFrameDurationMs; 
    }
    
    public void setOutputFrameDurationMs(int outputFrameDurationMs) { 
        this.outputFrameDurationMs = outputFrameDurationMs; 
    }

    public String getInputEncoding() { 
        return inputEncoding; 
    }
    
    public void setInputEncoding(String inputEncoding) { 
        this.inputEncoding = inputEncoding; 
    }

    public int getInputSampleRate() { 
        return inputSampleRate; 
    }
    
    public void setInputSampleRate(int inputSampleRate) { 
        this.inputSampleRate = inputSampleRate; 
    }

    public int getInputBitDepth() { 
        return inputBitDepth; 
    }
    
    public void setInputBitDepth(int inputBitDepth) { 
        this.inputBitDepth = inputBitDepth; 
    }

    public int getInputChannels() { 
        return inputChannels; 
    }
    
    public void setInputChannels(int inputChannels) { 
        this.inputChannels = inputChannels; 
    }

    public int getProtocolVersion() { 
        return protocolVersion; 
    }
    
    public void setProtocolVersion(int protocolVersion) { 
        this.protocolVersion = protocolVersion; 
    }

    public boolean isAutoReconnect() { 
        return autoReconnect; 
    }
    
    public void setAutoReconnect(boolean autoReconnect) { 
        this.autoReconnect = autoReconnect; 
    }
}

