package com.admitgenius.payload;

import com.admitgenius.dto.UserDTO;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JwtAuthenticationResponse {
    @JsonProperty("token")
    private String accessToken;
    private String tokenType = "Bearer";

    @JsonProperty("user")
    private UserDTO user;

    public JwtAuthenticationResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public JwtAuthenticationResponse(String accessToken, UserDTO user) {
        this.accessToken = accessToken;
        this.user = user;
    }

    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }
}