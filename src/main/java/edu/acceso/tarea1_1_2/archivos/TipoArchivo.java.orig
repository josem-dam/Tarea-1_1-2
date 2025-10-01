package edu.acceso.tarea1_1_2.archivos;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Tipo de archivo que puede ser un directorio, un archivo regular,
 * un enlace simbólico o cualquier otro tipo de archivo. Sirve de enum
 * auxiliar de {@link FileInfo}.
 */
public enum TipoArchivo {
    /** Representa un directorio */
    DIRECTORIO(Files::isDirectory),
    /** Representa un archivo regular */
    ARCHIVO(Files::isRegularFile),
    /** Representa un enlace simbólico */
    SIMBOLICO(Files::isSymbolicLink),
    /** Representa cualquier otro tipo de archivo */
    OTRO(path -> true);

    /**
     * Predicate que determina si una ruta corresponde a este tipo de archivo.
     */
    private final Predicate<Path> esTipo;

    /**
     * Constructor del enum.
     * @param esTipo Predicado que determina si una ruta corresponde a este tipo de archivo.
     */
    TipoArchivo(Predicate<Path> esTipo) {
        this.esTipo = esTipo;
    }

    /**
     * Obtiene el tipo del archivo proporcionado.
     * @param path La ruta del archivo.
     * @return El tipo de archivo.
     */
    public static TipoArchivo fromPath(Path path) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.esTipo.test(path))
                .findFirst()
                .orElseGet(() -> {
                    assert false: "Nunca debería llegar aquí. ¿El predicado de OTRO devuelve siempre verdadero?";
                    return OTRO;
                });
    }

    /**
     * Obtiene el tipo de archivo a partir de una cadena. Por ejemplo,
     * de "directorio" obtendrá {@link TipoArchivo#DIRECTORIO}.
     * Si la cadena no corresponde a un tipo válido, devuelve null.  
     * @param tipo  La cadena que representa el tipo de archivo.
     * @return  El tipo de archivo correspondiente o null si no es válido.
     */
    public static TipoArchivo fromString(String tipo) {
        try {
            return TipoArchivo.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}