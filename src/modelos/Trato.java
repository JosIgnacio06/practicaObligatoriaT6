package modelos;

import java.util.Calendar;

public class Trato {
    private int id;
    private String tipo;
    private String emailOtroUser;
    private Producto producto;
    private Calendar fecha;
    private double precio;
    private String comentario;
    private int puntuacion;

    //Constructor
    public Trato(int id, String tipo, String emailOtroUser, Producto producto, Calendar fecha, double precio, String comentario, int puntuacion) {
        this.id = id;
        this.tipo = tipo;
        this.emailOtroUser = emailOtroUser;
        this.producto = producto;
        this.fecha = fecha;
        this.precio = precio;
        this.comentario = comentario;
        this.puntuacion = puntuacion;
    }

    //Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEmailOtroUser() {
        return emailOtroUser;
    }

    public void setEmailOtroUser(String emailOtroUser) {
        this.emailOtroUser = emailOtroUser;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Calendar getFecha() {
        return fecha;
    }

    public void setFecha(Calendar fecha) {
        this.fecha = fecha;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        this.puntuacion = puntuacion;
    }
}
