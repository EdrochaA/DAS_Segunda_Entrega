package com.example.etravels.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit;
    // Actualiza la URL base para que apunte a tu directorio en el servidor AWS.
    private static final String BASE_URL = "http://ec2-51-44-167-78.eu-west-3.compute.amazonaws.com/erocha002/WEB/";

    public static Retrofit getInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
