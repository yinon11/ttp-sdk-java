package com.talktopc.sdk.common;

import java.util.Objects;

/**
 * Audio format specification
 * 
 * Represents audio format configuration including container, encoding,
 * sample rate, bit depth, and channel count.
 * Shared across SDK modules.
 */
public class AudioFormat {
    private String container;  // "raw" or "wav"
    private String encoding;  // "pcm", "pcmu", "pcma"
    private int sampleRate;    // Hz
    private int bitDepth;      // bits
    private int channels;      // 1 = mono

    public AudioFormat() {}

    public AudioFormat(String container, String encoding, int sampleRate, 
                      int bitDepth, int channels) {
        this.container = container;
        this.encoding = encoding;
        this.sampleRate = sampleRate;
        this.bitDepth = bitDepth;
        this.channels = channels;
    }

    // Getters and setters
    public String getContainer() { 
        return container; 
    }
    
    public void setContainer(String container) { 
        this.container = container; 
    }

    public String getEncoding() { 
        return encoding; 
    }
    
    public void setEncoding(String encoding) { 
        this.encoding = encoding; 
    }

    public int getSampleRate() { 
        return sampleRate; 
    }
    
    public void setSampleRate(int sampleRate) { 
        this.sampleRate = sampleRate; 
    }

    public int getBitDepth() { 
        return bitDepth; 
    }
    
    public void setBitDepth(int bitDepth) { 
        this.bitDepth = bitDepth; 
    }

    public int getChannels() { 
        return channels; 
    }
    
    public void setChannels(int channels) { 
        this.channels = channels; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioFormat that = (AudioFormat) o;
        return sampleRate == that.sampleRate &&
               bitDepth == that.bitDepth &&
               channels == that.channels &&
               Objects.equals(container, that.container) &&
               Objects.equals(encoding, that.encoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, encoding, sampleRate, bitDepth, channels);
    }

    @Override
    public String toString() {
        return String.format("AudioFormat{container='%s', encoding='%s', " +
                           "sampleRate=%d, bitDepth=%d, channels=%d}",
                           container, encoding, sampleRate, bitDepth, channels);
    }
}
