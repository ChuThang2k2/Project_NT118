package com.example.projectnt118.modle;

public class ForgotPwRequest {
    private String email;

    public ForgotPwRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
