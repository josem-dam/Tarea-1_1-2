package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

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
    private long tamanno = 0;

    /**
     * Fecha y hora de la última modificación del archivo o directorio.
     */
    private LocalDateTime modificacion;

    public FileInfo(Path path) {
        this.path = path;
        this.tipo = TipoArchivo.fromPath(path);
        try {
            this.propietario = Files.getOwner(path).getName();
        } catch (IOException e) {
            logger.error("Error al obtener el propietario del archivo: {}", path, e);
            this.propietario = "???";
        }
        try {
            this.tamanno = Files.size(path);
        } catch (IOException e) {
            logger.error("Error al obtener el tamaño del archivo: {}", path, e);
            this.tamanno = 0;
        }
        this.modificacion = obtenerFechaModificacion(path);
    }

    private LocalDateTime obtenerFechaModificacion(Path path) {
        try {
            FileTime fileTime = Files.getLastModifiedTime(path);
            return LocalDateTime.ofInstant(fileTime.toInstant(), ZoneId.systemDefault());
        } catch (IOException e) {
            logger.error("Error al obtener la fecha de modificación del archivo: {}", path, e);
            return null;
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
    public long getTamanno() {
        return tamanno;
    } 

    /**
     * Obtiene una representación legible del tamaño del archivo o directorio.
     * Por ejemplo, "1.23 MB", "456 KB", etc.
     * @return Una cadena que representa el tamaño en un formato legible.
     */
    public String getTamannoLegible() {
        String[] unidades = {"B", "KB", "MB", "GB"};
        double tamanno = this.tamanno;
        for(String unidad: unidades) {
            if(tamanno < 1024) {
                return String.format("%.2f %s", tamanno, unidad);
            }
            tamanno /= 1024;
        }
        return String.format("%.2f %s", tamanno, "TB");
    }

    public LocalDateTime getModificacion() {
        return modificacion;
    }

    @Override
    public String toString() {
        return String.format("%s\t%s\t%s\t%s", 
            getPath(), 
            getTipo(), 
            getPropietario(), 
            getTamannoLegible());
    }
}