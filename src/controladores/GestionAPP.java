package controladores;

import modelos.Producto;
import modelos.Trato;
import modelos.Usuario;
import utils.*;

import java.io.File;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;

/**
 * Controlador principal de Fernanpop. Contiene toda la lógica de negocio.
 *
 * Sigue el patrón MVC:
 *  - No lee entrada del usuario (sin Scanner).
 *  - No muestra menús ni imprime en pantalla (eso lo hace Main).
 *  - Coordina los modelos (Usuario, Producto, Trato) y las utilidades.
 *  - Devuelve datos o booleanos a la Vista para que esta decida qué mostrar.
 *
 * Persistencia: los datos se cargan al arrancar desde el fichero indicado
 * en AppConfig y se graban automáticamente tras cada modificación.
 */
public class GestionAPP {

    private ArrayList<Usuario> usuarios;

    public GestionAPP() {
        AppConfig.cargar();
        this.usuarios = PersistenciaUtils.cargar(AppConfig.getRutaDatos());
    }

    /** Constructor alternativo para tests (sin persistencia). */
    public GestionAPP(ArrayList<Usuario> usuarios) {
        this.usuarios = usuarios;
    }

    public ArrayList<Usuario> getUsuarios() { return usuarios; }

    // -------------------------------------------------------------------------
    // Persistencia
    // -------------------------------------------------------------------------

    /** Guarda el estado actual en disco. Se llama tras cada operación que modifica datos. */
    public void guardarDatos() {
        PersistenciaUtils.guardar(AppConfig.getRutaDatos(), usuarios);
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE USUARIOS
    // -------------------------------------------------------------------------

    /**
     * Crea y registra un nuevo usuario.
     * @return el Usuario creado, o null si el email ya está registrado.
     */
    public Usuario crearUsuario(String nombre, String apellidos, String email, long movil, String clave) {
        if (buscaMail(email) != null) return null;

        int id;
        do { id = generarIdUser(); } while (idExiste(usuarios, id));

        Usuario nuevo = new Usuario(id, nombre, apellidos, clave, movil, email);
        usuarios.add(nuevo);
        guardarDatos();
        return nuevo;
    }

    /**
     * Genera un código de verificación y lo envía por email al nuevo usuario.
     * @return el código generado, o null si el envío falló.
     */
    public String generarYEnviarCodigoVerificacion(String nombre, String apellidos, String email) {
        String codigo = generarClave();
        String asunto = "Validación de correo electrónico || Fernanpop.";
        String cuerpo = """
                Gracias %s %s por registrarte en Fernanpop.
                Solamente queda validar tu correo electrónico y podrás disfrutar de tus compras y ventas.
                
                Introduce este código: %s
                """.formatted(nombre, apellidos, codigo);

        boolean enviado = EmailUtils.enviarEmail(email, asunto, cuerpo);
        return enviado ? codigo : null;
    }

    /**
     * Envía notificaciones de venta por texto a vendedor y comprador.
     * @return true si ambos emails se enviaron correctamente.
     */
    public boolean enviarNotificacionesVenta(Usuario vendedor, Usuario comprador, Producto p) {
        String asuntoVendedor = "Venta realizada con éxito: " + p.getTitulo();
        String cuerpoVendedor = "Hola " + vendedor.getNombre() + ",\n\n" +
                "Tu producto '" + p.getTitulo() + "' (ID: " + p.getId() + ") " +
                "ha sido vendido por " + p.getPrecio() + "€.\n" +
                "Comprador: " + comprador.getNombre() + " (" + comprador.getEmail() + ").";

        String asuntoComprador = "Compra realizada con éxito: " + p.getTitulo();
        String cuerpoComprador = "Hola " + comprador.getNombre() + ",\n\n" +
                "Has comprado: '" + p.getTitulo() + "'\n" +
                "Precio: " + p.getPrecio() + "€.\nVendedor: " + vendedor.getNombre() + ".";

        boolean okV = EmailUtils.enviarEmail(vendedor.getEmail(), asuntoVendedor, cuerpoVendedor);
        boolean okC = EmailUtils.enviarEmail(comprador.getEmail(), asuntoComprador, cuerpoComprador);
        return okV && okC;
    }

    /**
     * Genera y envía por email el PDF de resumen de venta al comprador.
     * @return true si se generó y envió correctamente.
     */
    public boolean enviarResumenVentaPDF(Usuario vendedor, Usuario comprador,
                                         Producto producto, int idTrato) {
        File pdf = PDFUtils.generarResumenVenta(vendedor, comprador, producto, idTrato);
        if (pdf == null) return false;

        String asunto = "Fernanpop - Resumen de tu compra: " + producto.getTitulo();
        String cuerpo = "Hola " + comprador.getNombre() + ",\n\n" +
                "Adjuntamos el resumen de tu compra. ¡Gracias por usar Fernanpop!";

        return EmailUtils.enviarEmailConPDF(comprador.getEmail(), asunto, cuerpo, pdf);
    }

    /**
     * Valida credenciales. Si son correctas, registra el log y actualiza el properties.
     * @return el Usuario autenticado, o null si las credenciales son incorrectas.
     */
    public Usuario login(String email, String clave) {
        for (Usuario user : usuarios) {
            if (user.getEmail().equalsIgnoreCase(email.trim())
                    && user.getClave().equals(clave.trim())) {
                LogUtils.logInicioSesion(email);
                AppConfig.setUltimaSesion(email);
                return user;
            }
        }
        return null;
    }

    /** Registra el cierre de sesión en el log. */
    public void logout(String email) {
        LogUtils.logCierreSesion(email);
    }

    /**
     * Elimina el usuario de la lista y persiste.
     * @return true si existía y fue eliminado.
     */
    public boolean borrarUsuario(Usuario u) {
        if (u == null) return false;
        boolean eliminado = usuarios.remove(u);
        if (eliminado) guardarDatos();
        return eliminado;
    }

    /** Busca un usuario por email (insensible a mayúsculas). */
    public Usuario buscaMail(String email) {
        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE PRODUCTOS
    // -------------------------------------------------------------------------

    /** Devuelve todos los productos de todos los usuarios, ordenados por precio. */
    public ArrayList<Producto> getAllProductos() {
        ArrayList<Producto> todos = new ArrayList<>();
        for (Usuario u : usuarios) todos.addAll(u.getEnVenta());
        todos.sort(Comparator.comparingDouble(Producto::getPrecio));
        return todos;
    }

    public ArrayList<Producto> getProductosUser(String email) {
        Usuario u = buscaMail(email);
        return (u != null) ? u.getEnVenta() : new ArrayList<>();
    }

    public Producto buscaProductoID(long id) {
        for (Producto p : getAllProductos()) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    public int getTotalProductos() {
        int total = 0;
        for (Usuario u : usuarios) total += u.getEnVenta().size();
        return total;
    }

    /** Busca productos cuyo título o descripción contengan el texto indicado. */
    public ArrayList<Producto> buscaProductosTexto(String textoBusqueda) {
        ArrayList<Producto> encontrados = new ArrayList<>();
        String busquedaMinus = textoBusqueda.toLowerCase();
        for (Producto p : getAllProductos()) {
            if (p.getTitulo().toLowerCase().contains(busquedaMinus)
                    || p.getDescripcion().toLowerCase().contains(busquedaMinus)) {
                encontrados.add(p);
            }
        }
        return encontrados;
    }

    /**
     * Comprueba que el producto con ese ID exista y pertenezca al usuario.
     * @return el Producto si es válido y propio, null en caso contrario.
     */
    public Producto validarProductoPropio(long id, Usuario user) {
        Producto p = buscaProductoID(id);
        if (p == null) return null;
        for (Producto mio : user.getEnVenta()) {
            if (mio.getId() == id) return p;
        }
        return null;
    }

    /** Genera un ID de producto aleatorio de 7 dígitos que no esté en uso. */
    public long generarIdProductoUnico() {
        long nuevoId;
        do { nuevoId = (long) (Math.random() * 9000000) + 1000000; }
        while (buscaProductoID(nuevoId) != null);
        return nuevoId;
    }

    /** Añade el producto al usuario, registra en log y persiste. */
    public boolean publicarProducto(Usuario user, Producto p) {
        boolean ok = user.addProducto(p);
        if (ok) {
            LogUtils.logNuevoProducto(p.getId(), user.getId());
            guardarDatos();
        }
        return ok;
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE TRATOS
    // -------------------------------------------------------------------------

    /**
     * Registra una venta: crea los tratos en vendedor y comprador,
     * quita el producto de enVenta y persiste.
     * @return ID del trato generado, o -1 si algún parámetro es null.
     */
    public int registrarVenta(Usuario vendedor, Usuario comprador, Producto producto) {
        if (vendedor == null || comprador == null || producto == null) return -1;

        int idTrato = generarIdTratoUnico();
        vendedor.addTratoVenta(idTrato, comprador.getEmail(), producto);

        Trato tCompra = new Trato(idTrato, "COMPRA", vendedor.getEmail(), producto,
                Calendar.getInstance(), producto.getPrecio(), "", 0);
        comprador.addTratoCompra(tCompra);
        comprador.addValoracionPendiente(idTrato);
        vendedor.getEnVenta().remove(producto);

        LogUtils.logVentaCerrada(vendedor.getEmail(), comprador.getEmail());
        guardarDatos();
        return idTrato;
    }

    /** Genera un ID de trato aleatorio de 6 dígitos que no esté en uso. */
    public int generarIdTratoUnico() {
        int nuevoId;
        do { nuevoId = (int) (Math.random() * 900000) + 100000; }
        while (buscaTratoID(nuevoId) != null);
        return nuevoId;
    }

    /** Busca un trato por ID en ventas y compras de todos los usuarios. */
    public Trato buscaTratoID(int idBuscado) {
        for (Usuario u : usuarios) {
            for (Trato t : u.getVentas()) if (t.getId() == idBuscado) return t;
            for (Trato t : u.getCompras()) if (t.getId() == idBuscado) return t;
        }
        return null;
    }

    public Trato buscaVentaPorId(int idBuscado) {
        for (Usuario u : usuarios) {
            for (Trato t : u.getVentas()) if (t.getId() == idBuscado) return t;
        }
        return null;
    }

    /** Busca una compra pendiente de valorar (puntuacion == 0) del usuario dado. */
    public Trato buscaCompraPorId(Usuario user, int idBuscado) {
        for (Trato t : user.getCompras()) {
            if (t.getId() == idBuscado && t.getPuntuacion() == 0) return t;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE VALORACIONES
    // -------------------------------------------------------------------------

    /** Devuelve las compras pendientes de valorar del usuario, ordenadas por fecha. */
    public ArrayList<Trato> getValoracionesPendientes(Usuario user) {
        ArrayList<Trato> pendientes = new ArrayList<>();
        for (Trato t : user.getCompras()) {
            if (t.getPuntuacion() == 0) pendientes.add(t);
        }
        pendientes.sort(Comparator.comparing(Trato::getFecha));
        return pendientes;
    }

    /**
     * Registra la valoración en el trato del comprador y en el trato del vendedor.
     * @return true si se encontró el trato y se registró correctamente.
     */
    public boolean registrarValoracion(Usuario comprador, int idTrato, int puntuacion, String comentario) {
        Trato tratoComprador = buscaCompraPorId(comprador, idTrato);
        if (tratoComprador == null) return false;

        tratoComprador.setPuntuacion(puntuacion);
        tratoComprador.setComentario(comentario);

        Trato tratoVendedor = buscaVentaPorId(idTrato);
        if (tratoVendedor != null) {
            tratoVendedor.setPuntuacion(puntuacion);
            tratoVendedor.setComentario(comentario);
        }

        borraValoracionPendiente(comprador, idTrato);
        guardarDatos();
        return true;
    }

    public boolean borraValoracionPendiente(Usuario user, int idTrato) {
        return user.getValoracionesPendientes().remove(Integer.valueOf(idTrato));
    }

    // -------------------------------------------------------------------------
    // MÉTODOS AUXILIARES
    // -------------------------------------------------------------------------

    /**
     * Calcula la nota media del usuario a partir de sus ventas valoradas.
     * @return la media, o -1.0 si no tiene ninguna valoración.
     */
    public double notaMedia(Usuario user) {
        int suma = 0, count = 0;
        for (Trato t : user.getVentas()) {
            if (t.getPuntuacion() > 0) { suma += t.getPuntuacion(); count++; }
        }
        return count == 0 ? -1.0 : (double) suma / count;
    }

    public static int generarIdUser() {
        return (int) (Math.random() * 9000000) + 1000000;
    }

    public static boolean idExiste(ArrayList<Usuario> usuarios, int nuevoId) {
        for (Usuario u : usuarios) if (u.getId() == nuevoId) return true;
        return false;
    }

    /** Genera una clave aleatoria de 15 caracteres (letras, dígitos y símbolos). */
    public static String generarClave() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789*?-";
        StringBuilder clave = new StringBuilder();
        for (int i = 0; i < 15; i++)
            clave.append(caracteres.charAt((int) (Math.random() * caracteres.length())));
        return clave.toString();
    }

    // -------------------------------------------------------------------------
    // FUNCIONES DE ADMINISTRADOR
    // -------------------------------------------------------------------------

    /** Devuelve el contenido del fichero de log como String. */
    public String getContenidoLog() {
        try {
            File logFile = new File("logs" + File.separator + "fernanpop.log");
            if (!logFile.exists()) return "El fichero de log aún no existe.";
            return new String(Files.readAllBytes(logFile.toPath()));
        } catch (Exception e) {
            return "Error al leer el log: " + e.getMessage();
        }
    }

    /**
     * Genera el fichero Excel con todos los productos y lo envía por email al admin.
     * @return true si se generó y envió correctamente.
     */
    public boolean enviarListadoProductosPorEmail(String emailAdmin) {
        ArrayList<Producto> todos = getAllProductos();
        File excel = ExcelUtils.generarListadoProductos(todos);
        if (excel == null) return false;

        String asunto = "Fernanpop - Listado de productos";
        String cuerpo = "Adjunto encontrarás el listado completo de productos en venta (" + todos.size() + " productos).";
        return EmailUtils.enviarEmailConExcel(emailAdmin, asunto, cuerpo, excel);
    }

    /**
     * Copia el fichero de datos en la ruta indicada con timestamp en el nombre.
     * @return true si la copia se realizó correctamente.
     */
    public boolean realizarBackup(String rutaDestino) {
        try {
            Path origen = Paths.get(AppConfig.getRutaDatos());
            if (!Files.exists(origen)) {
                System.err.println("[BACKUP] No existe el fichero de datos para copiar.");
                return false;
            }

            Path dirDestino = Paths.get(rutaDestino);
            Files.createDirectories(dirDestino);

            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            Path destino = dirDestino.resolve("fernanpop_backup_" + timestamp + ".dat");

            Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            System.err.println("[BACKUP] Error: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // DATOS DE PRUEBA
    // -------------------------------------------------------------------------

    /**
     * Inserta usuarios y productos de prueba. Solo actúa si la lista está vacía
     * (es decir, en la primera ejecución sin datos persistidos).
     */
    public void insercionDatos() {
        if (!usuarios.isEmpty()) return;

        // --- Usuarios existentes ---
        Usuario ana   = new Usuario(1000001, "Ana",   "García",   "1234", 611000001L, "ana@fernanpop.com");
        Usuario luis  = new Usuario(1000002, "Luis",  "Martínez", "1234", 622000002L, "luis@fernanpop.com");
        Usuario marta = new Usuario(1000003, "Marta", "López",    "1234", 633000003L, "marta@fernanpop.com");
        Usuario admin = new Usuario(1000004, "Admin", "Fernanpop","admin", 600000000L, "admin@fernanpop.com");
        admin.setEsAdmin(true);

        // --- Usuarios MOK adicionales para pruebas ---
        Usuario carlos = new Usuario(1000005, "Carlos", "Sánchez", "1234", 644000005L, "carlos@fernanpop.com");
        Usuario laura  = new Usuario(1000006, "Laura",  "Ruiz",    "1234", 655000006L, "laura@fernanpop.com");

        System.out.println("[MOK] --- Credenciales de todos los usuarios de prueba ---");
        System.out.println("[MOK] ana@fernanpop.com     | Contraseña: 1234");
        System.out.println("[MOK] luis@fernanpop.com    | Contraseña: 1234");
        System.out.println("[MOK] marta@fernanpop.com   | Contraseña: 1234");
        System.out.println("[MOK] admin@fernanpop.com   | Contraseña: admin");
        System.out.println("[MOK] carlos@fernanpop.com  | Contraseña: 1234");
        System.out.println("[MOK] laura@fernanpop.com   | Contraseña: 1234");

        // --- Productos de Ana ---
        Producto bici    = new Producto(2000001, "Bicicleta de montaña", "Shimano 21 velocidades, ruedas 27\"", 180.0, "Usado");
        Producto camara  = new Producto(2000002, "Cámara Canon EOS", "Réflex 24MP, objetivo 18-55mm", 320.0, "Usado");
        Producto teclado = new Producto(2000003, "Teclado mecánico", "Switch Cherry MX Red, RGB", 75.0, "Nuevo");
        ana.addProducto(bici);
        ana.addProducto(camara);
        ana.addProducto(teclado);

        // --- Productos de Luis ---
        luis.addProducto(new Producto(2000004, "Sofá esquinero",      "3+2 plazas, color gris perla",        250.0, "Usado"));
        luis.addProducto(new Producto(2000005, "Monitor 27 pulgadas", "IPS 144Hz, resolución 2K",            210.0, "Usado"));

        // --- Productos de Marta ---
        marta.addProducto(new Producto(2000006, "Patinete eléctrico", "Autonomía 25km, 25km/h",              150.0, "Usado"));
        marta.addProducto(new Producto(2000007, "Auriculares Sony",   "WH-1000XM4, cancelación de ruido",    120.0, "Nuevo"));
        marta.addProducto(new Producto(2000008, "Cafetera Nespresso", "Incluye 50 cápsulas variadas",         55.0, "Usado"));

        // --- Productos de Carlos (MOK) ---
        carlos.addProducto(new Producto(2000009, "Tablet Samsung",    "Galaxy Tab A8, 64GB, WiFi",            95.0, "Usado"));
        carlos.addProducto(new Producto(2000010, "Silla gaming",      "Reclinable 180°, reposabrazos 4D",    180.0, "Nuevo"));

        // --- Productos de Laura (MOK) ---
        laura.addProducto(new Producto(2000011, "Aspiradora Dyson",   "Sin cable, 40min autonomía",          200.0, "Usado"));
        laura.addProducto(new Producto(2000012, "Libro 'Clean Code'", "Edición en inglés, tapa blanda",       18.0, "Usado"));

        usuarios.add(ana);
        usuarios.add(luis);
        usuarios.add(marta);
        usuarios.add(admin);
        usuarios.add(carlos);
        usuarios.add(laura);

        // Simulamos que Luis compró la bici a Ana
        registrarVenta(ana, luis, bici);

        LogUtils.logNuevoProducto(2000002, 1000001);
        LogUtils.logNuevoProducto(2000003, 1000001);
        LogUtils.logNuevoProducto(2000004, 1000002);
        LogUtils.logNuevoProducto(2000005, 1000002);
        LogUtils.logNuevoProducto(2000006, 1000003);
        LogUtils.logNuevoProducto(2000007, 1000003);
        LogUtils.logNuevoProducto(2000008, 1000003);
        LogUtils.logNuevoProducto(2000009, 1000005);
        LogUtils.logNuevoProducto(2000010, 1000005);
        LogUtils.logNuevoProducto(2000011, 1000006);
        LogUtils.logNuevoProducto(2000012, 1000006);

        guardarDatos();
    }
}
