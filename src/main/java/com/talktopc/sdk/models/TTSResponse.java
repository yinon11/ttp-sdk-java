package com.talktopc.sdk.models;

/**
 * TTS Response with audio and metadata
 * 
 * @deprecated This class is maintained for backward compatibility.
 * Please use {@link com.talktopc.sdk.tts.models.TTSResponse} instead.
 * 
 * This is a wrapper class that delegates to the TTS SDK version.
 */
@Deprecated
public class TTSResponse {
    private final com.talktopc.sdk.tts.models.TTSResponse delegate;
    
    public TTSResponse(com.talktopc.sdk.tts.models.TTSResponse delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Create a TTSResponse from the TTS SDK response
     * @param delegate TTS SDK response
     * @return TTSResponse wrapper
     */
    public static TTSResponse from(com.talktopc.sdk.tts.models.TTSResponse delegate) {
        return new TTSResponse(delegate);
    }
    
    // Delegate all getter methods
    public byte[] getAudio() { return delegate.getAudio(); }
    public int getSampleRate() { return delegate.getSampleRate(); }
    public long getDurationMs() { return delegate.getDurationMs(); }
    public long getAudioSizeBytes() { return delegate.getAudioSizeBytes(); }
    public double getCreditsUsed() { return delegate.getCreditsUsed(); }
    public String getConversationId() { return delegate.getConversationId(); }
    
    @Override
    public String toString() {
        return delegate.toString();
    }
    
    // Get the underlying delegate (for internal use)
    com.talktopc.sdk.tts.models.TTSResponse getDelegate() {
        return delegate;
    }
}
