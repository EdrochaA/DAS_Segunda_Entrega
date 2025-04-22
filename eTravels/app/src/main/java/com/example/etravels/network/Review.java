package com.example.etravels.network;

public class Review {
    private String usuario;
    private String titulo;
    private String comentario;
    private double lat;
    private double lon;

    public String getUsuario()   { return usuario; }
    public String getTitulo()    { return titulo; }
    public String getComentario(){ return comentario; }
    public double getLat()       { return lat; }
    public double getLon()       { return lon; }
}
