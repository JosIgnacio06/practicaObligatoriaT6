package utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Sistema de log de actividad de Fernanpop.
 * Cada entrada sigue el formato CSV indicado en el enunciado.
 * El fichero se guarda en logs/fernanpop.log (ruta relativa al directorio de ejecución).
 */
public class LogUtils {

    // Ruta relativa: funciona tanto en desarrollo como en el .jar ejecutable
    private static final String LOG_PATH = "logs" + File.separator + "fernanpop.log";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // -------------------------------------------------------------------------
    // Métodos públicos de log
    // -------------------------------------------------------------------------

    /** a) Inicio de sesión: "Inicio de sesión";emailUser;fecha/hora */
    public static void logInicioSesion(String email) {
        escribir("\"Inicio de sesión\";" + email + ";" + ahora());
    }

    /** b) Cierre de sesión: "Cierre de sesión";emailUser;fecha/hora */
    public static void logCierreSesion(String email) {
        escribir("\"Cierre de sesión\";" + email + ";" + ahora());
    }

    /** c) Nuevo producto: "Nuevo producto en venta";IdProducto;idUser;fecha/hora */
    public static void logNuevoProducto(long idProducto, int idUser) {
        escribir("\"Nuevo producto en venta\";" + idProducto + ";" + idUser + ";" + ahora());
    }

    /** d) Venta cerrada: "Venta Cerrada";emailVendedor;emailComprador;fecha/hora */
    public static void logVentaCerrada(String emailVendedor, String emailComprador) {
        escribir("\"Venta Cerrada\";" + emailVendedor + ";" + emailComprador + ";" + ahora());
    }

    // -------------------------------------------------------------------------
    // Privados
    // -------------------------------------------------------------------------

    private static String ahora() {
        return SDF.format(new Date());
    }

    private static synchronized void escribir(String linea) {
        try {
            // Crear el directorio si no existe
            Path logFile = Paths.get(LOG_PATH);
            Files.createDirectories(logFile.getParent());

            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter(logFile.toFile(), true))) { // append = true
                bw.write(linea);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("[LOG ERROR] No se pudo escribir en el log: " + e.getMessage());
        }
    }
}
