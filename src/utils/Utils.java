package utils;

import java.util.Scanner;

// UTILIDAD: Métodos de apoyo para la interfaz de consola.
public class Utils {

    public static void limpiarPantalla() {
        for (int i = 0; i < 5; i++) System.out.println();
    }

    public static void pulsaContinuar(Scanner s) {
        System.out.print("Pulsa ENTER para continuar: ");
        s.nextLine();
        limpiarPantalla();
    }
}
