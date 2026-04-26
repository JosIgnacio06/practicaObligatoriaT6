package modelos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

// MODELO: Datos del usuario y operaciones propias sobre sus colecciones.
// No tiene ninguna dependencia de vistas ni controladores.
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nombre;
    private String apel;
    private String clave;
    private long movil;          // FIX: long en vez de int para evitar overflow en móviles de 9 dígitos
    private String email;
    private boolean esAdmin;     // NUEVO: flag de administrador
    private ArrayList<Producto> enVenta;
    private ArrayList<Trato> ventas;
    private ArrayList<Trato> compras;
    private ArrayList<Integer> valoracionesPendientes;

    public Usuario(int id, String nombre, String apel, String clave, long movil, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apel = apel;
        this.clave = clave;
        this.movil = movil;
        this.email = email;
        this.esAdmin = false;
        this.enVenta = new ArrayList<>();
        this.ventas = new ArrayList<>();
        this.compras = new ArrayList<>();
        this.valoracionesPendientes = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Getters y setters
    // -------------------------------------------------------------------------
    public int getId()                   { return id; }
    public void setId(int id)            { this.id = id; }

    public String getNombre()            { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApel()              { return apel; }
    public void setApel(String apel)     { this.apel = apel; }

    public String getClave()             { return clave; }
    public void setClave(String clave)   { this.clave = clave; }

    public long getMovil()               { return movil; }
    public void setMovil(long movil)     { this.movil = movil; }

    public String getEmail()             { return email; }
    public void setEmail(String email)   { this.email = email; }

    public boolean isEsAdmin()               { return esAdmin; }
    public void setEsAdmin(boolean esAdmin)  { this.esAdmin = esAdmin; }

    public ArrayList<Producto> getEnVenta()              { return enVenta; }
    public void setEnVenta(ArrayList<Producto> enVenta)  { this.enVenta = enVenta; }

    public ArrayList<Trato> getVentas()              { return ventas; }
    public void setVentas(ArrayList<Trato> ventas)   { this.ventas = ventas; }

    public ArrayList<Trato> getCompras()             { return compras; }
    public void setCompras(ArrayList<Trato> compras) { this.compras = compras; }

    public ArrayList<Integer> getValoracionesPendientes()                          { return valoracionesPendientes; }
    public void setValoracionesPendientes(ArrayList<Integer> valoracionesPendientes) { this.valoracionesPendientes = valoracionesPendientes; }

    // -------------------------------------------------------------------------
    // Métodos de negocio del modelo
    // -------------------------------------------------------------------------

    public int productosEnVenta() {
        if (enVenta == null) return -1;
        return enVenta.size();
    }

    public boolean addProducto(Producto p) {
        try {
            if (p == null) return false;
            if (p.getId() < 0 || p.getPrecio() <= 0) return false;
            enVenta.add(p);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean quitaProducto(long id) {
        try {
            for (int i = 0; i < enVenta.size(); i++) {
                if (enVenta.get(i).getId() == id) {
                    enVenta.remove(i);
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    public boolean addValoracionPendiente(int idTrato) {
        return valoracionesPendientes.add(idTrato);
    }

    public int addTratoVenta(int id, String emailComprador, Producto p) {
        Trato nuevoTrato = new Trato(id, "VENTA", emailComprador, p,
                Calendar.getInstance(), p.getPrecio(), "", 0);
        this.ventas.add(nuevoTrato);
        return nuevoTrato.getId();
    }

    public boolean addTratoCompra(Trato t) {
        if (t != null) {
            this.compras.add(t);
            return true;
        }
        return false;
    }
}
