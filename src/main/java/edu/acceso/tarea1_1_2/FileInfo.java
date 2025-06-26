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

    private Path path;
    private boolean isDirectory;
    private String propietario = "";
    private long tamanho = 0; // Tamaño en bytes

    public FileInfo(Path path) {
        this.path = path;
        this.isDirectory = Files.isDirectory(path);
        try {
            this.propietario = Files.getOwner(path).getName();
            this.tamanho = Files.size(path);
        } catch (IOException e) {
            logger.error("Error al obtener información del archivo: {}", path, e);
        }
    }

    public Path getPath() {
        return path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getPropietario() {
        return propietario;
    }

    public long getTamanho() {
        return tamanho;
    } 

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