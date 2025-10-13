package com.non.rental.exception;

/**
 * Exception thrown when RentCast API calls fail.
 */
public class RentCastApiException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public RentCastApiException(String message) {
        super(message);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public RentCastApiException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
        this.responseBody = null;
    }

    public RentCastApiException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public RentCastApiException(String message, int statusCode, String responseBody, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}