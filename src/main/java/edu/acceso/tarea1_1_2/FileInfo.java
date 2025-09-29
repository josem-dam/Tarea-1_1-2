package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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

    /**
     * Captura la excepciones que genera el Stream para imprimer el error
     * y continuar con el siguiente elemento.
     * @param <T> Tipo de elemento en el Stream
     * @param stream Flujo de elementos a procesar
     * @return El mismo flujo pero protegido contra excepciones (los elementos que
     *         generen excepciones serán omitidos y se registrará el error)
     */
    private static <T> Stream<T> safeStream(Stream<T> stream) {
        Iterator<T> iterator = stream.iterator();
        Iterable<T> iterable = () -> new Iterator<>() {
            @Override
            public boolean hasNext() {
                while(true) {
                    try {
                        return iterator.hasNext();
                    }
                    catch(Exception e) {
                        logger.error("Inaccesible: {}", e.getMessage());
                    }
                }
            }

            @Override
            public T next() {
                while(true) {
                    try {
                        return iterator.next();
                    }
                    catch(Exception e) {
                        logger.error("Inaccesible: {}", e.getMessage());
                    }
                }
            }
        };

        return StreamSupport.stream(iterable.spliterator(), false)
            .onClose(stream::close);
    }

    /**
     * Obtiene propiamente la lista de archivos del directorio requerido
     * según las indicaciones y filtros definidos por el usuario.
     * 
     * @param directory Directorio donde se realizará la obtención de archivos.
     * @param depth Profundidad máxima de búsqueda.
     * @return El flujo de archivos encontrados que cumplen con los filtros.
     * @throws IOException Si ocurre un error al interactuar con el sistema de archivos.
     */
    public static Stream<FileInfo> list(Path directory, int depth) throws IOException {
        // Protegemos Files.walk con safeStream para evitar excepciones.
        // cuando se intenta acceder a archivos o directorios sin permisos.
       return safeStream(Files.walk(directory, depth, FileVisitOption.FOLLOW_LINKS))
           .map(FileInfo::new);
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