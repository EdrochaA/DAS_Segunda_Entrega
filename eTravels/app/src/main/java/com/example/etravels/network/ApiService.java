package com.example.etravels.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {

    /**
     * Login del usuario.
     * Se debe recibir en el JSON: success, message, email, phone, foto
     */
    @FormUrlEncoded
    @POST("login.php")
    Call<LoginResponse> loginUser(
            @Field("nombre")   String nombre,
            @Field("password") String password
    );

    /**
     * Registro de un nuevo usuario.
     * Debe recibir en el JSON: success, message
     */
    @FormUrlEncoded
    @POST("register.php")
    Call<RegisterResponse> registerUser(
            @Field("nombre")   String nombre,
            @Field("email")    String email,
            @Field("password") String password,
            @Field("telefono") String telefono
    );

    /**
     * Subida de la foto de perfil.
     * Recibe el nombre de usuario ("name") y la imagen en Base64 ("imagen").
     * Devuelve success, message y la URL pública ("url").
     */
    @FormUrlEncoded
    @POST("upload_profile.php")
    Call<GenericResponse> uploadProfileImage(
            @Field("name")   String name,
            @Field("imagen") String imagen
    );
}
