package com.example.etravels.network;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    private boolean success;
    private String message;

    // Nuevos campos para el perfil
    @SerializedName("name")
    private String name;
    @SerializedName("phone")
    private String phone;
    @SerializedName("email")
    private String email;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    // Getters para los nuevos campos
    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }
}
