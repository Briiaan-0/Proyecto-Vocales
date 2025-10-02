import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Padre {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Padre <fichero> [numHijos]");
            return;
        }

        String fichero = args[0];
        int numHijos = 4;
        if (args.length >= 2) {
            try {
                numHijos = Integer.parseInt(args[1]);
                if (numHijos <= 0) {
                    System.out.println("numHijos debe ser > 0. Usando 4.");
                    numHijos = 4;
                }
            } catch (NumberFormatException ex) {
                System.out.println("numHijos no es un entero. Usando 4.");
                numHijos = 4;
            }
        }

        try {
            List<String> lineas = Files.readAllLines(Paths.get(fichero), StandardCharsets.UTF_8);
            int totalLineas = lineas.size();
            System.out.printf("Fichero '%s' -> %d líneas. Lanzando %d hijos.%n", fichero, totalLineas, numHijos);

            // reparto equilibrado
            int base = totalLineas / numHijos;
            int rem = totalLineas % numHijos;

            List<Process> procesos = new ArrayList<>();
            List<String> ficherosRes = new ArrayList<>();

            // ruta al ejecutable java actual y classpath
            String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
            String classpath = System.getProperty("java.class.path");

            for (int i = 0; i < numHijos; i++) {
                int inicio = i * base + Math.min(i, rem);
                int tam = base + (i < rem ? 1 : 0);
                int fin = inicio + tam - 1; // si tam == 0, fin = inicio - 1

                String out = String.format("hijo%d.res", i + 1);
                ficherosRes.add(out);

                List<String> cmd = Arrays.asList(
                        javaBin, "-cp", classpath, "Hijo",
                        fichero,
                        String.valueOf(inicio),
                        String.valueOf(fin),
                        out
                );

                ProcessBuilder pb = new ProcessBuilder(cmd);
                pb.redirectErrorStream(true); // une stderr a stdout del proceso hijo
                // opcional: pb.directory(new File(".")); // establece working dir si hace falta

                try {
                    Process p = pb.start();
                    procesos.add(p);
                    System.out.printf("Lanzado hijo %d: líneas %d..%d -> %s%n", i + 1, inicio, fin, out);
                } catch (IOException e) {
                    System.err.printf("Error al lanzar hijo %d: %s%n", i + 1, e.getMessage());
                    procesos.add(null);
                }
            }

            // Esperar a que finalicen todos (paralelamente)
            for (int i = 0; i < procesos.size(); i++) {
                Process p = procesos.get(i);
                if (p == null) continue;
                try {
                    int exit = p.waitFor();
                    System.out.printf("Hijo %d finalizado con código %d%n", i + 1, exit);
                    // opcional: leer stdout del hijo si quieres el log
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            // comentar o descomentar si quieres ver la salida de cada hijo
                            // System.out.printf("Hijo%d> %s%n", i+1, line);
                        }
                    } catch (IOException ignored) {}
                } catch (InterruptedException e) {
                    System.err.println("Esperando hijo interrumpido");
                    Thread.currentThread().interrupt();
                }
            }

            // Leer .res y sumar
            int totalPalabras = 0;
            int totalVocales = 0;

            System.out.println("=== Resultados parciales ===");
            for (int i = 0; i < ficherosRes.size(); i++) {
                Path pRes = Paths.get(ficherosRes.get(i));
                int palabras = 0;
                int vocales = 0;

                if (Files.exists(pRes)) {
                    List<String> outLines = Files.readAllLines(pRes, StandardCharsets.UTF_8);
                    for (String l : outLines) {
                        l = l.trim();
                        if (l.startsWith("palabras=")) {
                            try { palabras = Integer.parseInt(l.substring("palabras=".length()).trim()); }
                            catch (NumberFormatException ignored) {}
                        } else if (l.startsWith("vocales=")) {
                            try { vocales = Integer.parseInt(l.substring("vocales=".length()).trim()); }
                            catch (NumberFormatException ignored) {}
                        }
                    }
                } else {
                    System.err.printf("ATENCIÓN: fichero %s no encontrado para hijo %d%n", pRes.toString(), i + 1);
                }

                System.out.printf("Hijo %d -> palabras=%d, vocales=%d%n", i + 1, palabras, vocales);
                totalPalabras += palabras;
                totalVocales += vocales;
            }

            double promedio = (totalPalabras == 0) ? 0.0 : ((double) totalVocales / totalPalabras);

            System.out.println("=== Informe final ===");
            System.out.println("Total palabras: " + totalPalabras);
            System.out.println("Total vocales: " + totalVocales);
            System.out.printf("Promedio vocales por palabra: %.4f%n", promedio);

        } catch (IOException e) {
            System.err.println("Error I/O en padre: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
