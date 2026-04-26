package utils;

import modelos.Usuario;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class PersistenciaUtils {
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
    public static boolean guardar(String rutaFichero, ArrayList<Usuario> usuarios) {
        try {
            Files.createDirectories(Paths.get(rutaFichero).getParent());

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(new FileOutputStream(rutaFichero)))) {
                oos.writeObject(usuarios);
            }

            return true;

        } catch (IOException e) {
            System.err.println("[PERSISTENCIA] Error al guardar datos: " + e.getMessage());
            return false;
        }
    }
}
