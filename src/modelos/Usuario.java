package modelos;

import java.util.ArrayList;
import java.util.Calendar;

public class Usuario {
    private int id;
    private String nombre;
    private String apel;
    private String clave;
    private int movil;
    private String email;
    private ArrayList<Producto> enVenta;
    private ArrayList<Trato> ventas;
    private ArrayList<Trato> compras;
    private ArrayList<Integer> valoracionesPendientes;

    //Constructor
    public Usuario(int id, String nombre, String apel, String clave, int movil, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apel = apel;
        this.clave = clave;
        this.movil = movil;
        this.email = email;
        // Inicializamos las listas vacías
        this.enVenta = new ArrayList<>();
        this.ventas = new ArrayList<>();
        this.compras = new ArrayList<>();
        this.valoracionesPendientes = new ArrayList<>();
    }

    //Getters y setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApel() {
        return apel;
    }

    public void setApel(String apel) {
        this.apel = apel;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public int getMovil() {
        return movil;
    }

    public void setMovil(int movil) {
        this.movil = movil;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<Producto> getEnVenta() {
        return enVenta;
    }

    public void setEnVenta(ArrayList<Producto> enVenta) {
        this.enVenta = enVenta;
    }

    public ArrayList<Trato> getVentas() {
        return ventas;
    }

    public void setVentas(ArrayList<Trato> ventas) {
        this.ventas = ventas;
    }

    public ArrayList<Trato> getCompras() {
        return compras;
    }

    public void setCompras(ArrayList<Trato> compras) {
        this.compras = compras;
    }

    public ArrayList<Integer> getValoracionesPendientes() {
        return valoracionesPendientes;
    }

    public void setValoracionesPendientes(ArrayList<Integer> valoracionesPendientes) {
        this.valoracionesPendientes = valoracionesPendientes;
    }

    //Otros métodos
    //Este metodo devuelve el número de productos que tenemos en venta.
    public int productosEnVenta(){
        if (enVenta == null) return -1;
        return enVenta.size();
    }

    //Este metodo devuelve true o false según si se ha podido o no añadir un producto al usuario.
    public boolean addProducto(Producto p) {
        try {
            // 1. Protección contra nulos (Evita el NullPointerException)
            if (p == null) {
                return false;
            }

            // 2. Protección contra datos ilógicos (Reglas de negocio)
            // Por ejemplo: que el ID no sea negativo o el precio sea mayor a 0
            if (p.getId() < 0 || p.getPrecio() <= 0) {
                return false;
            }

            // 3. Si está bien, intentamos añadir
            enVenta.add(p);

        } catch (Exception e) {
            // Esto captura cualquier error inesperado
            return false;
        }
        return true;
    }

    //Borramos el producto.
    public boolean quitaProducto(long id) {
        try {
            for (int i = 0; i < enVenta.size(); i++) {
                // Debemos comparar el ID del parámetro con el ID del producto en la lista
                if (enVenta.get(i).getId() == id) {
                    enVenta.remove(i); // Eliminamos el producto físicamente de la lista
                    return true; // Retornamos true al encontrarlo y borrarlo
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false; // Si termina el bucle y no encuentra el ID, devuelve false
    }

    public boolean addValoracionPendiente(int id) {
        return true;
    }

    // Metodo para añadir una Venta (Crea el objeto dentro)
    public int addTratoVenta(int id, String emailComprador, Producto p) {
        // Creamos el trato de tipo VENTA
        // Usamos Calendar.getInstance() para la fecha actual y el precio del producto
        Trato nuevoTrato = new Trato(id, "VENTA", emailComprador, p, Calendar.getInstance(), p.getPrecio(), "", 0);

        this.ventas.add(nuevoTrato);

        return nuevoTrato.getId(); // Devolvemos el ID por si el Main lo necesita
    }

    // Metodo para añadir una Compra (Recibe el trato ya configurado)
    public boolean addTratoCompra(Trato t) {
        if (t != null) {
            this.compras.add(t);
            return true;
        }
        return false;
    }


}



