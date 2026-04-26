package utils;

import modelos.Producto;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

//Genera un fichero Excel (CSV compatible con Excel) con el listado de productos.
public class ExcelUtils {

    private static final String TEMP_DIR = "temp";
    public static File generarListadoProductos(ArrayList<Producto> productos) {
        try {
            Files.createDirectories(Paths.get(TEMP_DIR));
            // Intentamos con Apache POI; si no está, CSV
            try {
                return generarConPOI(productos);
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                return generarCSV(productos);
            }
        } catch (IOException e) {
            System.err.println("[EXCEL] Error al generar el fichero: " + e.getMessage());
            return null;
        }
    }
    private static File generarCSV(ArrayList<Producto> productos) throws IOException {
        File fichero = new File(TEMP_DIR + File.separator + "productos_fernanpop.csv");
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(fichero), "UTF-8"))) {

            // BOM para que Excel abra en UTF-8
            bw.write('\uFEFF');
            bw.write("ID;Titulo;Descripcion;Precio (EUR);Estado");
            bw.newLine();

            for (Producto p : productos) {
                bw.write(String.format("%d;%s;%s;%.2f;%s",
                        p.getId(),
                        escaparCSV(p.getTitulo()),
                        escaparCSV(p.getDescripcion()),
                        p.getPrecio(),
                        p.getEstado()));
                bw.newLine();
            }
        }
        return fichero;
    }

    private static String escaparCSV(String valor) {
        if (valor == null) return "";
        // Envuelve en comillas si contiene punto y coma, comillas o saltos de línea
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    // -------------------------------------------------------------------------
    // Apache POI (si está disponible)
    // -------------------------------------------------------------------------
    private static File generarConPOI(ArrayList<Producto> productos)
            throws ClassNotFoundException, IOException {

        // Verificamos que POI esté en el classpath
        Class.forName("org.apache.poi.ss.usermodel.Workbook");

        try {
            Class<?> wbClass   = Class.forName("org.apache.poi.xssf.usermodel.XSSFWorkbook");
            Object   wb        = wbClass.getDeclaredConstructor().newInstance();
            Object   sheet     = wbClass.getMethod("createSheet", String.class)
                                        .invoke(wb, "Productos");

            // Cabecera
            String[] cabecera = {"ID", "Titulo", "Descripcion", "Precio (EUR)", "Estado"};
            crearFilaPOI(sheet, 0, cabecera);

            // Datos
            for (int i = 0; i < productos.size(); i++) {
                Producto p = productos.get(i);
                String[] fila = {
                        String.valueOf(p.getId()),
                        p.getTitulo(),
                        p.getDescripcion(),
                        String.format("%.2f", p.getPrecio()),
                        p.getEstado()
                };
                crearFilaPOI(sheet, i + 1, fila);
            }

            File fichero = new File(TEMP_DIR + File.separator + "productos_fernanpop.xlsx");
            try (FileOutputStream fos = new FileOutputStream(fichero)) {
                wbClass.getMethod("write", OutputStream.class).invoke(wb, fos);
            }
            wbClass.getMethod("close").invoke(wb);
            return fichero;

        } catch (Exception e) {
            throw new ClassNotFoundException("POI no disponible");
        }
    }

    private static void crearFilaPOI(Object sheet, int rowNum, String[] valores) throws Exception {
        Class<?> sheetClass = sheet.getClass();
        Object row = sheetClass.getMethod("createRow", int.class).invoke(sheet, rowNum);
        Class<?> rowClass = row.getClass();
        for (int i = 0; i < valores.length; i++) {
            Object cell = rowClass.getMethod("createCell", int.class).invoke(row, i);
            cell.getClass().getMethod("setCellValue", String.class).invoke(cell, valores[i]);
        }
    }
}
