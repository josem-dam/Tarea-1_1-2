package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase que representa la información de un archivo o directorio.
 * Esta clase puede ser extendida para incluir más detalles como tamaño, fecha de modificación, etc
 */
public class FileInfo {
    private static final Logger logger = LoggerFactory.getLogger(FileInfo.class);

    /**
     * Ruta del archivo o directorio.
     * Esta ruta es relativa al directorio de trabajo actual.
     */
    private Path path;
    /** Tipo de archivo (directorio, archivo regular, etc.)  */
    private TipoArchivo tipo;
    /** Propietario del archivo o directorio.  */
    private String propietario = "";
    /** Tamaño del archivo o directorio. */
    private long tamanho = 0;

    public FileInfo(Path path) {
        this.path = path;
        this.tipo = TipoArchivo.fromPath(path);
        try {
            this.propietario = Files.getOwner(path).getName();
            this.tamanho = Files.size(path);
        } catch (IOException e) {
            logger.error("Error al obtener información del archivo: {}", path, e);
        }
    }

    /**
     * Obtiene la ruta del archivo o directorio.
     * Esta ruta es relativa al directorio de trabajo actual.
     * @return La ruta del archivo o directorio.
     */
    public Path getPath() {
        return path;
    }

    /**
     * Obtiene el tipo de archivo.
     * @return El tipo de archivo (directorio, archivo regular, etc.).
     */
    public TipoArchivo getTipo() {
        return tipo;
    }

    /**
     * Obtiene el propietario del archivo o directorio.
     * @return El propietario del archivo o directorio.
     */
    public String getPropietario() {
        return propietario;
    }

    /**
     * Obtiene el tamaño del archivo o directorio.
     * @return El tamaño del archivo o directorio.
     */
    public long getTamanho() {
        return tamanho;
    } 

    /**
     * Obtiene una representación legible del tamaño del archivo o directorio.
     * Por ejemplo, "1.23 MB", "456 KB", etc.
     * @return Una cadena que representa el tamaño en un formato legible.
     */
    public String getTamanhoLegible() {
        if (tamanho < 1024) {
            return tamanho + " B";
        } else if (tamanho < 1024 * 1024) {
            return String.format("%.2f KB", tamanho / 1024.0);
        } else if (tamanho < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", tamanho / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", tamanho / (1024.0 * 1024 * 1024));
        }
    }
}