package modelos;

import java.io.Serializable;

// MODELO: Solo datos y getters/setters. Sin imports de vistas ni controladores.
public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String titulo;
    private String descripcion;
    private double precio;
    private String estado;

    public Producto(long id, String titulo, String descripcion, Double precio, String estado) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.estado = estado;
    }

    public long getId()                  { return id; }
    public void setId(long id)           { this.id = id; }

    public String getTitulo()            { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion()                   { return descripcion; }
    public void setDescripcion(String descripcion)   { this.descripcion = descripcion; }

    public Double getPrecio()            { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    public String getEstado()            { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
