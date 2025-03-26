package com.example.projectnt118.modle;

public class VerifyCodeRequest {


    private String email;
    private String token;

    public VerifyCodeRequest(String email, String token) {
        this.token = token;
        this.email = email;
    }

    @Override
    public String toString() {
        return "VerifyCodeRequest{" +
                "email='" + email + '\'' +
                ", code='" + token + '\'' +
                '}';
    }



    public String getEmail() {
        return email;
    }

    public String getCode() {
        return token;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public void setCode(String code) {
        this.token = code;
    }
}
