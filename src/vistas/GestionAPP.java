package vistas;

import modelos.Producto;
import modelos.Trato;
import modelos.Usuario;
import utils.EmailUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import static vistas.Main.s; // Importamos el scanner del main

public class GestionAPP {
    //Atributos
    private ArrayList<Usuario> usuarios;

    //Getters y setters
    public GestionAPP(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public ArrayList<Usuario> getUsuarios() {
        return usuarios;
    }

    //Otros metodos
    // PUNTO 3: REGISTRO
    public Usuario addUsuario() {
        String nombre, apellidos, clave, email, clave2, movilPrueba;
        int id, movil = 0;
        boolean comprobarClave, comprobarTelefono = false, comprobarID;

        // 1. Pedir datos personales
        System.out.print("Dime tu nombre: ");
        nombre = s.nextLine();
        System.out.print("Dime tus apellidos: ");
        apellidos = s.nextLine();
        System.out.print("Dime tu email: ");
        email = s.nextLine();

        // 2. Validar Teléfono
        do {
            System.out.print("Dime tu número de teléfono: ");
            try {
                movilPrueba = s.nextLine();
                if (movilPrueba.length() == 9) {
                    movil = Integer.parseInt(movilPrueba);
                    comprobarTelefono = true;
                } else {
                    System.out.println("El número de teléfono debe tener 9 dígitos.");
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce solo números.");
            }
        } while (!comprobarTelefono);

        // 3. Validar Claves
        do {
            System.out.print("Dime una clave: ");
            clave = s.nextLine();
            System.out.print("Repite la clave: ");
            clave2 = s.nextLine();
            comprobarClave = clave.equals(clave2);
            if (!comprobarClave) System.out.println("Las claves no coinciden.");
        } while (!comprobarClave);

        // 4. Generar ID único
        do {
            id = generarIdUser();
            comprobarID = !idExiste(usuarios, id);
        } while (!comprobarID);

        // 5. Llamada al nuevo metodo de verificación
        // Si el metodo termina, es porque ha verificado con éxito (devuelve true)
        verificarEmail(nombre, apellidos, email);

        // 6. Crear y devolver el usuario
        return new Usuario(id, nombre, apellidos, clave, movil, email);
    }

    public boolean verificarEmail(String nombre, String apellidos, String email) {
        String verificacionCorreo;
        boolean verificado = false;
        String asunto = "Validación de correo electrónico || Fernanpop.";

        do {
            String codigo = generarClave(); // Generamos el código aleatorio
            String cuerpo = """
                    Gracias %s %s por registrarte en Fernanpop.
                    Solamente queda validar tu correo electrónico y podrás disfrutar de tus compras y ventas.
                    
                    Introduce este código: %s
                    """.formatted(nombre, apellidos, codigo);

            EmailUtils.enviarEmail(email, asunto, cuerpo);

            System.out.print("Introduce el código recibido por email: ");
            verificacionCorreo = s.nextLine();

            if (codigo.equals(verificacionCorreo)) {
                System.out.println("¡Correo verificado correctamente!");
                verificado = true;
            } else {
                System.out.println("Código incorrecto. Se enviará uno nuevo.");
            }
        } while (!verificado);

        return verificado;
    }

    public void enviarNotificacionesVenta(Usuario vendedor, Usuario comprador, Producto p) {
        // 1. Preparar datos para el Vendedor
        String emailVendedor = vendedor.getEmail();
        String asuntoVendedor = "Venta realizada con éxito: " + p.getTitulo();
        String cuerpoVendedor = "Hola " + vendedor.getNombre() + ",\n\n" +
                "Tu producto '" + p.getTitulo() + "' (ID: " + p.getId() + ") " +
                "ha sido vendido con éxito por un importe de " + p.getPrecio() + "€.\n" +
                "El comprador es: " + comprador.getNombre() + " (" + comprador.getEmail() + ").";

        // 2. Preparar datos para el Comprador
        String emailComprador = comprador.getEmail();
        String asuntoComprador = "Compra realizada con éxito: " + p.getTitulo();
        String cuerpoComprador = "Hola " + comprador.getNombre() + ",\n\n" +
                "Has realizado la compra del producto: '" + p.getTitulo() + "'\n" +
                "Precio pagado: " + p.getPrecio() + "€.\n" +
                "Vendedor: " + vendedor.getNombre() + ".";

        // 3. Enviar mediante la utilidad
        EmailUtils.enviarEmail(emailVendedor, asuntoVendedor, cuerpoVendedor);
        EmailUtils.enviarEmail(emailComprador, asuntoComprador, cuerpoComprador);
    }

    //Metodo que se encarga de hacer el login
    public Usuario login(String usuario, String clave) {
        for (Usuario user : usuarios) {
            if (user.getNombre().equalsIgnoreCase(usuario.trim()) && user.getClave().equals(clave.trim())) {
                return user;
            }
        }
        return null;
    }

    //Genera el ID del usuario
    public static int generarIdUser() {
        return (int) (Math.random() * 9000000) + 1000000;
    }

    //Comprobamos si ed ID existe
    public static boolean idExiste(ArrayList<Usuario> usuarios, int nuevoId) {
        for (Usuario u : usuarios) {
            if (u.getId() == nuevoId) return true;
        }
        return false;
    }

    //Generamos la clave para la verificación del email.
    public static String generarClave() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789*?-";
        StringBuilder clave = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            clave.append(caracteres.charAt((int) (Math.random() * caracteres.length())));
        }
        return clave.toString();
    }

    // Metodo para todos los productos.
    public ArrayList<Producto> getAllProductos() {
        ArrayList<Producto> todosLosProductos = new ArrayList<>();


        // Recorremos la lista global de usuarios de la aplicación
        for (Usuario u : usuarios) {
            // Obtenemos la lista de productos 'enVenta' de cada usuario
            // y la añadimos a nuestra lista global
            todosLosProductos.addAll(u.getEnVenta());
        }

        return todosLosProductos;
    }

    // Este metodo devuelve solo los productos de un usuario específico
    public ArrayList<Producto> getProductosUser(String email) {
        // 1. Buscamos al usuario por su email usando el metodo buscaMail
        Usuario u = buscaMail(email);

        // 2. Si el usuario existe, devolvemos su lista de productos en venta
        if (u != null) {
            return u.getEnVenta();
        }

        // 3. Si no existe, devolvemos una lista vacía para evitar errores
        return new ArrayList<>();
    }

    //Metodo que se encarga de buscar el Usuario según su email.
    public Usuario buscaMail(String email) {
        for (Usuario u : usuarios) {
            // Comparamos el email ignorando mayúsculas/minúsculas
            if (u.getEmail().equalsIgnoreCase(email)) {
                return u; // Usuario encontrado
            }
        }
        return null; // No existe ningún usuario con ese email
    }

    //Metodo para buscar un producto bajo el ID.
    public Producto buscaProductoID(long id) {
        for (Producto producto : getAllProductos()) {
            if (producto.getId() == id) return producto;
        }
        return null;
    }

    public boolean borrarUsuario(Usuario u) {
        if (u == null) return false;

        // Intentamos eliminarlo de la lista devuelve true si el objeto existía y fue eliminado
        return usuarios.remove(u);
    }

    public Producto validarProductoPropio(int id, Usuario user) {
        // Primero buscamos el producto globalmente
        Producto p = buscaProductoID(id);

        // Si no existe, devolvemos null
        if (p == null) return null;

        // Si existe, recorremos la lista del usuario para ver si lo tiene en venta
        for (Producto mio : user.getEnVenta()) {
            if (mio.getId() == id) {
                return p; // Es suyo, lo devolvemos
            }
        }

        return null; // Existe, pero no es suyo
    }

    public int mostrarMenuHistorial(Scanner s) {
        System.out.println("""
                *******************************
                     HISTORIAL DE TRATOS
                
                1. Historial de Compras
                2. Historial de Ventas
                3. Volver al menú
                """);
        System.out.print("Selecciona una opción: ");

        int opcion = -1;
        try {
            opcion = Integer.parseInt(s.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Error: Introduce un número válido (1, 2 o 3).");
        }

        return opcion;
    }

    //Genera el ID del producto
    public long generarIdProductoUnico() {
        long nuevoId;
        boolean repetido;

        do {
            repetido = false;
            // Generamos el número aleatorio de 7 cifras (entre 1.000.000 y 9.999.999)
            nuevoId = (long) (Math.random() * 9000000) + 1000000;

            // Comprobamos si ya existe.
            // Si buscaProductoID recibe int, asegúrate de cambiarlo a long
            // para que soporte IDs grandes sin errores.
            if (this.buscaProductoID(nuevoId) != null) {
                repetido = true;
            }

        } while (repetido);

        return nuevoId;
    }

    //Genera ID unico a trato
    public int generarIdTratoUnico() {
        int nuevoId;
        boolean repetido;

        do {
            repetido = false;
            // Generamos un ID de 6 cifras (entre 100.000 y 999.999)
            nuevoId = (int) (Math.random() * 900000) + 100000;

            // Comprobamos en la base de datos global de tratos
            // (Si tienes una lista maestra de tratos en GestionAPP, búscalo ahí)
            if (this.buscaTratoID(nuevoId) != null) {
                repetido = true;
            }
        } while (repetido);

        return nuevoId;
    }

    //Metodo que busca el trato bajo el ID
    public Trato buscaTratoID(int idBuscado) {
        // Recorremos todos los usuarios del sistema
        for (Usuario u : this.usuarios) {

            // 1. Buscamos en su lista de ventas
            for (Trato tVenta : u.getVentas()) {
                if (tVenta.getId() == idBuscado) {
                    return tVenta;
                }
            }

            // 2. Buscamos en su lista de compras
            for (Trato tCompra : u.getCompras()) {
                if (tCompra.getId() == idBuscado) {
                    return tCompra;
                }
            }
        }
        return null;
    }

    // Metodo para listar las COMPRAS
    public void listarCompras(Usuario user) {
        if (user.getCompras().isEmpty()) {
            System.out.printf("No tienes compras registradas en tu historial.%n");
            return;
        }

        for (Trato t : user.getCompras()) {
            String fechaStr = t.getFecha().get(Calendar.DAY_OF_MONTH) + "/" +
                    (t.getFecha().get(Calendar.MONTH) + 1) + "/" +
                    t.getFecha().get(Calendar.YEAR);

            System.out.printf("""
                    *******************************
                    FICHA DE COMPRA (ID: %d)
                    
                    Fecha: %s
                    Producto: %s
                    Importe: %.2f€
                    Vendedor: %s
                    %n""", t.getId(), fechaStr, t.getProducto().getTitulo(), t.getPrecio(), t.getEmailOtroUser());
        }
    }

    // Metodo para listar las VENTAS
    public void listarVentas(Usuario user) {
        if (user.getVentas().isEmpty()) {
            System.out.printf("No tienes ventas registradas en tu historial.%n");
            return;
        }

        for (Trato t : user.getVentas()) {
            String fechaStr = t.getFecha().get(Calendar.DAY_OF_MONTH) + "/" +
                    (t.getFecha().get(Calendar.MONTH) + 1) + "/" +
                    t.getFecha().get(Calendar.YEAR);

            System.out.printf("""
                      *******************************
                      FICHA DE VENTA (ID: %d)
                    
                    Fecha: %s
                    Producto: %s
                    Importe: %.2f€
                    Comprador: %s
                    Valoracion: %d/5
                    Comentario: %s
                      %n""", t.getId(), fechaStr, t.getProducto().getTitulo(), t.getPrecio(), t.getEmailOtroUser(), t.getPuntuacion(), t.getComentario());
        }
    }

    public int mostrarMenuValoraciones(Scanner s) {
        System.out.printf("""
        ***********************************
                Menú de valoraciones
        1. Mostrar mis valoraciones pendientes
        2. Valorar una compra
        3. Volver
        ***********************************
        """);
        System.out.print("Introduzca la opción deseada: ");

        int opcion = -1;
        try {
            opcion = Integer.parseInt(s.nextLine());
        } catch (NumberFormatException e) {
            // En este menú no capturamos el error aquí, devolvemos -1 y el Main lo gestiona
        }

        return opcion;
    }

    public ArrayList<Trato> getValoracionesPendientes(Usuario user) {
        ArrayList<Trato> pendientes = new ArrayList<>();

        // Recorremos todas las compras del usuario
        for (Trato t : user.getCompras()) {
            // Si la puntuación es 0, significa que aún no se ha valorado
            if (t.getPuntuacion() == 0) {
                pendientes.add(t);
            }
        }

        return pendientes;
    }

    // Borra la marca de valoración pendiente (el "1" que añadimos en la venta)
    public boolean borraValoracionPendiente(Usuario user, int idTrato) {
        // Buscamos si el usuario tiene valoraciones pendientes
        if (!user.getValoracionesPendientes().isEmpty()) {
            // Al ser un ArrayList<Integer>, borramos el último aviso de pendiente
            user.getValoracionesPendientes().remove(user.getValoracionesPendientes().size() - 1);
            return true;
        }
        return false;
    }

    // Buscador específico para encontrar un trato de COMPRA por su ID en un usuario
    public Trato buscaCompraPorId(Usuario user, int idBuscado) {
        for (Trato t : user.getCompras()) {
            if (t.getId() == idBuscado && t.getPuntuacion() == 0) {
                return t;
            }
        }
        return null;
    }

    public Trato buscaVentaPorId(int idBuscado) {
        // Recorremos todos los usuarios
        for (Usuario u : this.usuarios) {
            // Buscamos en sus ventas
            for (Trato tVenta : u.getVentas()) {
                if (tVenta.getId() == idBuscado) {
                    return tVenta;
                }
            }
        }
        return null;
    }

    // Metodo para GestionAPP.java
    public int mostrarMenuBusqueda(Scanner s) {
        System.out.printf("""
        ***********************************************
                 Menú de búsqueda de productos
        1.  Mostrar todos los productos del programa
        2.  Buscar productos con una id determinada
        3.  Buscar productos por texto en el nombre
        4.  Volver
        ***********************************************
        """);
        System.out.print("Introduzca la opción deseada: ");

        int opcion = -1;
        try {
            opcion = Integer.parseInt(s.nextLine());
        } catch (NumberFormatException e) {
            // Devolvemos -1 y el Main gestionará el error
        }

        return opcion;
    }

    public int getTotalProductos() {
        int total = 0;

        // Recorremos la lista maestra de usuarios de la aplicación
        for (Usuario u : this.usuarios) {
            // Sumamos el tamaño de la lista de productos en venta de cada usuario
            total += u.getEnVenta().size();
        }

        return total;
    }

    public ArrayList<Producto> buscaProductosTexto(String textoBusqueda) {
        ArrayList<Producto> encontrados = new ArrayList<>();
        String busquedaMinus = textoBusqueda.toLowerCase(); // Para ignorar mayúsculas

        // Usamos el metodo que ya tenemos para obtener todos los productos
        for (Producto p : this.getAllProductos()) {
            if (p.getTitulo().toLowerCase().contains(busquedaMinus)) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }
}