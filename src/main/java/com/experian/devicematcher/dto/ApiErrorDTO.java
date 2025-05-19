package com.experian.devicematcher.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiErrorDTO(
    @JsonProperty("status")
    int status,

    @JsonProperty("error")
    String error,

    @JsonProperty("message")
    String message
) {}
