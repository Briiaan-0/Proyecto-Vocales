import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Clase Padre.
 * <p>
 * Esta clase lee uno o varios ficheros de texto, lanza procesos Hijo para
 * procesar cada línea del fichero en paralelo, y recopila los resultados
 * parciales para generar un informe final de palabras y vocales.
 * </p>
 * <p>
 * Los argumentos esperados son:
 * java Padre <fichero1> [fichero2 ...] [numHijos]
 * </p>
 *
 * @author Brian Giraldo
 * @version 1.0
 */

public class Padre {

    /**
     * Método principal de la clase Padre.
     * Lee los argumentos, valida el número de hijos y procesa cada fichero.
     *
     * @param args Array de argumentos de la línea de comandos. Puede contener
     *             varios ficheros y opcionalmente el número de hijos al final.
     */

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Uso: java Padre <fichero1> [fichero2 ...] [numHijos]");
            return;
        }

        List<String> ficheros = new ArrayList<>();
        int numHijos = 4; // valor por defecto

        // Separar ficheros de numHijos
        for (String arg : args) {
            try {
                numHijos = Integer.parseInt(arg);
            } catch (NumberFormatException e) {
                ficheros.add(arg);
            }
        }

        if (ficheros.isEmpty()) {
            System.out.println("No se proporcionaron ficheros de entrada.");
            return;
        }

        // Ruta a java y classpath actual
        String javaBin = Paths.get(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("java.class.path");

        for (String fichero : ficheros) {
            System.out.println("\n=== Procesando fichero: " + fichero + " ===");
            try {
                List<String> lineas = Files.readAllLines(Paths.get(fichero), StandardCharsets.UTF_8);
                int totalLineas = lineas.size();
                System.out.printf("Fichero '%s' -> %d líneas. Lanzando %d hijos.%n", fichero, totalLineas, numHijos);

                // reparto equilibrado
                int base = (numHijos > 0) ? totalLineas / numHijos : 0;
                int rem = (numHijos > 0) ? totalLineas % numHijos : 0;

                List<Process> procesos = new ArrayList<>();
                List<String> ficherosRes = new ArrayList<>();

                // baseName para nombres de salida únicos
                String baseName = Paths.get(fichero).getFileName().toString().replaceAll("\\.txt$", "");

                for (int i = 0; i < numHijos; i++) {
                    int inicio = i * base + Math.min(i, rem);
                    int tam = base + (i < rem ? 1 : 0);
                    int fin = inicio + tam - 1;

                    String out = String.format("%s_hijo%d.res", baseName, i + 1);
                    ficherosRes.add(out);

                    List<String> cmd = Arrays.asList(
                            javaBin, "-cp", classpath, "Hijo",
                            fichero,
                            String.valueOf(inicio),
                            String.valueOf(fin),
                            out
                    );

                    ProcessBuilder pb = new ProcessBuilder(cmd);
                    pb.redirectErrorStream(true);

                    try {
                        Process p = pb.start();
                        procesos.add(p);
                        System.out.printf("Lanzado hijo %d: líneas %d..%d -> %s%n", i + 1, inicio, fin, out);
                    } catch (IOException e) {
                        System.err.printf("Error al lanzar hijo %d: %s%n", i + 1, e.getMessage());
                        procesos.add(null);
                    }
                }

                // Esperar a los hijos
                for (int i = 0; i < procesos.size(); i++) {
                    Process p = procesos.get(i);
                    if (p == null) continue;
                    try {
                        int exit = p.waitFor();
                        System.out.printf("Hijo %d finalizado con código %d%n", i + 1, exit);
                    } catch (InterruptedException e) {
                        System.err.println("Esperando hijo interrumpido");
                        Thread.currentThread().interrupt();
                    }
                }

                // Leer .res y sumar resultados
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
                                String texto = l.substring("palabras=".length()).trim();
                                if (!texto.isEmpty()) {
                                    palabras = texto.split("\\s+").length; // contar palabras por espacios
                                } else {
                                    palabras = 0;
                                }
                            }
                            else if (l.startsWith("vocales=")) {
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
}
