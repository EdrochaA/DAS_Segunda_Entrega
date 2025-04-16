package com.example.etravels.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("register.php")
    Call<RegisterResponse> registerUser(
            @Field("nombre") String nombre,
            @Field("email") String email,
            @Field("password") String password,
            @Field("telefono") String telefono
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> loginUser(
            @Field("nombre") String nombre,
            @Field("password") String password
    );
}
