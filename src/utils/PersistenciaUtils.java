package utils;

import modelos.Usuario;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

/**
 * Gestiona la persistencia de los datos de la aplicación mediante serialización Java.
 *
 * La ubicación del fichero de datos se lee del fichero de propiedades (AppConfig).
 * Si no existe el fichero, arranca con lista vacía y la crea al primer guardado.
 *
 * Criterios de diseño:
 *  - Solo se serializan los objetos Usuario (que contienen Producto y Trato).
 *  - NO se serializa el controlador completo ni estructuras de datos auxiliares.
 *  - El guardado es atómico: se escribe en un fichero temporal y luego se renombra,
 *    para evitar corrupción de datos si la app se cierra a mitad de escritura.
 */
public class PersistenciaUtils {

    /**
     * Carga la lista de usuarios desde disco.
     * @param rutaFichero ruta relativa o absoluta al fichero .dat
     * @return lista de usuarios cargada, o lista vacía si no existe o hay error.
     */
    @SuppressWarnings("unchecked")
    public static ArrayList<Usuario> cargar(String rutaFichero) {
        Path path = Paths.get(rutaFichero);
        if (!Files.exists(path)) {
            return new ArrayList<>(); // Primera ejecución: sin datos previos
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(path.toFile())))) {
            Object obj = ois.readObject();
            if (obj instanceof ArrayList) {
                return (ArrayList<Usuario>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[PERSISTENCIA] Error al cargar datos: " + e.getMessage());
            System.err.println("              Se arrancará con datos vacíos.");
        }
        return new ArrayList<>();
    }

    /**
     * Guarda la lista de usuarios en disco de forma atómica.
     * Escribe en un fichero temporal y luego lo mueve (renombra) al destino final.
     * @param rutaFichero ruta relativa o absoluta al fichero .dat
     * @param usuarios    lista de usuarios a guardar
     * @return true si se guardó correctamente.
     */
    public static boolean guardar(String rutaFichero, ArrayList<Usuario> usuarios) {
        Path destino = Paths.get(rutaFichero);
        Path temporal = Paths.get(rutaFichero + ".tmp");

        try {
            // Crear el directorio si no existe
            Files.createDirectories(destino.getParent());

            // Escribir en fichero temporal primero
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(temporal.toFile())))) {
                oos.writeObject(usuarios);
            }

            // Mover atómicamente el temporal al destino (atomicMove puede no estar disponible
            // en todos los FS, así que usamos REPLACE_EXISTING como fallback)
            try {
                Files.move(temporal, destino,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;

        } catch (IOException e) {
            System.err.println("[PERSISTENCIA] Error al guardar datos: " + e.getMessage());
            // Intentar limpiar el temporal si quedó huérfano
            try { Files.deleteIfExists(temporal); } catch (IOException ignored) {}
            return false;
        }
    }
}
