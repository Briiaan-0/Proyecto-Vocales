import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;

public class Hijo {
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

        int totalPalabras = 0;
        int totalVocales = 0;

        try {
            List<String> lineas = Files.readAllLines(Paths.get(fichero), StandardCharsets.UTF_8);

            for (int i = inicio; i <= fin && i < lineas.size(); i++) {
                String linea = lineas.get(i).trim();
                if (linea.isEmpty()) continue;

                // Normalizamos solo a nivel de palabra, y contamos tokens por espacios
                String[] palabras = linea.split("\\s+");
                for (String palabra : palabras) {
                    if (palabra.isEmpty()) continue;
                    totalPalabras++;

                    // normalizar y eliminar marcas diacríticas para contar la 'base' (ü -> u, á -> a)
                    String normalized = Normalizer.normalize(palabra.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
                    normalized = normalized.replaceAll("\\p{M}", ""); // elimina marcas diacríticas

                    for (char c : normalized.toCharArray()) {
                        if ("aeiou".indexOf(c) >= 0) totalVocales++;
                    }
                }
            }

            // Escribir resultado
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(ficheroSalida), StandardCharsets.UTF_8)) {
                bw.write("palabras=" + totalPalabras);
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
