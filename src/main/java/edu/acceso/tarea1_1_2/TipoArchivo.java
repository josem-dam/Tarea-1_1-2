package edu.acceso.tarea1_1_2;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;

/**
 * Tipo de archivo que puede ser un directorio, un archivo regular,
 * un enlace simbólico o cualquier otro tipo de archivo.
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

    TipoArchivo(Predicate<Path> esTipo) {
        this.esTipo = esTipo;
    }

    /**
     * Crea un tipo de archivo a partir de una ruta.
     * @param path La ruta del archivo.
     * @return El tipo de archivo correspondiente.  
     */
    public static TipoArchivo fromPath(Path path) {
        return Arrays.stream(values())
                .filter(tipo -> tipo.esTipo.test(path))
                .findFirst()
                .orElse(OTRO);
    }

    /**
     * Crea un tipo de archivo a partir de una cadena.
     * Si la cadena no corresponde a un tipo válido, retorna null.  
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

    @Override
    public String toString() {
        return this.name();
    }
}