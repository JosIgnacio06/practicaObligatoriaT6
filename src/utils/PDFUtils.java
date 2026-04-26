package utils;

import modelos.Producto;
import modelos.Usuario;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Genera un PDF con el resumen de una venta usando iText (si disponible)
 * o un fallback en texto plano con extensión .pdf para entornos sin iText.
 *
 * NOTA: Para activar la generación real de PDF, añade itext-core al classpath.
 * El JAR de iText 5 (itextpdf-5.x.x.jar) es suficiente.
 * Si no está disponible, se genera un fichero de texto con los datos de la venta.
 */
public class PDFUtils {

    private static final String PDF_TEMP_DIR = "temp";
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    /**
     * Genera un fichero PDF (o texto) con el resumen de la venta y lo devuelve.
     * @return File apuntando al PDF generado, o null si hubo error.
     */
    public static File generarResumenVenta(Usuario vendedor, Usuario comprador, Producto producto, int idTrato) {
        try {
            Files.createDirectories(Paths.get(PDF_TEMP_DIR));
            String nombreFichero = PDF_TEMP_DIR + File.separator + "resumen_venta_" + idTrato + ".pdf";
            File fichero = new File(nombreFichero);

            // Intentamos con iText; si no está, usamos texto plano con cabecera PDF mínima
            try {
                return generarConIText(fichero, vendedor, comprador, producto, idTrato);
            } catch (NoClassDefFoundError | ClassNotFoundException e) {
                return generarTextoPlano(fichero, vendedor, comprador, producto, idTrato);
            }
        } catch (IOException e) {
            System.err.println("[PDF] Error al generar el PDF: " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Generación con iText 5
    // -------------------------------------------------------------------------
    private static File generarConIText(File fichero, Usuario vendedor, Usuario comprador,
                                        Producto producto, int idTrato)
            throws ClassNotFoundException, IOException {

        // Cargamos dinámicamente para que no falle en compilación si iText no está
        try {
            Class<?> docClass      = Class.forName("com.itextpdf.text.Document");
            Class<?> writerClass   = Class.forName("com.itextpdf.text.pdf.PdfWriter");
            Class<?> paraClass     = Class.forName("com.itextpdf.text.Paragraph");
            Class<?> fontClass     = Class.forName("com.itextpdf.text.FontFactory");

            Object document = docClass.getDeclaredConstructor().newInstance();
            try (FileOutputStream fos = new FileOutputStream(fichero)) {
                writerClass.getMethod("getInstance", docClass, OutputStream.class)
                           .invoke(null, document, fos);
                docClass.getMethod("open").invoke(document);

                String contenido = buildContenido(vendedor, comprador, producto, idTrato);
                Object parrafo = paraClass.getDeclaredConstructor(String.class).newInstance(contenido);
                docClass.getMethod("add", Class.forName("com.itextpdf.text.Element"))
                        .invoke(document, parrafo);
                docClass.getMethod("close").invoke(document);
            }
            return fichero;
        } catch (Exception e) {
            throw new ClassNotFoundException("iText no disponible");
        }
    }

    // -------------------------------------------------------------------------
    // Fallback: texto plano guardado como .pdf (legible con cualquier editor)
    // -------------------------------------------------------------------------
    private static File generarTextoPlano(File fichero, Usuario vendedor, Usuario comprador,
                                          Producto producto, int idTrato) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fichero))) {
            bw.write(buildContenido(vendedor, comprador, producto, idTrato));
        }
        return fichero;
    }

    private static String buildContenido(Usuario vendedor, Usuario comprador,
                                         Producto producto, int idTrato) {
        return """
                ========================================
                   FERNANPOP - RESUMEN DE VENTA
                ========================================
                
                ID Trato:     %d
                Fecha:        %s
                
                --- PRODUCTO ---
                Título:       %s
                Descripción:  %s
                Estado:       %s
                Precio:       %.2f €
                
                --- VENDEDOR ---
                Nombre:       %s %s
                Email:        %s
                
                --- COMPRADOR ---
                Nombre:       %s %s
                Email:        %s
                
                ========================================
                Gracias por usar Fernanpop.
                ========================================
                """.formatted(
                idTrato, SDF.format(new Date()),
                producto.getTitulo(), producto.getDescripcion(), producto.getEstado(), producto.getPrecio(),
                vendedor.getNombre(), vendedor.getApel(), vendedor.getEmail(),
                comprador.getNombre(), comprador.getApel(), comprador.getEmail()
        );
    }
}
