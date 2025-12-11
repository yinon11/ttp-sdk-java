package com.talktopc.sdk.models;

/**
 * TTS Request Configuration
 * 
 * @deprecated This class is maintained for backward compatibility.
 * Please use {@link com.talktopc.sdk.tts.models.TTSRequest} instead.
 * 
 * This is a type alias - it's the same class, just in a different package for backward compatibility.
 */
@Deprecated
public class TTSRequest {
    // Delegate to TTS SDK version
    private final com.talktopc.sdk.tts.models.TTSRequest delegate;
    
    private TTSRequest(com.talktopc.sdk.tts.models.TTSRequest delegate) {
        this.delegate = delegate;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Delegate all getter methods
    public String getText() { return delegate.getText(); }
    public String getVoiceId() { return delegate.getVoiceId(); }
    public Double getSpeed() { return delegate.getSpeed(); }
    public String getOutputContainer() { return delegate.getOutputContainer(); }
    public String getOutputEncoding() { return delegate.getOutputEncoding(); }
    public Integer getOutputSampleRate() { return delegate.getOutputSampleRate(); }
    public Integer getOutputBitDepth() { return delegate.getOutputBitDepth(); }
    public Integer getOutputChannels() { return delegate.getOutputChannels(); }
    public Integer getOutputFrameDurationMs() { return delegate.getOutputFrameDurationMs(); }
    
    // Get the underlying delegate (for internal use)
    public com.talktopc.sdk.tts.models.TTSRequest getDelegate() {
        return delegate;
    }
    
    public static class Builder {
        private com.talktopc.sdk.tts.models.TTSRequest.Builder ttsBuilder = com.talktopc.sdk.tts.models.TTSRequest.builder();
        
        public Builder text(String text) {
            ttsBuilder.text(text);
            return this;
        }
        
        public Builder voiceId(String voiceId) {
            ttsBuilder.voiceId(voiceId);
            return this;
        }
        
        public Builder speed(double speed) {
            ttsBuilder.speed(speed);
            return this;
        }
        
        public Builder outputContainer(String container) {
            ttsBuilder.outputContainer(container);
            return this;
        }
        
        public Builder outputEncoding(String encoding) {
            ttsBuilder.outputEncoding(encoding);
            return this;
        }
        
        public Builder outputSampleRate(int sampleRate) {
            ttsBuilder.outputSampleRate(sampleRate);
            return this;
        }
        
        public Builder outputBitDepth(int bitDepth) {
            ttsBuilder.outputBitDepth(bitDepth);
            return this;
        }
        
        public Builder outputChannels(int channels) {
            ttsBuilder.outputChannels(channels);
            return this;
        }
        
        public Builder outputFrameDurationMs(int durationMs) {
            ttsBuilder.outputFrameDurationMs(durationMs);
            return this;
        }
        
        public Builder phoneSystem() {
            ttsBuilder.phoneSystem();
            return this;
        }
        
        public Builder highQuality() {
            ttsBuilder.highQuality();
            return this;
        }
        
        public Builder standardQuality() {
            ttsBuilder.standardQuality();
            return this;
        }
        
        public TTSRequest build() {
            return new TTSRequest(ttsBuilder.build());
        }
    }
}
