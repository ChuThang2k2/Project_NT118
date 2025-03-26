package com.example.projectnt118.modle;

public class VerifyCodeResponse {
    private String message;
    private boolean success ;

    public VerifyCodeResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess() {
        return success;
    }

    @Override
    public String toString() {
        return "VerifyCodeResponse{" +
                "message='" + message + '\'' +
                ", success=" + success +
                '}';
    }
}
