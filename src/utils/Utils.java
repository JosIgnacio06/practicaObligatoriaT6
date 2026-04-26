package utils;

import java.util.Scanner;

public class Utils {
    private static void limpiarPantalla(){
        for (int i = 0; i < 5; i++) {
            System.out.println();
        }
    }
    public static void pulsaContinuar(Scanner s) {
        System.out.print("Pulsa ENTER para continuar: ");
        s.nextLine();
        limpiarPantalla();
    }
}