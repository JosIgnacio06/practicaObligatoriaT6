package vistas;

import controladores.GestionAPP;
import modelos.Producto;
import modelos.Trato;
import modelos.Usuario;
import utils.AppConfig;
import utils.Utils;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Main {

    public static Scanner s = new Scanner(System.in);
    static GestionAPP app = new GestionAPP();   // Carga config + datos persistidos

    // =========================================================================
    // PUNTO DE ENTRADA
    // =========================================================================

    public static void main(String[] args) {
        app.insercionDatos(); // Solo inserta si no hay datos previos

        // Credenciales MOK — visibles siempre al arrancar para facilitar pruebas
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║           USUARIOS DE PRUEBA (MOK)               ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║  ana@fernanpop.com      │ Contraseña: 1234       ║");
        System.out.println("║  luis@fernanpop.com     │ Contraseña: 1234       ║");
        System.out.println("║  marta@fernanpop.com    │ Contraseña: 1234       ║");
        System.out.println("║  admin@fernanpop.com    │ Contraseña: admin      ║");
        System.out.println("║  carlos@fernanpop.com   │ Contraseña: 1234       ║");
        System.out.println("║  laura@fernanpop.com    │ Contraseña: 1234       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        boolean salir = false;

        do {
            int opSinlog = pintarMenuSinLog();

            switch (opSinlog) {
                case -1:
                    System.out.println("ERROR: Debes introducir un número.");
                    break;

                case 1: // Buscar productos (acceso sin login)
                    menuBusqueda();
                    break;

                case 2: // Iniciar sesión
                    salir = flujoLogin();
                    break;

                case 3: // Registrarse
                    registrarUsuario();
                    break;

                case 4: // Salir
                    System.out.println("Saliendo del programa. ¡Hasta pronto!");
                    salir = true;
                    break;

                default:
                    System.out.println("Introduce un valor correcto.");
                    break;
            }
        } while (!salir);
    }

    // =========================================================================
    // FLUJO DE LOGIN
    // =========================================================================

    /**
     * Gestiona el login y el menú de usuario logueado.
     * @return true si el usuario eligió "Salir de la aplicación" (opción 10).
     */
    private static boolean flujoLogin() {
        System.out.print("Dime tu email: ");
        String email = s.nextLine();
        System.out.print("Dime la contraseña/clave: ");
        String contrasenia = s.nextLine();

        Usuario userActivo = app.login(email, contrasenia);

        if (userActivo == null) {
            System.out.println("Usuario o contraseña incorrectos.");
            Utils.pulsaContinuar(s);
            return false;
        }

        //Mensaje de última sesión desde properties
        String ultimaSesion = AppConfig.getUltimaSesion(email);
        if (ultimaSesion != null) {
            System.out.println("\n  > Usted inició sesión por última vez el " + ultimaSesion);
        } else {
            System.out.println("\n  > Bienvenido/a, es su primer inicio de sesión.");
        }

        System.out.println("  > Bienvenido/a, " + userActivo.getNombre() + " " + userActivo.getApel() + "!");
        Utils.pulsaContinuar(s);

        // Menú de usuario logueado
        return menuUsuario(userActivo);
    }

    // =========================================================================
    // MENÚ DE USUARIO LOGUEADO
    // =========================================================================

    /**
     * @return true si el usuario eligió "Salir de la aplicación".
     */
    private static boolean menuUsuario(Usuario userActivo) {
        int opLog;
        boolean salirApp = false;

        do {
            opLog = pintarMenuLog(userActivo);

            switch (opLog) {
                case 1: // Perfil
                    mostrarPerfil(userActivo);
                    break;

                case 2: // Cambiar datos
                    modificarDatos(userActivo);
                    break;

                case 3: // Mis productos
                    menuMisProductos(userActivo);
                    break;

                case 4: // Añadir producto
                    añadirProducto(userActivo);
                    break;

                case 5: // Buscar productos
                    menuBusqueda();
                    break;

                case 6: // Valoraciones
                    menuValoraciones(userActivo);
                    break;

                case 7: // Historial
                    menuHistorial(userActivo);
                    break;

                case 8: // Borrar perfil
                    if (confirmarBorrarPerfil(userActivo)) {
                        opLog = 9; // Forzamos salida del menú de usuario
                    }
                    break;

                case 9: // Cerrar sesión
                    app.logout(userActivo.getEmail());
                    System.out.println("Sesión cerrada. ¡Hasta pronto!");
                    Utils.pulsaContinuar(s);
                    break;

                case 10: // Salir de la app
                    app.logout(userActivo.getEmail());
                    System.out.println("Saliendo de la aplicación...");
                    salirApp = true;
                    break;

                // ---- Opciones de ADMINISTRADOR ----
                case 11:
                    if (userActivo.isEsAdmin()) menuAdminConfig(userActivo);
                    break;
                case 12:
                    if (userActivo.isEsAdmin()) adminEnviarListado(userActivo);
                    break;
                case 13:
                    if (userActivo.isEsAdmin()) adminBackup();
                    break;
            }
        } while (opLog != 9 && opLog != 10 && !salirApp);

        return salirApp;
    }

    // =========================================================================
    // REGISTRO DE NUEVO USUARIO
    // =========================================================================

    private static void registrarUsuario() {
        String nombre, apellidos, clave, email, clave2;
        long movil = 0;
        boolean comprobarClave, comprobarTelefono = false;

        System.out.print("Dime tu nombre: ");
        nombre = s.nextLine();
        System.out.print("Dime tus apellidos: ");
        apellidos = s.nextLine();
        System.out.print("Dime tu email: ");
        email = s.nextLine();

        // Validar teléfono (FIX: long en vez de int)
        do {
            System.out.print("Dime tu número de teléfono (9 dígitos): ");
            try {
                String movilStr = s.nextLine();
                if (movilStr.length() == 9) {
                    movil = Long.parseLong(movilStr);
                    comprobarTelefono = true;
                } else {
                    System.out.println("El número de teléfono debe tener exactamente 9 dígitos.");
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Introduce solo números.");
            }
        } while (!comprobarTelefono);

        // Validar claves
        do {
            System.out.print("Dime una clave: ");
            clave = s.nextLine();
            System.out.print("Repite la clave: ");
            clave2 = s.nextLine();
            comprobarClave = clave.equals(clave2);
            if (!comprobarClave) System.out.println("Las claves no coinciden. Inténtalo de nuevo.");
        } while (!comprobarClave);

        // Verificación de email con manejo de fallo de envío
        // Si el email no existe o el servidor rechaza el correo, no quedamos en bucle infinito.
        System.out.println("\nVamos a verificar tu correo electrónico...");
        if (!verificarEmail(nombre, apellidos, email)) {
            System.out.println("Registro cancelado: no se pudo verificar el email.");
            Utils.pulsaContinuar(s);
            return;
        }

        Usuario nuevo = app.crearUsuario(nombre, apellidos, email, movil, clave);
        if (nuevo != null) {
            System.out.println("¡Registro completado! Bienvenido/a, " + nuevo.getNombre() + ".");
        } else {
            System.out.println("Error: ese email ya está registrado.");
        }
        Utils.pulsaContinuar(s);
    }

    private static boolean verificarEmail(String nombre, String apellidos, String email) {
        final int MAX_INTENTOS = 3;
        int intentos = 0;

        while (intentos < MAX_INTENTOS) {
            String codigo = app.generarYEnviarCodigoVerificacion(nombre, apellidos, email);

            if (codigo == null) {
                // El envío falló (email inválido o sin conexión)
                System.out.println("No se pudo enviar el email de verificación a: " + email);
                System.out.print("¿Quieres intentarlo de nuevo? (S/N): ");
                if (!s.nextLine().equalsIgnoreCase("S")) {
                    return false; // El usuario cancela
                }
                intentos++;
                continue;
            }

            System.out.print("Introduce el código recibido por email (o escribe CANCELAR): ");
            String introducido = s.nextLine();

            if (introducido.equalsIgnoreCase("CANCELAR")) {
                return false;
            }

            if (codigo.equals(introducido)) {
                System.out.println("¡Correo verificado correctamente!");
                return true;
            } else {
                intentos++;
                int restantes = MAX_INTENTOS - intentos;
                if (restantes > 0) {
                    System.out.println("Código incorrecto. Te quedan " + restantes + " intento(s). Se enviará un nuevo código.");
                } else {
                    System.out.println("Demasiados intentos fallidos. Operación cancelada.");
                }
            }
        }
        return false;
    }

    // =========================================================================
    // MENÚ DE BÚSQUEDA
    // =========================================================================

    private static void menuBusqueda() {
        int opcionB;
        do {
            opcionB = pintarMenuBusqueda();
            System.out.println();

            switch (opcionB) {
                case 1:
                    if (app.getTotalProductos() == 0) {
                        System.out.println("No hay productos disponibles.");
                    } else {
                        for (Producto p : app.getAllProductos()) imprimirProducto(p);
                        System.out.println("*******************************************************");
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 2:
                    System.out.print("Introduzca el ID del producto: ");
                    try {
                        long idBusqueda = Long.parseLong(s.nextLine());
                        Producto pEncontrado = app.buscaProductoID(idBusqueda);
                        if (pEncontrado != null) {
                            imprimirProducto(pEncontrado);
                            System.out.println("*******************************************************");
                        } else {
                            System.out.printf("No se ha encontrado ningún producto con el ID: %d%n", idBusqueda);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Error: El ID debe ser un número.");
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 3:
                    System.out.print("Introduzca el texto a buscar: ");
                    String texto = s.nextLine();
                    ArrayList<Producto> resultados = app.buscaProductosTexto(texto);
                    if (resultados.isEmpty()) {
                        System.out.printf("No se han encontrado productos con: '%s'%n", texto);
                    } else {
                        for (Producto p : resultados) imprimirProducto(p);
                        System.out.println("*******************************************************");
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 4:
                    System.out.println("Volviendo...");
                    break;

                default:
                    if (opcionB != -1) System.out.println("Opción no disponible.");
                    break;
            }
        } while (opcionB != 4);
    }

    // =========================================================================
    // MIS PRODUCTOS
    // =========================================================================

    private static void menuMisProductos(Usuario userActivo) {
        int opProd;
        ArrayList<Producto> misProductos;

        do {
            misProductos = app.getProductosUser(userActivo.getEmail());

            System.out.println("\n******************************************");
            System.out.println("      Menú de mis productos en venta");
            System.out.println("1.  Mostrar todos mis productos");
            System.out.println("2.  Borrar un producto");
            System.out.println("3.  Vender un producto");
            System.out.println("4.  Volver");
            System.out.print("Introduzca la opción deseada: ");

            try { opProd = Integer.parseInt(s.nextLine()); }
            catch (NumberFormatException e) { opProd = -1; }

            switch (opProd) {
                case 1:
                    System.out.println("\n=== LISTADO DE MIS PRODUCTOS ===");
                    if (misProductos.isEmpty()) {
                        System.out.println("Actualmente no tienes ningún producto publicado.");
                    } else {
                        System.out.printf("Tienes %d productos registrados:%n", misProductos.size());
                        System.out.println("---------------------------------------------------------");
                        for (Producto p : misProductos) {
                            System.out.printf("""
                                    ID:          %d
                                    Título:      %s
                                    Precio:      %.2f €
                                    Estado:      %s
                                    Descripción: %s
                                    ---------------------------------------------------------
                                    """, p.getId(), p.getTitulo(), p.getPrecio(), p.getEstado(), p.getDescripcion());
                        }
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 2:
                    System.out.print("Escribe el ID del producto a borrar: ");
                    try {
                        long idBorrar = Long.parseLong(s.nextLine());
                        if (userActivo.quitaProducto(idBorrar)) {
                            app.guardarDatos();
                            System.out.println("¡Producto borrado correctamente!");
                        } else {
                            System.out.println("ERROR. No tienes ningún producto con ese ID.");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("ERROR. Introduce un número.");
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 3:
                    venderProducto(userActivo);
                    break;

                case 4:
                    System.out.println("Volviendo al menú principal...");
                    break;

                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opProd != 4);
    }

    private static void venderProducto(Usuario userActivo) {
        System.out.println("--- Finalizar Venta ---");
        long idProducto = -1;
        boolean idValido = false;

        do {
            System.out.print("Introduce el ID del producto que has vendido: ");
            try {
                idProducto = Long.parseLong(s.nextLine());
                idValido = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: El ID debe ser un número.");
            }
        } while (!idValido);

        Producto producto = app.validarProductoPropio(idProducto, userActivo);
        if (producto == null) {
            System.out.println("Error: El producto no existe o no te pertenece.");
            Utils.pulsaContinuar(s);
            return;
        }

        System.out.println("\nProducto: " + producto.getTitulo() + " (" + producto.getPrecio() + "€)");
        System.out.print("¿Confirmas la venta? (S/N): ");
        if (!s.nextLine().equalsIgnoreCase("S")) {
            System.out.println("Venta cancelada.");
            return;
        }

        System.out.print("Email del comprador: ");
        Usuario comprador = app.buscaMail(s.nextLine());
        if (comprador == null) {
            System.out.println("Error: Comprador no encontrado en el sistema.");
            Utils.pulsaContinuar(s);
            return;
        }
        if (comprador.getEmail().equalsIgnoreCase(userActivo.getEmail())) {
            System.out.println("Error: No puedes venderte un producto a ti mismo.");
            Utils.pulsaContinuar(s);
            return;
        }

        int idTrato = app.registrarVenta(userActivo, comprador, producto);
        if (idTrato == -1) {
            System.out.println("Error interno al registrar la venta.");
            Utils.pulsaContinuar(s);
            return;
        }

        System.out.println("Venta completada con éxito. (ID trato: " + idTrato + ")");

        // Enviar notificaciones de texto
        boolean emailsOk = app.enviarNotificacionesVenta(userActivo, comprador, producto);
        if (!emailsOk) System.out.println("Aviso: no se pudieron enviar las notificaciones por email.");

        // Enviar PDF de resumen al comprador
        System.out.println("Enviando resumen de compra en PDF al comprador...");
        boolean pdfOk = app.enviarResumenVentaPDF(userActivo, comprador, producto, idTrato);
        if (pdfOk) {
            System.out.println("PDF de resumen enviado correctamente a " + comprador.getEmail() + ".");
        } else {
            System.out.println("Aviso: no se pudo enviar el PDF de resumen.");
        }

        Utils.pulsaContinuar(s);
    }

    // =========================================================================
    // AÑADIR PRODUCTO
    // =========================================================================

    private static void añadirProducto(Usuario userActivo) {
        System.out.println("\n--- NUEVO PRODUCTO EN VENTA ---");
        System.out.print("Título: ");
        String titulo = s.nextLine();
        System.out.print("Descripción: ");
        String desc = s.nextLine();

        double precio = 0;
        boolean precioCorrecto = false;
        do {
            System.out.print("Precio: ");
            try {
                precio = Double.parseDouble(s.nextLine().replace(",", "."));
                if (precio <= 0) System.out.println("El precio debe ser mayor a 0.");
                else precioCorrecto = true;
            } catch (NumberFormatException e) {
                System.out.println("Error: Introduce un número válido (ej: 12.50).");
            }
        } while (!precioCorrecto);

        String estado;
        do {
            System.out.print("Estado (Nuevo/Usado): ");
            estado = s.nextLine().trim();
            if (!estado.equalsIgnoreCase("Nuevo") && !estado.equalsIgnoreCase("Usado")) {
                System.out.println("Error: Solo puedes elegir entre 'Nuevo' o 'Usado'.");
            }
        } while (!estado.equalsIgnoreCase("Nuevo") && !estado.equalsIgnoreCase("Usado"));

        Producto nuevoProducto = new Producto(app.generarIdProductoUnico(), titulo, desc, precio, estado);

        // FIX: usamos publicarProducto() que registra en log y guarda datos
        if (app.publicarProducto(userActivo, nuevoProducto)) {
            System.out.println("¡Producto '" + titulo + "' publicado con éxito!");
        } else {
            System.out.println("Error: El sistema no pudo procesar el producto.");
        }
        Utils.pulsaContinuar(s);
    }

    // =========================================================================
    // VALORACIONES
    // =========================================================================

    // FIX #6: menuValoraciones ahora tiene su propio do-while
    private static void menuValoraciones(Usuario userActivo) {
        int opcionV;
        do {
            opcionV = pintarMenuValoraciones();
            System.out.println();

            switch (opcionV) {
                case 1:
                    ArrayList<Trato> listaPendientes = app.getValoracionesPendientes(userActivo);
                    if (listaPendientes.isEmpty()) {
                        System.out.println("No tienes tratos pendientes de valorar.");
                    } else {
                        System.out.println("Tratos pendientes de valorar:");
                        System.out.println("******************************************************");
                        for (Trato t : listaPendientes) {
                            System.out.printf("""
                                    ID: %d
                                    Vendedor: %s
                                    Producto: %s
                                    Precio: %.2f €
                                    ------------------------------------------------------
                                    """, t.getId(), t.getEmailOtroUser(),
                                    t.getProducto().getTitulo(), t.getPrecio());
                        }
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 2:
                    System.out.print("Introduzca el ID de la compra a valorar: ");
                    int idAValorar;
                    try {
                        idAValorar = Integer.parseInt(s.nextLine());
                    } catch (NumberFormatException e) {
                        System.out.println("Error: El ID debe ser un número.");
                        break;
                    }

                    Trato tratoAValorar = app.buscaCompraPorId(userActivo, idAValorar);
                    if (tratoAValorar == null) {
                        System.out.println("No se encontró ninguna compra pendiente con ese ID.");
                        break;
                    }

                    int nota = 0;
                    do {
                        System.out.print("Puntuación (1 a 5): ");
                        try { nota = Integer.parseInt(s.nextLine()); }
                        catch (NumberFormatException e) { nota = -1; }
                        if (nota < 1 || nota > 5) System.out.println("Error: Nota entre 1 y 5.");
                    } while (nota < 1 || nota > 5);

                    System.out.print("Comentario: ");
                    String comentario = s.nextLine();

                    if (app.registrarValoracion(userActivo, idAValorar, nota, comentario)) {
                        System.out.println("Valoración registrada con éxito.");
                    } else {
                        System.out.println("No se pudo registrar la valoración.");
                    }
                    Utils.pulsaContinuar(s);
                    break;

                case 3:
                    System.out.println("Volviendo...");
                    break;

                default:
                    System.out.println("Opción no válida.");
                    break;
            }
        } while (opcionV != 3);
    }

    // =========================================================================
    // HISTORIAL DE TRATOS
    // =========================================================================

    private static void menuHistorial(Usuario userActivo) {
        int opcionH;
        do {
            opcionH = pintarMenuHistorial();
            switch (opcionH) {
                case 1:
                    System.out.println("\n--- LISTADO DE COMPRAS ---");
                    listarCompras(userActivo);
                    Utils.pulsaContinuar(s);
                    break;
                case 2:
                    System.out.println("\n--- LISTADO DE VENTAS ---");
                    listarVentas(userActivo);
                    Utils.pulsaContinuar(s);
                    break;
                case 3:
                    System.out.println("Volviendo al menú principal...");
                    break;
                default:
                    if (opcionH != -1) System.out.println("Opción no disponible.");
                    break;
            }
        } while (opcionH != 3);
    }

    // =========================================================================
    // PERFIL Y MODIFICAR DATOS
    // =========================================================================

    private static void mostrarPerfil(Usuario userActivo) {
        double media = app.notaMedia(userActivo);
        String mediaStr = (media < 0) ? "Sin valoraciones aún" : String.format("%.1f / 5.0", media);
        System.out.println("\n=== PERFIL DE USUARIO ===");
        System.out.println("Nombre:        " + userActivo.getNombre());
        System.out.println("Apellido:      " + userActivo.getApel());
        System.out.println("Móvil:         " + userActivo.getMovil());
        System.out.println("Email:         " + userActivo.getEmail());
        System.out.println("Valoración:    " + mediaStr);
        if (userActivo.isEsAdmin()) System.out.println("Rol:           ADMINISTRADOR");
        System.out.println("=========================");
        Utils.pulsaContinuar(s);
    }

    private static boolean confirmarBorrarPerfil(Usuario userActivo) {
        System.out.println("\n¡ATENCIÓN! Estás a punto de borrar tu cuenta permanentemente.");
        System.out.print("¿Estás seguro? (S/N): ");
        if (s.nextLine().equalsIgnoreCase("S")) {
            if (app.borrarUsuario(userActivo)) {
                System.out.println("Tu perfil ha sido eliminado. Esperamos volver a verte.");
                Utils.pulsaContinuar(s);
                return true;
            } else {
                System.out.println("Hubo un error al eliminar tu perfil.");
            }
        } else {
            System.out.println("Operación cancelada. Tu perfil sigue activo.");
        }
        Utils.pulsaContinuar(s);
        return false;
    }

    private static void modificarDatos(Usuario userActivo) {
        int opCambio = 0;
        do {
            System.out.println("\n--- MODIFICAR DATOS ---");
            System.out.println("1. Nombre   (Actual: " + userActivo.getNombre() + ")");
            System.out.println("2. Apellido (Actual: " + userActivo.getApel() + ")");
            System.out.println("3. Móvil    (Actual: " + userActivo.getMovil() + ")");
            System.out.println("4. Email    (Actual: " + userActivo.getEmail() + ")");
            System.out.println("5. Volver");
            System.out.print("¿Qué dato deseas modificar?: ");

            try {
                opCambio = Integer.parseInt(s.nextLine());
                switch (opCambio) {
                    case 1:
                        System.out.print("Nuevo nombre: ");
                        userActivo.setNombre(s.nextLine());
                        app.guardarDatos();
                        System.out.println("¡Dato actualizado!");
                        break;
                    case 2:
                        System.out.print("Nuevo apellido: ");
                        userActivo.setApel(s.nextLine());
                        app.guardarDatos();
                        System.out.println("¡Dato actualizado!");
                        break;
                    case 3:
                        System.out.print("Nuevo móvil (9 dígitos): ");
                        try {
                            String movilStr = s.nextLine();
                            if (movilStr.length() == 9) {
                                userActivo.setMovil(Long.parseLong(movilStr));
                                app.guardarDatos();
                                System.out.println("¡Dato actualizado!");
                            } else {
                                System.out.println("Error: El móvil debe tener exactamente 9 dígitos.");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Introduce solo números.");
                        }
                        break;
                    case 4:
                        System.out.print("Introduce el nuevo email: ");
                        String nuevoEmail = s.nextLine().trim();
                        if (app.buscaMail(nuevoEmail) != null) {
                            System.out.println("Error: Ese email ya está registrado en el sistema.");
                            break;
                        }
                        System.out.println("Verificando el nuevo email...");
                        // FIX: mismo mecanismo anti-bucle que en el registro
                        if (verificarEmail(userActivo.getNombre(), userActivo.getApel(), nuevoEmail)) {
                            userActivo.setEmail(nuevoEmail);
                            app.guardarDatos();
                            System.out.println("¡Email verificado y actualizado con éxito!");
                        } else {
                            System.out.println("Cambio de email cancelado.");
                        }
                        break;
                    case 5:
                        System.out.println("Volviendo...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (Exception e) {
                System.out.println("Error: Valor no válido.");
            }
        } while (opCambio != 5);
    }

    // =========================================================================
    // MENÚS DE ADMINISTRADOR
    // =========================================================================

    private static void menuAdminConfig(Usuario admin) {
        System.out.println("\n========== CONFIGURACIÓN DEL SISTEMA ==========");
        System.out.println("\n--- Propiedades actuales ---");
        Properties props = AppConfig.getAllProps();
        for (String key : props.stringPropertyNames()) {
            System.out.println("  " + key + " = " + props.getProperty(key));
        }

        System.out.println("\n--- Contenido del log de actividad ---");
        System.out.println(app.getContenidoLog());
        System.out.println("================================================");
        Utils.pulsaContinuar(s);
    }

    private static void adminEnviarListado(Usuario admin) {
        System.out.println("\n--- ENVIAR LISTADO DE PRODUCTOS POR EMAIL ---");
        System.out.println("El listado se enviará a: " + admin.getEmail());
        System.out.print("¿Confirmar? (S/N): ");
        if (!s.nextLine().equalsIgnoreCase("S")) {
            System.out.println("Cancelado.");
            return;
        }
        System.out.println("Generando y enviando fichero Excel/CSV...");
        boolean ok = app.enviarListadoProductosPorEmail(admin.getEmail());
        if (ok) {
            System.out.println("¡Listado enviado correctamente a " + admin.getEmail() + "!");
        } else {
            System.out.println("Error al generar o enviar el listado.");
        }
        Utils.pulsaContinuar(s);
    }

    private static void adminBackup() {
        System.out.println("\n--- COPIA DE SEGURIDAD ---");
        System.out.println("Introduce la ruta donde guardar la copia de seguridad.");
        System.out.println("  Ejemplo Windows: C:\\Users\\nacho\\Desktop\\backup");
        System.out.println("  Ejemplo relativa: backup  (se creará en el mismo directorio que la app)");
        System.out.print("Ruta de destino: ");
        String ruta = s.nextLine().trim();

        if (ruta.isEmpty()) {
            ruta = "backup";
            System.out.println("Ruta vacía. Se usará la ruta por defecto: backup/");
        }

        boolean ok = app.realizarBackup(ruta);
        if (ok) {
            System.out.println("¡Copia de seguridad realizada correctamente en: " + ruta);
        } else {
            System.out.println("Error al realizar la copia de seguridad. Comprueba la ruta.");
        }
        Utils.pulsaContinuar(s);
    }

    // =========================================================================
    // PINTADO DE MENÚS
    // =========================================================================

    private static int pintarMenuSinLog() {
        boolean accesoInvitado = AppConfig.isAccesoInvitado();

        System.out.print("""
                ****************************************
                          Menú sin loguear
                1. Buscar Productos.
                2. Iniciar sesión.
                3. Registrarse.
                4. Salir.
                """);
        if (accesoInvitado) {
            System.out.println("  [Acceso invitado ACTIVADO: puedes consultar el catálogo sin login]");
        }
        System.out.print("Introduce la opción deseada: ");
        try {
            return Integer.parseInt(s.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static int pintarMenuLog(Usuario user) {
        int numVal = (user.getValoracionesPendientes() != null)
                ? user.getValoracionesPendientes().size() : 0;

        boolean esAdmin = user.isEsAdmin();

        System.out.printf("""
                ******************************************
                Tiene usted %d tratos que valorar
                          Menú de usuario
                1.  Mostrar mi perfil de usuario
                2.  Cambiar mis datos personales
                3.  Ver mis productos en venta
                4.  Introducir un producto para vender
                5.  Buscar Productos
                6.  Ver valoraciones pendientes
                7.  Ver mi historial de tratos
                8.  Borrar mi perfil de usuario
                9.  Cerrar sesión
                10. Salir de la aplicación
                """, numVal);

        if (esAdmin) {
            System.out.println("--- OPCIONES DE ADMINISTRADOR ---");
            System.out.println("11. Ver configuración y log del sistema");
            System.out.println("12. Enviar listado de productos por email");
            System.out.println("13. Realizar copia de seguridad");
        }

        System.out.print("Introduzca la opción deseada: ");

        try {
            int op = Integer.parseInt(s.nextLine());
            int maxOp = esAdmin ? 13 : 10;
            if (op < 1 || op > maxOp) {
                System.out.println("Introduce un valor entre 1 y " + maxOp + ".");
                Utils.pulsaContinuar(s);
                return pintarMenuLog(user); // Recursión simple en vez de bucle anidado
            }
            return op;
        } catch (NumberFormatException e) {
            System.out.println("ERROR. Debes introducir un número.");
            Utils.pulsaContinuar(s);
            return pintarMenuLog(user);
        }
    }

    private static int pintarMenuBusqueda() {
        System.out.print("""
                ***********************************************
                         Menú de búsqueda de productos
                1.  Mostrar todos los productos
                2.  Buscar por ID
                3.  Buscar por texto en título o descripción
                4.  Volver
                ***********************************************
                Introduzca la opción deseada:\s""");
        try { return Integer.parseInt(s.nextLine()); }
        catch (NumberFormatException e) { return -1; }
    }

    private static int pintarMenuHistorial() {
        System.out.print("""
                *******************************
                     HISTORIAL DE TRATOS
                1. Historial de Compras
                2. Historial de Ventas
                3. Volver al menú
                *******************************
                Selecciona una opción:\s""");
        try { return Integer.parseInt(s.nextLine()); }
        catch (NumberFormatException e) {
            System.out.println("Error: Introduce un número válido (1, 2 o 3).");
            return -1;
        }
    }

    private static int pintarMenuValoraciones() {
        System.out.print("""
                ***********************************
                        Menú de valoraciones
                1. Mostrar mis valoraciones pendientes
                2. Valorar una compra
                3. Volver
                ***********************************
                Introduzca la opción deseada:\s""");
        try { return Integer.parseInt(s.nextLine()); }
        catch (NumberFormatException e) { return -1; }
    }

    // =========================================================================
    // PRESENTACIÓN DE DATOS
    // =========================================================================

    private static void imprimirProducto(Producto p) {
        System.out.printf("""
                *******************************************************
                Producto ID: %d
                Titulo:      %s
                Descripcion: %s
                Precio:      %.2f €
                Estado:      %s
                """, p.getId(), p.getTitulo(), p.getDescripcion(), p.getPrecio(), p.getEstado());
    }

    private static void listarCompras(Usuario user) {
        if (user.getCompras().isEmpty()) {
            System.out.println("No tienes compras registradas en tu historial.");
            return;
        }
        for (Trato t : user.getCompras()) {
            String fechaStr = t.getFecha().get(java.util.Calendar.DAY_OF_MONTH) + "/" +
                    (t.getFecha().get(java.util.Calendar.MONTH) + 1) + "/" +
                    t.getFecha().get(java.util.Calendar.YEAR);
            System.out.printf("""
                    *******************************
                    FICHA DE COMPRA (ID: %d)
                    Fecha:    %s
                    Producto: %s
                    Importe:  %.2f€
                    Vendedor: %s
                    %n""", t.getId(), fechaStr, t.getProducto().getTitulo(),
                    t.getPrecio(), t.getEmailOtroUser());
        }
    }

    private static void listarVentas(Usuario user) {
        if (user.getVentas().isEmpty()) {
            System.out.println("No tienes ventas registradas en tu historial.");
            return;
        }
        for (Trato t : user.getVentas()) {
            String fechaStr = t.getFecha().get(java.util.Calendar.DAY_OF_MONTH) + "/" +
                    (t.getFecha().get(java.util.Calendar.MONTH) + 1) + "/" +
                    t.getFecha().get(java.util.Calendar.YEAR);
            System.out.printf("""
                    *******************************
                    FICHA DE VENTA (ID: %d)
                    Fecha:      %s
                    Producto:   %s
                    Importe:    %.2f€
                    Comprador:  %s
                    Valoracion: %d/5
                    Comentario: %s
                    %n""", t.getId(), fechaStr, t.getProducto().getTitulo(),
                    t.getPrecio(), t.getEmailOtroUser(),
                    t.getPuntuacion(), t.getComentario());
        }
    }
}
