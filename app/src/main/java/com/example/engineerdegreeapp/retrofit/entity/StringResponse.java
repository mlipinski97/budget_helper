package com.example.engineerdegreeapp.retrofit.entity;

public class StringResponse {
    private String responseContent;

    public StringResponse() {
    }

    public StringResponse(String responseContent) {
        this.responseContent = responseContent;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public void setResponseContent(String responseContent) {
        this.responseContent = responseContent;
    }
}
