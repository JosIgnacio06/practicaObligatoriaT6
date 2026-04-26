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
 * CONTROLADOR: Contiene toda la lógica de negocio de la aplicación.
 *
 * Reglas MVC:
 *  - NO tiene Scanner ni lee entrada del usuario.
 *  - NO tiene System.out.println (la Vista muestra los resultados).
 *  - NO pinta menús (responsabilidad de Main/Vista).
 *  - SÍ coordina Modelos y Utilidades, y devuelve datos o booleanos a la Vista.
 *
 * Persistencia:
 *  - Los datos se cargan al arrancar desde el fichero indicado en AppConfig.
 *  - Se graban automáticamente tras cada cambio (guardarDatos()).
 *  - Solo se serializan los objetos Usuario (que contienen Producto y Trato).
 */
public class GestionAPP {

    private ArrayList<Usuario> usuarios;

    // -------------------------------------------------------------------------
    // Constructor: carga config y datos persistidos
    // -------------------------------------------------------------------------
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

    /**
     * Guarda el estado actual de la aplicación en disco.
     * Se llama automáticamente tras cada operación que modifica datos.
     */
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
     * Envía el email de verificación con el código generado.
     * @return el código generado, o null si el envío de email falló.
     *
     * FIX: antes devolvía siempre el código aunque el email no existiera,
     * generando un bucle infinito. Ahora devuelve null si el envío falla.
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
        return enviado ? codigo : null; // null señala fallo de envío a la Vista
    }

    /**
     * Envía notificaciones de venta (texto) a vendedor y comprador.
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
     * Comprueba credenciales por email y clave.
     * Si son correctas: registra log, actualiza properties y devuelve el Usuario.
     * @return el Usuario si son correctas, null en caso contrario.
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

    /**
     * Registra el cierre de sesión en el log.
     */
    public void logout(String email) {
        LogUtils.logCierreSesion(email);
    }

    /**
     * Elimina el usuario de la lista.
     * @return true si existía y fue eliminado.
     */
    public boolean borrarUsuario(Usuario u) {
        if (u == null) return false;
        boolean eliminado = usuarios.remove(u);
        if (eliminado) guardarDatos();
        return eliminado;
    }

    /**
     * Busca un usuario por email (insensible a mayúsculas).
     */
    public Usuario buscaMail(String email) {
        for (Usuario u : usuarios) {
            if (u.getEmail().equalsIgnoreCase(email)) return u;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE PRODUCTOS
    // -------------------------------------------------------------------------

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

    public Producto validarProductoPropio(long id, Usuario user) {
        Producto p = buscaProductoID(id);
        if (p == null) return null;
        for (Producto mio : user.getEnVenta()) {
            if (mio.getId() == id) return p;
        }
        return null;
    }

    public long generarIdProductoUnico() {
        long nuevoId;
        do { nuevoId = (long) (Math.random() * 9000000) + 1000000; }
        while (buscaProductoID(nuevoId) != null);
        return nuevoId;
    }

    /**
     * Añade un producto al usuario y persiste.
     * También registra en el log.
     */
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
     * Registra la venta y guarda datos.
     * @return ID del trato o -1 si hubo error.
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

    public int generarIdTratoUnico() {
        int nuevoId;
        do { nuevoId = (int) (Math.random() * 900000) + 100000; }
        while (buscaTratoID(nuevoId) != null);
        return nuevoId;
    }

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

    public Trato buscaCompraPorId(Usuario user, int idBuscado) {
        for (Trato t : user.getCompras()) {
            if (t.getId() == idBuscado && t.getPuntuacion() == 0) return t;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // GESTIÓN DE VALORACIONES
    // -------------------------------------------------------------------------

    public ArrayList<Trato> getValoracionesPendientes(Usuario user) {
        ArrayList<Trato> pendientes = new ArrayList<>();
        for (Trato t : user.getCompras()) {
            if (t.getPuntuacion() == 0) pendientes.add(t);
        }
        pendientes.sort(Comparator.comparing(Trato::getFecha));
        return pendientes;
    }

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

    /**
     * Devuelve el contenido del fichero de log como String.
     */
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
     * Realiza una copia de seguridad del fichero de datos en la ruta indicada.
     * @param rutaDestino directorio donde guardar la copia.
     * @return true si la copia se realizó correctamente.
     */
    public boolean realizarBackup(String rutaDestino) {
        try {
            Path origen = Paths.get(AppConfig.getRutaDatos());
            if (!Files.exists(origen)) {
                System.err.println("[BACKUP] No existe el fichero de datos para copiar.");
                return false;
            }

            // Aseguramos que el directorio de destino exista
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
    // DATOS DE PRUEBA (solo se insertan si la app arranca sin datos persistidos)
    // -------------------------------------------------------------------------

    /**
     * Inserta usuarios y productos de prueba.
     * Solo debe llamarse si usuarios está vacío (primera ejecución).
     * Credenciales:
     *   ana@fernanpop.com  / 1234
     *   luis@fernanpop.com / 1234
     *   marta@fernanpop.com / 1234
     *   admin@fernanpop.com / admin (administrador)
     */
    public void insercionDatos() {
        if (!usuarios.isEmpty()) return; // Ya hay datos persistidos, no sobrescribir

        Usuario ana   = new Usuario(1000001, "Ana",   "García",   "1234", 611000001L, "ana@fernanpop.com");
        Usuario luis  = new Usuario(1000002, "Luis",  "Martínez", "1234", 622000002L, "luis@fernanpop.com");
        Usuario marta = new Usuario(1000003, "Marta", "López",    "1234", 633000003L, "marta@fernanpop.com");
        Usuario admin = new Usuario(1000004, "Admin", "Fernanpop","admin", 600000000L, "admin@fernanpop.com");
        admin.setEsAdmin(true);

        // Productos de Ana
        Producto bici  = new Producto(2000001, "Bicicleta de montaña", "Shimano 21 velocidades, ruedas 27\"", 180.0, "Usado");
        Producto camara = new Producto(2000002, "Cámara Canon EOS", "Réflex 24MP, objetivo 18-55mm", 320.0, "Usado");
        Producto teclado = new Producto(2000003, "Teclado mecánico", "Switch Cherry MX Red, RGB", 75.0, "Nuevo");
        ana.addProducto(bici);
        ana.addProducto(camara);
        ana.addProducto(teclado);

        // Productos de Luis
        luis.addProducto(new Producto(2000004, "Sofá esquinero",    "3+2 plazas, color gris perla",        250.0, "Usado"));
        luis.addProducto(new Producto(2000005, "Monitor 27 pulgadas", "IPS 144Hz, resolución 2K",          210.0, "Usado"));

        // Productos de Marta
        marta.addProducto(new Producto(2000006, "Patinete eléctrico", "Autonomía 25km, 25km/h",            150.0, "Usado"));
        marta.addProducto(new Producto(2000007, "Auriculares Sony",   "WH-1000XM4, cancelación de ruido",  120.0, "Nuevo"));
        marta.addProducto(new Producto(2000008, "Cafetera Nespresso", "Incluye 50 cápsulas variadas",       55.0, "Usado"));

        // FIX: el trato de prueba usaba una copia de bici que seguía en enVenta de Ana.
        // Ahora registramos la venta correctamente usando registrarVenta para que
        // bici se elimine de enVenta de Ana y quede solo en el historial.
        usuarios.add(ana);
        usuarios.add(luis);
        usuarios.add(marta);
        usuarios.add(admin);

        // Simulamos que Luis compró la bici a Ana
        registrarVenta(ana, luis, bici);

        // Log de datos de prueba
        LogUtils.logNuevoProducto(2000002, 1000001);
        LogUtils.logNuevoProducto(2000003, 1000001);
        LogUtils.logNuevoProducto(2000004, 1000002);
        LogUtils.logNuevoProducto(2000005, 1000002);
        LogUtils.logNuevoProducto(2000006, 1000003);
        LogUtils.logNuevoProducto(2000007, 1000003);
        LogUtils.logNuevoProducto(2000008, 1000003);

        guardarDatos();
    }
}
