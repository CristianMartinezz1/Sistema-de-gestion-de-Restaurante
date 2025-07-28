/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Ventana;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import java.io.File;
import java.io.IOException;

public class GeneradorPDF {
   public static File generarPdfRegistro(String nombre, String correo, String usuario, String contrasena) throws IOException {
    try (PDDocument document = new PDDocument()) {
        PDPage pagina = new PDPage(PDRectangle.LETTER);
        document.addPage(pagina);

        PDPageContentStream contenido = new PDPageContentStream(document, pagina);

        float pageWidth = pagina.getMediaBox().getWidth();
        float margenIzquierdo = 70;
        float margenSuperior = 720;
        float interlineado = 22;
        float anchoContenido = pageWidth - 2 * margenIzquierdo;

        // 🎨 Fondo del contenido
        contenido.setNonStrokingColor(240, 248, 255); // Azul muy claro
        contenido.addRect(margenIzquierdo - 10, 120, anchoContenido + 20, 550);
        contenido.fill();
        contenido.setNonStrokingColor(0, 0, 0); // Restablece color a negro

        // 📌 Título centrado
        String titulo = " Bienvenido a Nuestro Sistema";
        float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(titulo) / 1000 * 22;
        float titleX = (pageWidth - titleWidth) / 2;
        float titleY = margenSuperior;

        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA_BOLD, 22);
        contenido.newLineAtOffset(titleX, titleY);
        contenido.showText(titulo);
        contenido.endText();

        // 🔵 Línea decorativa
        contenido.setStrokingColor(100, 149, 237); // Cornflower blue
        contenido.setLineWidth(2.5f);
        contenido.moveTo(margenIzquierdo, titleY - 8);
        contenido.lineTo(pageWidth - margenIzquierdo, titleY - 8);
        contenido.stroke();

        float y = titleY - 40;

        // 👋 Mensaje de saludo
        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA, 14);
        contenido.newLineAtOffset(margenIzquierdo, y);
        contenido.showText("Hola " + nombre + ",");
        contenido.endText();

        y -= interlineado;

        // ✉️ Texto de agradecimiento completo
        String agradecimiento = "Queremos darte una cálida bienvenida a nuestro equipo. "
                + "Tu registro ha sido exitoso y ahora formas parte de una comunidad comprometida con la excelencia, "
                + "el servicio y la colaboración. Estamos emocionados de trabajar contigo y esperamos que esta experiencia "
                + "sea enriquecedora tanto personal como profesionalmente.";

        // Divide el párrafo en líneas
        contenido.setFont(PDType1Font.HELVETICA, 12);
        int maxCharsPerLine = 90;
        for (String linea : dividirTexto(agradecimiento, maxCharsPerLine)) {
            y -= interlineado;
            contenido.beginText();
            contenido.newLineAtOffset(margenIzquierdo, y);
            contenido.showText(linea);
            contenido.endText();
        }

        y -= interlineado * 1.5;

        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA_BOLD, 13);
        contenido.newLineAtOffset(margenIzquierdo, y);
        contenido.showText("Tus credenciales de acceso son las siguientes:");
        contenido.endText();

        y -= interlineado * 1.3;

        float indentado = margenIzquierdo + 25;

        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA, 12);
        contenido.newLineAtOffset(indentado, y);
        contenido.showText(" Usuario: " + usuario);
        contenido.endText();

        y -= interlineado;

        contenido.beginText();
        contenido.newLineAtOffset(indentado, y);
        contenido.showText(" Correo: " + correo);
        contenido.endText();

        y -= interlineado;

        contenido.beginText();
        contenido.newLineAtOffset(indentado, y);
        contenido.showText(" Contraseña: " + contrasena);
        contenido.endText();

        y -= interlineado * 2;

        // ✅ Despedida
        String footer = "Gracias por confiar en nosotros. Estamos aquí para apoyarte en cada paso del camino.";
        float footerWidth = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(footer) / 1000 * 12;
        float footerX = (pageWidth - footerWidth) / 2;

        contenido.beginText();
        contenido.setFont(PDType1Font.HELVETICA_OBLIQUE, 12);
        contenido.newLineAtOffset(footerX, 80);
        contenido.showText(footer);
        contenido.endText();

        contenido.close();

        File archivoPdf = new File("registro_" + usuario + ".pdf");
        document.save(archivoPdf);
        return archivoPdf;
    }
}

/**
 * Divide texto largo en varias líneas respetando el límite de caracteres.
 */
private static java.util.List<String> dividirTexto(String texto, int maxPorLinea) {
    java.util.List<String> lineas = new java.util.ArrayList<>();
    String[] palabras = texto.split(" ");
    StringBuilder linea = new StringBuilder();
    for (String palabra : palabras) {
        if (linea.length() + palabra.length() + 1 > maxPorLinea) {
            lineas.add(linea.toString());
            linea = new StringBuilder();
        }
        linea.append(palabra).append(" ");
    }
    if (!linea.toString().isEmpty()) {
        lineas.add(linea.toString().trim());
    }
    return lineas;
}
     
    

}
