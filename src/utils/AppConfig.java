package utils;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

//Gestiona el fichero de configuración fernanpop.properties.
public class AppConfig {

    // Ruta relativa al directorio de ejecución
    private static final String CONFIG_PATH = "config" + File.separator + "fernanpop.properties";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    // Valores por defecto
    private static final String DEFAULT_DATA_FILE    = "data" + File.separator + "fernanpop.dat";
    private static final String DEFAULT_GUEST_ACCESS = "false";

    private static Properties props = new Properties();

    // -------------------------------------------------------------------------
    // Carga inicial
    // -------------------------------------------------------------------------

    //Carga el fichero de propiedades. Si no existe, crea uno con valores por defecto.

    public static void cargar() {
        Path configPath = Paths.get(CONFIG_PATH);
        if (Files.exists(configPath)) {
            try (InputStream is = new FileInputStream(configPath.toFile())) {
                props.load(is);
            } catch (IOException e) {
                System.err.println("[CONFIG] Error al cargar propiedades: " + e.getMessage());
                cargarDefaults();
            }
        } else {
            cargarDefaults();
            guardar();  // Crea el fichero con los valores por defecto
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /** Ruta del fichero de datos serializados. */
    public static String getRutaDatos() {
        return props.getProperty("data.file", DEFAULT_DATA_FILE);
    }

    /** ¿Está habilitado el acceso como invitado (sin login)? */
    public static boolean isAccesoInvitado() {
        return Boolean.parseBoolean(props.getProperty("acceso.invitado", DEFAULT_GUEST_ACCESS));
    }

    /**
     * Fecha/hora del último inicio de sesión de un usuario.
     * @return cadena con la fecha, o null si nunca ha iniciado sesión.
     */
    public static String getUltimaSesion(String email) {
        // La clave no puede contener puntos problemáticos; usamos @ y . tal cual en Properties
        String key = "ultima.sesion." + email.replace("@", "_at_").replace(".", "_");
        return props.getProperty(key);
    }

    // -------------------------------------------------------------------------
    // Setters (guardan automáticamente)
    // -------------------------------------------------------------------------

    /** Registra la fecha/hora de inicio de sesión del usuario y persiste. */
    public static void setUltimaSesion(String email) {
        String key = "ultima.sesion." + email.replace("@", "_at_").replace(".", "_");
        props.setProperty(key, SDF.format(new Date()));
        guardar();
    }

    /** Devuelve todas las propiedades (para el menú admin). */
    public static Properties getAllProps() {
        return props;
    }

    // -------------------------------------------------------------------------
    // Privados
    // -------------------------------------------------------------------------

    private static void cargarDefaults() {
        props.setProperty("data.file",       DEFAULT_DATA_FILE);
        props.setProperty("acceso.invitado", DEFAULT_GUEST_ACCESS);
    }

    public static void guardar() {
        try {
            Path configPath = Paths.get(CONFIG_PATH);
            Files.createDirectories(configPath.getParent());
            try (OutputStream os = new FileOutputStream(configPath.toFile())) {
                props.store(os, "Configuracion de Fernanpop");
            }
        } catch (IOException e) {
            System.err.println("[CONFIG] Error al guardar propiedades: " + e.getMessage());
        }
    }
}
