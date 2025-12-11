package com.talktopc.sdk.exception;

/**
 * Exception thrown when TTS operations fail
 * 
 * @deprecated This class is maintained for backward compatibility.
 * Please use {@link com.talktopc.sdk.tts.exception.TtsException} instead.
 */
@Deprecated
public class TtsException extends com.talktopc.sdk.tts.exception.TtsException {
    public TtsException(String message) {
        super(message);
    }
    
    public TtsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public TtsException(int statusCode, String errorMessage) {
        super(statusCode, errorMessage);
    }
}
