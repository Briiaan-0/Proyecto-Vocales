import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;

/**
 * Clase Hijo.
 * <p>
 * Procesa un rango de líneas de un fichero, convierte todas las palabras
 * a minúsculas y normaliza los acentos, cuenta las vocales y escribe los
 * resultados en un fichero de salida.
 * </p>
 * <p>
 * Los argumentos esperados son:
 * java Hijo <fichero> <indiceInicio> <indiceFin> <ficheroSalida>
 * </p>
 *
 * @author Brian Giraldo
 * @version 1.0
 */

public class Hijo {

    /**
     * Método principal de la clase Hijo.
     * Lee las líneas indicadas del fichero, procesa las palabras y vocales
     * y escribe los resultados en el fichero de salida.
     *
     * @param args Array de argumentos de la línea de comandos. Debe contener:
     *             args[0] -> fichero de entrada
     *             args[1] -> índice de inicio
     *             args[2] -> índice de fin
     *             args[3] -> fichero de salida
     */

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Uso: java Hijo <fichero> <indiceInicio> <indiceFin> <ficheroSalida>");
            System.exit(1);
        }

        String fichero = args[0];
        int inicio = Integer.parseInt(args[1]);
        int fin = Integer.parseInt(args[2]);
        String ficheroSalida = args[3];

        if (inicio < 0) inicio = 0;

        int totalVocales = 0;
        StringBuilder todasPalabras = new StringBuilder();

        try {
            List<String> lineas = Files.readAllLines(Paths.get(fichero), StandardCharsets.UTF_8);

            for (int i = inicio; i <= fin && i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) continue;

                String[] palabras = linea.split("\\s+");
                for (String palabra : palabras) {
                    if (palabra.isEmpty()) continue;

                    // convertir a minúscula y normalizar
                    String normalized = Normalizer.normalize(palabra.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
                    normalized = normalized.replaceAll("\\p{M}", "");

                    // agregar palabra al StringBuilder
                    if (todasPalabras.length() > 0) {
                        todasPalabras.append(" ");
                    }
                    todasPalabras.append(normalized);

                    // contar vocales
                    for (char c : normalized.toCharArray()) {
                        if ("aeiou".indexOf(c) >= 0) totalVocales++;
                    }
                }
            }

            // escribir resultado
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(ficheroSalida), StandardCharsets.UTF_8)) {
                bw.write("palabras=" + todasPalabras.toString());
                bw.newLine();
                bw.write("vocales=" + totalVocales);
                bw.newLine();
            }

        } catch (IOException e) {
            System.err.println("Error I/O en hijo: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }
}
