package com.talktopc.sdk;

/**
 * Audio format specification
 * 
 * @deprecated This class is maintained for backward compatibility.
 * Please use {@link com.talktopc.sdk.common.AudioFormat} instead.
 * 
 * This class extends the common AudioFormat for backward compatibility.
 */
@Deprecated
public class AudioFormat extends com.talktopc.sdk.common.AudioFormat {
    public AudioFormat() {
        super();
    }

    public AudioFormat(String container, String encoding, int sampleRate, 
                      int bitDepth, int channels) {
        super(container, encoding, sampleRate, bitDepth, channels);
    }
}

