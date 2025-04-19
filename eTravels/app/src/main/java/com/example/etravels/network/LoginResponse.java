package com.example.etravels.network;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String message;

    // Datos de perfil devueltos por login.php
    private String phone;
    private String email;

    @SerializedName("foto")
    private String photoUrl;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }
}
