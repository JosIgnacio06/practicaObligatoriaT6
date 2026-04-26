package vistas;
import modelos.Trato;
import modelos.Usuario;
import modelos.Producto;
import utils.Utils;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

public class Main {
    // Scanner e instancia para todos los métodos
    public static Scanner s = new Scanner(System.in);
    static GestionAPP app = new GestionAPP(new ArrayList<>());

    static void main() {
        int opLog = 0, opSinlog;
        String user, contrasenia;

        do {
            opSinlog = pintarMenuSinLog();
            switch (opSinlog) {
                case -1:
                    System.out.println("ERROR: Debes de introducir un número.");
                    break;

                case 1: // Buscar Productos (Menú Sin Loguear / Menú Logueado)
                    int opcionB;
                    do {
                        // 1. Mostramos el submenú de búsqueda
                        opcionB = app.mostrarMenuBusqueda(s);
                        System.out.printf("%n");

                        switch (opcionB) {
                            case 1: // Mostrar todos
                                if (app.getTotalProductos() == 0) {
                                    System.out.println("No hay productos disponibles.");
                                } else {
                                    for (Usuario u : app.getUsuarios()) {
                                        for (Producto p : u.getEnVenta()) {
                                            System.out.printf("""
                                    *******************************************************
                                    Informacion del producto. id: %d
                                    Titulo: %s
                                    Descripcion: %s
                                    Precio: %.1f
                                    Estado: %s
                                    """, p.getId(), p.getTitulo(), p.getDescripcion(), p.getPrecio(), p.getEstado());
                                        }
                                    }
                                    System.out.println("*******************************************************");
                                }
                                break;

                            case 2: // Buscar por ID
                                System.out.print("Introduzca el ID del producto a buscar: ");
                                try {
                                    long idBusqueda = Long.parseLong(s.nextLine());
                                    Producto pEncontrado = app.buscaProductoID(idBusqueda);
                                    if (pEncontrado != null) {
                                        System.out.printf("""
                                *******************************************************
                                Informacion del producto. id: %d
                                Titulo: %s
                                Descripcion: %s
                                Precio: %.1f
                                Estado: %s
                                *******************************************************
                                """, pEncontrado.getId(), pEncontrado.getTitulo(), pEncontrado.getDescripcion(), pEncontrado.getPrecio(), pEncontrado.getEstado());
                                    } else {
                                        System.out.printf("No se ha encontrado ningún producto con el ID: %d%n", idBusqueda);
                                    }
                                } catch (NumberFormatException e) {
                                    System.out.println("Error: El ID debe ser un numero.");
                                }
                                break;

                            case 3: // Buscar por Texto
                                System.out.print("Introduzca el texto a buscar: ");
                                String texto = s.nextLine();
                                ArrayList<Producto> resultados = app.buscaProductosTexto(texto);

                                if (resultados.isEmpty()) {
                                    System.out.printf("No se han encontrado productos con: '%s'%n", texto);
                                } else {
                                    for (Producto p : resultados) {
                                        System.out.printf("""
                                *******************************************************
                                Informacion del producto. id: %d
                                Titulo: %s
                                Descripcion: %s
                                Precio: %.1f
                                Estado: %s
                                """, p.getId(), p.getTitulo(), p.getDescripcion(), p.getPrecio(), p.getEstado());
                                    }
                                    System.out.println("*******************************************************");
                                }
                                break;

                            case 4:
                                System.out.println("Volviendo...");
                                break;

                            default:
                                if (opcionB != -1) {
                                    System.out.println("Opción no disponible.");
                                }
                                break;
                        }

                        if (opcionB >= 1 && opcionB <= 3) {
                            Utils.pulsaContinuar(s);
                        }

                    } while (opcionB != 4);
                    break;

                case 2: // Iniciar sesión
                    System.out.print("Dime el usuario/nombre: ");
                    user = s.nextLine();
                    System.out.print("Dime la contraseña/clave: ");
                    contrasenia = s.nextLine();

                    // Llamada a la instancia 'app'
                    Usuario userActivo = app.login(user, contrasenia);

                    if (userActivo == null) {
                        System.out.println("Usuario o contraseña incorrecta");
                        Utils.pulsaContinuar(s);
                    } else {
                        do {
                            opLog = pintarMenuLog(userActivo);
                            // --- MENÚ DE USUARIO LOGUEADO ---
                            switch (opLog) {
                                case 1: // Mostrar perfil
                                    System.out.println("\n=== PERFIL DE USUARIO ===");
                                    System.out.println("Nombre:   " + userActivo.getNombre());
                                    System.out.println("Apellido: " + userActivo.getApel());
                                    System.out.println("Móvil:    " + userActivo.getMovil());
                                    System.out.println("Email:    " + userActivo.getEmail());
                                    System.out.println("=========================");
                                    Utils.pulsaContinuar(s);
                                    break;

                                case 2: // Cambiar datos personales
                                    modificarDatos(userActivo);
                                    break;

                                case 3: // Menú de mis productos en venta
                                    int opProd;
                                    ArrayList<Producto> misProductos = app.getProductosUser(userActivo.getEmail());

                                    do {
                                        System.out.println("\n******************************************");
                                        System.out.println("      Menú de mis productos en venta");
                                        System.out.println("1.  Mostrar todos mis productos");
                                        System.out.println("2.  Borrar un producto");
                                        System.out.println("3.  Vender un producto");
                                        System.out.println("4.  Volver");
                                        System.out.print("Introduzca la opción deseada: ");

                                        try {
                                            opProd = Integer.parseInt(s.nextLine());
                                        } catch (NumberFormatException e) {
                                            opProd = -1;
                                        }

                                        switch (opProd) {
                                            case 1: // Mostrar todos mis productos
                                                System.out.println("\n=== LISTADO DE MIS PRODUCTOS ===");
                                                if (misProductos.isEmpty()) {
                                                    System.out.println("Actualmente no tienes ningún producto publicado.");
                                                } else {
                                                    // Usamos el size() de la lista que ya tenemos cargada
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
                                                                        """,
                                                                p.getId(), p.getTitulo(), p.getPrecio(),
                                                                p.getEstado(), p.getDescripcion());
                                                    }
                                                }
                                                Utils.pulsaContinuar(s);
                                                break;

                                            case 2: // Borrar un producto
                                                long idBorrar;

                                                System.out.print("Escribe el ID del producto: ");

                                                // 1. Validamos primero el formato del número como tú quieres
                                                try {
                                                    idBorrar = Long.parseLong(s.nextLine());
                                                } catch (NumberFormatException e) {
                                                    System.out.println("ERROR. Introduce un número.");
                                                    Utils.pulsaContinuar(s);
                                                    break; // Salimos del case 2 si no es un número
                                                }

                                                // 2. Una vez que el número es válido, intentamos borrarlo del sistema
                                                if (userActivo.quitaProducto(idBorrar)) {

                                                    // 3. Si se borra con éxito, refrescamos la lista local
                                                    // para que la opción 1 (Mostrar) esté siempre actualizada
                                                    misProductos = app.getProductosUser(userActivo.getEmail());

                                                    System.out.println("¡Producto borrado correctamente!");
                                                } else {
                                                    // Si quitaProducto devuelve false, es que el ID no le pertenece
                                                    System.out.println("ERROR. No tienes ningún producto con ese ID.");
                                                }

                                                Utils.pulsaContinuar(s);
                                                break;

                                            case 3: // Vender un producto
                                                System.out.println("--- Finalizar Venta ---");

                                                int idProducto = -1;
                                                boolean idValido = false;

                                                // 1. Validación del ID
                                                do {
                                                    try {
                                                        System.out.print("Introduce el ID del producto que has vendido: ");
                                                        idProducto = Integer.parseInt(s.nextLine());
                                                        idValido = true;
                                                    } catch (NumberFormatException e) {
                                                        System.out.println("Error: El ID debe ser un número entero.");
                                                    }
                                                } while (!idValido);

                                                // 2. Validación de propiedad
                                                Producto producto = app.validarProductoPropio(idProducto, userActivo);

                                                if (producto == null) {
                                                    System.out.println("Error: El producto no existe o no te pertenece.");
                                                    break;
                                                }

                                                // 3. Confirmación
                                                System.out.println("\nProducto: " + producto.getTitulo() + " (" + producto.getPrecio() + "€)");
                                                System.out.print("¿Confirmas la venta? (S/N): ");

                                                if (s.nextLine().equalsIgnoreCase("S")) {
                                                    System.out.print("Email del comprador: ");
                                                    Usuario comprador = app.buscaMail(s.nextLine());

                                                    if (comprador != null) {
                                                        // 1. Generamos el ID único una sola vez
                                                        int idTrato = app.generarIdTratoUnico();

                                                        // 2. Registramos la VENTA en el vendedor (userActivo)
                                                        // Este metodo interno de Usuario ya crea el objeto Trato por nosotros
                                                        userActivo.addTratoVenta(idTrato, comprador.getEmail(), producto);

                                                        // 3. Registramos la COMPRA en el comprador
                                                        // Creamos el objeto "espejo" para el comprador y se lo pasamos
                                                        Trato tCompra = new Trato(idTrato, "COMPRA", userActivo.getEmail(), producto, Calendar.getInstance(), producto.getPrecio(), "", 0);
                                                        comprador.addTratoCompra(tCompra);

                                                        // 4. Notificaciones y limpieza (lo de siempre)
                                                        app.enviarNotificacionesVenta(userActivo, comprador, producto);
                                                        comprador.getValoracionesPendientes().add(1);
                                                        userActivo.getEnVenta().remove(producto);

                                                        System.out.println("Venta completada con éxito.");
                                                    } else {
                                                        System.out.println("Error: Comprador no encontrado.");
                                                    }
                                                }
                                                break;

                                            case 4:
                                                System.out.println("Volviendo al menú principal...");
                                                break;

                                            default:
                                                System.out.println("Opción no válida.");
                                                break;
                                        }
                                    } while (opProd != 4);
                                    break;

                                case 4:
                                    System.out.println("\n--- NUEVO PRODUCTO EN VENTA ---");
                                    System.out.print("Título: ");
                                    String titulo = s.nextLine();
                                    System.out.print("Descripción: ");
                                    String desc = s.nextLine();

                                    double precio = 0;
                                    boolean precioCorrecto = false;
                                    // Validación de número decimal
                                    do {
                                        try {
                                            System.out.print("Precio: ");
                                            precio = Double.parseDouble(s.nextLine());
                                            // Validamos que no sea un precio negativo antes de seguir
                                            if (precio <= 0) {
                                                System.out.println("El precio debe ser mayor a 0.");
                                            } else {
                                                precioCorrecto = true;
                                            }
                                        } catch (NumberFormatException e) {
                                            System.out.println("Error: Introduce un número válido (ej: 12.50).");
                                        }
                                    } while (!precioCorrecto);

                                    String estado;
                                    // Validación de texto específico
                                    do {
                                        System.out.print("Estado (Nuevo/Usado): ");
                                        estado = s.nextLine();
                                        // Corregido: Solo entra si falla AMBAS condiciones
                                        if (!estado.equalsIgnoreCase("Nuevo") && !estado.equalsIgnoreCase("Usado")) {
                                            System.out.println("Error: Solo puedes elegir entre 'Nuevo' o 'Usado'.");
                                        }
                                    } while (!estado.equalsIgnoreCase("Nuevo") && !estado.equalsIgnoreCase("Usado"));

                                    // 2. Crear el objeto Producto
                                    // Se genera el ID automáticamente en el constructor
                                    Producto nuevoProducto = new Producto(app.generarIdProductoUnico(), titulo, desc, precio, estado);

                                    // 3. Usamos addProducto del usuario activo para guardarlo en su ArrayList
                                    if (userActivo.addProducto(nuevoProducto)) {
                                        System.out.println("¡Producto '" + titulo + "' publicado con éxito!");
                                    } else {
                                        System.out.println("Error: El sistema no pudo procesar el producto.");
                                    }

                                    Utils.pulsaContinuar(s);
                                    break;

                                case 5: // Búsqueda de productos
                                    // Mostramos el menú y obtenemos la opción validada
                                    do {
                                        opcionB = app.mostrarMenuBusqueda(s);
                                        System.out.println();

                                        switch (opcionB) {
                                            case 1: // Mostrar todos los productos del programa
                                                int total = app.getTotalProductos();

                                                if (total == 0) {
                                                    System.out.printf("No hay productos disponibles en el programa.%n");
                                                } else {
                                                    // Recorremos todos los usuarios para llegar a sus productos
                                                    for (Usuario u : app.getUsuarios()) {
                                                        for (Producto p : u.getEnVenta()) {
                                                            System.out.printf("""
                                                                            *******************************************************
                                                                            Informacion del producto. id: %d
                                                                            Titulo: %s
                                                                            Descripcion: %s
                                                                            Precio: %.1f
                                                                            Estado: %s
                                                                            """,
                                                                    p.getId(),
                                                                    p.getTitulo(),
                                                                    p.getDescripcion(),
                                                                    p.getPrecio(),
                                                                    p.getEstado());
                                                        }
                                                    }
                                                    // Al final de la lista, ponemos el separador y el Utils
                                                    System.out.printf("*******************************************************%n");
                                                    Utils.pulsaContinuar(s);
                                                }
                                                break;

                                            case 2: // Buscar productos con un id determinada
                                                System.out.print("Introduzca el ID del producto a buscar: ");
                                                long idBusqueda;
                                                try {
                                                    idBusqueda = Long.parseLong(s.nextLine());
                                                } catch (NumberFormatException e) {
                                                    System.out.printf("Error: El ID debe ser un número.%n");
                                                    break;
                                                }

                                                // Usamos tu metodo de búsqueda
                                                Producto pEncontrado = app.buscaProductoID(idBusqueda);

                                                if (pEncontrado != null) {
                                                    System.out.printf("""
                                                                    *******************************************************
                                                                    Información del producto. id: %d
                                                                    Título: %s
                                                                    Descripción: %s
                                                                    Precio: %.1f
                                                                    Estado: %s
                                                                    *******************************************************
                                                                    """,
                                                            pEncontrado.getId(),
                                                            pEncontrado.getTitulo(),
                                                            pEncontrado.getDescripcion(),
                                                            pEncontrado.getPrecio(),
                                                            pEncontrado.getEstado());
                                                } else {
                                                    System.out.printf("No se ha encontrado ningún producto con el ID: %d%n", idBusqueda);
                                                }

                                                System.out.print("Pulse tecla + Enter para continuar...... ");
                                                s.nextLine();
                                                break;

                                            case 3: // Buscar productos por texto en el nombre
                                                System.out.print("Introduzca el texto a buscar: ");
                                                String texto = s.nextLine();

                                                // Obtenemos la lista de coincidencias
                                                ArrayList<Producto> resultados = app.buscaProductosTexto(texto);

                                                if (resultados.isEmpty()) {
                                                    System.out.printf("No se han encontrado productos que contengan: '%s'%n", texto);
                                                } else {
                                                    System.out.printf("Resultados de la busqueda '%s':%n", texto);

                                                    for (Producto p : resultados) {
                                                        System.out.printf("""
                                                                        *******************************************************
                                                                        Informacion del producto. id: %d
                                                                        Titulo: %s
                                                                        Descripcion: %s
                                                                        Precio: %.1f
                                                                        Estado: %s
                                                                        """,
                                                                p.getId(),
                                                                p.getTitulo(),
                                                                p.getDescripcion(),
                                                                p.getPrecio(),
                                                                p.getEstado());
                                                    }
                                                    // Separador final de la lista
                                                    System.out.printf("*******************************************************%n");
                                                }

                                                System.out.print("Pulse tecla + Enter para continuar...... ");
                                                s.nextLine();
                                                break;

                                            case 4:
                                                System.out.println("Volviendo...");
                                                break;

                                            default:
                                                // Si el try-catch devolvió -1 o cualquier otro número inválido
                                                System.out.println("Opción no válida.");
                                                break;
                                        }
                                    } while (opcionB != 4);
                                    break;

                                case 6: // Gestión de Valoraciones
                                    int opcionV = app.mostrarMenuValoraciones(s);
                                    System.out.printf("%n"); // Salto de línea para separar el menú de la respuesta

                                    switch (opcionV) {
                                        case 1: // Mostrar mis valoraciones pendientes
                                            ArrayList<Trato> listaPendientes = app.getValoracionesPendientes(userActivo);

                                            if (listaPendientes.isEmpty()) {
                                                System.out.printf("No tienes tratos pendientes de valorar.%n");
                                            } else {
                                                System.out.printf("Tratos pendientes de valorar:%n");
                                                System.out.printf("******************************************************%n");

                                                for (Trato t : listaPendientes) {
                                                    System.out.printf("""
                                                                    Informacion de Venta
                                                                    ID: %d
                                                                    Vendedor: %s
                                                                    Producto: %s
                                                                    Precio: %.1f
                                                                    Puntuación: No valorado
                                                                    Comentario: No valorado
                                                                    ------------------------------------------------------
                                                                    """,
                                                            t.getId(),
                                                            t.getEmailOtroUser(),
                                                            t.getProducto().getTitulo(),
                                                            t.getPrecio());
                                                }

                                                Utils.pulsaContinuar(s);
                                            }
                                            break;

                                        case 2: // Valorar una compra
                                            System.out.print("Introduzca el ID de la compra a valorar: ");
                                            int idAValorar;
                                            try {
                                                idAValorar = Integer.parseInt(s.nextLine());
                                            } catch (NumberFormatException e) {
                                                System.out.printf("Error: El ID debe ser un numero.%n");
                                                break;
                                            }

                                            // 1. Buscamos el trato en las compras del usuario activo (Comprador)
                                            Trato tratoComprador = app.buscaCompraPorId(userActivo, idAValorar);

                                            if (tratoComprador != null) {
                                                // 2. Pedir Puntuacion con validacion
                                                int nota;
                                                String entrada;
                                                do {
                                                    System.out.print("Puntuacion (1 a 5): ");
                                                    entrada = s.nextLine();
                                                    try {
                                                        nota = Integer.parseInt(entrada);
                                                    } catch (NumberFormatException e) {
                                                        nota = -1; // Forzamos el error para que repita el bucle
                                                    }

                                                    if (nota < 1 || nota > 5) {
                                                        System.out.printf("Error: Nota entre 1 y 5.%n");
                                                    }
                                                } while (nota < 1 || nota > 5);

                                                // 3. Pedir Comentario
                                                System.out.print("Comentario: ");
                                                String comentario = s.nextLine();

                                                // 4. Actualizamos el trato del COMPRADOR
                                                tratoComprador.setPuntuacion(nota);
                                                tratoComprador.setComentario(comentario);

                                                // 5. ACTUALIZACION ESPEJO: Buscamos y actualizamos el trato del VENDEDOR
                                                // Buscamos en toda la app el trato de venta que tenga este mismo ID
                                                Trato tratoVendedor = app.buscaVentaPorId(idAValorar);
                                                if (tratoVendedor != null) {
                                                    tratoVendedor.setPuntuacion(nota);
                                                    tratoVendedor.setComentario(comentario);
                                                }

                                                // 6. Limpiamos la lista de notificaciones pendientes del usuario
                                                app.borraValoracionPendiente(userActivo, idAValorar);

                                                System.out.printf("Valoracion registrada con exito.%n");
                                            } else {
                                                System.out.printf("No se encontro ninguna compra pendiente con ese ID.%n");
                                            }
                                            break;

                                        case 3:
                                            System.out.printf("Volviendo...%n");
                                            break;

                                        default:
                                            // Mostramos el mensaje de error para -1 y cualquier otro número
                                            System.out.printf("Opción no válida.%n");
                                            break;
                                    }
                                    break;

                                case 7: // Historial de tratos
                                    // Mostramos el menú con el bloque de texto y obtenemos la opción validada
                                    int opcionH;
                                    do {
                                        opcionH = app.mostrarMenuHistorial(s);

                                        switch (opcionH) {
                                            case 1:
                                                System.out.printf("%n--- LISTADO DE COMPRAS ---%n");
                                                app.listarCompras(userActivo);
                                                Utils.pulsaContinuar(s);
                                                break;

                                            case 2:
                                                System.out.printf("%n--- LISTADO DE VENTAS ---%n");
                                                app.listarVentas(userActivo);
                                                Utils.pulsaContinuar(s);
                                                break;

                                            case 3:
                                                System.out.printf("Volviendo al menu principal...%n");
                                                Utils.pulsaContinuar(s);
                                                break;

                                            default:
                                                // Si el try-catch de mostrarMenuHistorial devolvió -1, ya se avisó del error
                                                if (opcionH != -1) {
                                                    System.out.printf("Opcion no disponible.%n");
                                                }
                                                break;
                                        }

                                    } while (opcionH != 3);
                                    break;

                                case 8: // Borrar mi perfil de usuario.
                                    System.out.println("\n¡ATENCIÓN! Estás a punto de borrar tu cuenta permanentemente.");
                                    System.out.print("¿Estás seguro de que quieres darte de baja? (S/N): ");
                                    String confirmacion = s.nextLine();

                                    if (confirmacion.equalsIgnoreCase("S")) {
                                        // 1. Usamos el metodo para borrarlo
                                        if (app.borrarUsuario(userActivo)) {
                                            System.out.println("Tu perfil ha sido eliminado con éxito. Esperamos volver a verte.");

                                            // 2. Forzamos la salida del menú de usuario logueado
                                            opLog = 9;
                                        } else {
                                            System.out.println("Hubo un error al intentar eliminar tu perfil. Inténtalo de nuevo.");
                                        }
                                        Utils.pulsaContinuar(s);
                                    } else {
                                        System.out.println("Operación cancelada. Tu perfil sigue activo.");
                                    }
                                    break;

                                case 9: //Cierra sesión
                                    System.out.println("Cerrando sesión...");
                                    break;

                                case 10: //Cierra la app
                                    System.out.println("Saliendo de la aplicación...");
                                    break;
                            }
                        } while (opLog != 10 && opLog != 9);
                    }
                    break;

                case 3: // Registro
                    Usuario nuevo = app.addUsuario();
                    app.getUsuarios().add(nuevo);
                    break;

                case 4:
                    System.out.println("Saliendo del programa...");
                    break;

                default:
                    System.out.println("Introduce un valor correcto.");
                    break;
            }
        } while (opSinlog != 4 && opLog != 10);
    }

    // --- MÉTODOS DE APOYO (Pintar menús y Submenú) ---

    private static int pintarMenuSinLog() {
        System.out.print("""
                ****************************************
                        Menú sin loguear
                1. Buscar Productos.
                2. Iniciar sesión.
                3. Registrarse.
                4. Salir.
                Introduce la opción deseada:\s""");
        try {
            return Integer.parseInt(s.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    //Una vez iniciamos sesión, pintamos este menú.
    private static int pintarMenuLog(Usuario user) {
        int op;
        do {
            // Usamos el getter para obtener la lista y luego consultamos su tamaño
            int numVal = (user.getValoracionesPendientes() != null)
                    ? user.getValoracionesPendientes().size()
                    : 0;

            System.out.printf("""
                    ******************************************
                    Tiene usted %d tratos que valorar
                              Menú de usuario
                    1.  Mostrar mi perfil de usuario
                    2.  Cambiar mis datos personales
                    3.  Ver mis productos en venta
                    4.  Introducir un producto para vender
                    5.  Buscar Productos.
                    6.  Ver valoraciones pendientes.
                    7.  Ver mi historial de tratos.
                    8.  Borrar mi perfil de usuario.
                    9.  Cerrar sesión
                    10. Salir
                    Introduzca la opción deseada:\s""", numVal);

            try {
                op = Integer.parseInt(s.nextLine());
                if (op < 1 || op > 10) {
                    System.out.println("Introduce un valor dentro de los parámetros");
                    Utils.pulsaContinuar(s);
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR. Debes introducir un número");
                op = -1;
                Utils.pulsaContinuar(s);
            }
        } while (op < 1 || op > 10);
        return op;
    }

    //Modificamos los datos del usuario
    private static void modificarDatos(Usuario userActivo) {
        int opCambio = 0;
        do {
            System.out.println("\n--- MODIFICAR DATOS ---");
            System.out.println("1. Nombre   (Actual: " + userActivo.getNombre() + ")");
            System.out.println("2. Apellido (Actual: " + userActivo.getApel() + ")");
            System.out.println("3. Móvil    (Actual: " + userActivo.getMovil() + ")");
            System.out.println("4. Email    (Actual: " + userActivo.getEmail() + ")");
            System.out.println("5. Volver al menú anterior");
            System.out.print("¿Qué dato desea modificar?: ");

            try {
                opCambio = Integer.parseInt(s.nextLine());
                switch (opCambio) {
                    case 1:
                        System.out.print("Nuevo nombre: ");
                        userActivo.setNombre(s.nextLine());
                        break;
                    case 2:
                        System.out.print("Nuevo apellido: ");
                        userActivo.setApel(s.nextLine());
                        break;
                    case 3:
                        System.out.print("Nuevo móvil: ");
                        userActivo.setMovil(Integer.parseInt(s.nextLine()));
                        break;
                    case 4:
                        System.out.print("Introduce el nuevo email: ");
                        String nuevoEmail = s.nextLine();

                        // Llamamos al metodo para volver a verificar el mail
                        // Pasamos el nombre y apellido actual del usuario y el NUEVO email
                        boolean verificado = app.verificarEmail(userActivo.getNombre(), userActivo.getApel(), nuevoEmail);

                        if (verificado) {
                            userActivo.setEmail(nuevoEmail);
                            System.out.println("¡Email verificado y actualizado con éxito!");
                        } else {
                            // En caso de que el metodo pudiera devolver false
                            System.out.println("No se pudo verificar el email. Se mantiene el anterior.");
                        }
                        break;
                    case 5:
                        System.out.println("Volviendo...");
                        break;

                    default:
                        System.out.println("Opción no válida.");
                }
                if (opCambio >= 1 && opCambio <= 4) System.out.println("¡Dato actualizado!");
            } catch (Exception e) {
                System.out.println("Error: Valor no válido.");
            }
        } while (opCambio != 5);
    }

}