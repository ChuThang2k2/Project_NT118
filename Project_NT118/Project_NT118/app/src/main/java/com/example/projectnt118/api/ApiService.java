package com.example.projectnt118.api;

import com.example.projectnt118.modle.ForgotPwRequest;
import com.example.projectnt118.modle.PotholeResponse;
import com.example.projectnt118.modle.ResetPwRequest;
import com.example.projectnt118.modle.ResetPwResponse;
import com.example.projectnt118.modle.SignInRequest;
import com.example.projectnt118.modle.SignInResponse;
import com.example.projectnt118.modle.SignUpRequest;
import com.example.projectnt118.modle.SignUpResponse;
import com.example.projectnt118.modle.VerifyCodeRequest;
import com.example.projectnt118.modle.VerifyCodeResponse;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @POST("login")
    Call<SignInResponse> signIn(@Body SignInRequest request);

    @GET("logout")
    Call<ResponseBody> logout();

    @POST("signup")
    Call<SignUpResponse> signUp(@Body SignUpRequest signUpRequest);

    @POST("forgot")
    Call<ResponseBody> forgotPassword(@Body ForgotPwRequest request);

    @POST("checkToken")
    Call<VerifyCodeResponse> verifyCode(@Body VerifyCodeRequest request);

    @POST("resetPw")
    Call<ResetPwResponse> resetPassword(@Body ResetPwRequest request);

    @GET("/potholes")
    Call<List<PotholeResponse>> getPotholes();
}
