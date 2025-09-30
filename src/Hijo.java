import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;


public class Hijo {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.out.println("Uso: java Hijo <fichero> <indiceInicio> <indiceFin> <ficheroSalida>");
            return;
        }

        String fichero = args[0];
        int inicio = Integer.parseInt(args[1]);
        int fin = Integer.parseInt(args[2]);
        String ficheroSalida = args[3];

        int totalPalabras = 0;
        int totalVocales = 0;

        try {
            List<String> lineas = Files.readAllLines(Paths.get(fichero));

            for (int i = inicio; i <= fin && i < lineas.size(); i++) {
                String linea = lineas.get(i).toLowerCase();
                if (linea.isBlank()) continue;

                String[] palabras = linea.split("\\s+");
                totalPalabras += palabras.length;

                for (String palabra : palabras) {
                    for (char c : palabra.toCharArray()) {
                        if ("aeiouáéíóú".indexOf(c) >= 0){
                            totalVocales++;
                        }
                    }
                }
            }

            try (PrintWriter pw = new PrintWriter(ficheroSalida)) {
                pw.println("vocales= "+totalVocales);
                pw.println("palabras= "+totalPalabras);
            }

        } catch (IOException e) {
          e.printStackTrace();
        }
    }
}
