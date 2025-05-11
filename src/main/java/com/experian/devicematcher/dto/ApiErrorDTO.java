package com.experian.devicematcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiErrorDTO {
    @JsonProperty("status")
    private int status;

    @JsonProperty("error")
    private String error;

    @JsonProperty("message")
    private String message;

    public ApiErrorDTO(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ApiErrorDTO{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
