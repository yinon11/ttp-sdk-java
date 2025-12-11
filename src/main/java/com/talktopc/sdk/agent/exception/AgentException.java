package com.talktopc.sdk.agent.exception;

/**
 * Exception thrown when Agent SDK operations fail
 */
public class AgentException extends RuntimeException {
    private final int statusCode;
    private final String errorMessage;
    
    public AgentException(String message) {
        super(message);
        this.statusCode = -1;
        this.errorMessage = message;
    }
    
    public AgentException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.errorMessage = message;
    }
    
    public AgentException(int statusCode, String errorMessage) {
        super(String.format("Agent SDK Error [%d]: %s", statusCode, errorMessage));
        this.statusCode = statusCode;
        this.errorMessage = errorMessage;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
