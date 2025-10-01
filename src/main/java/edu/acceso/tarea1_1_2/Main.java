package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.acceso.tarea1_1_2.archivos.FileInfo;
import edu.acceso.tarea1_1_2.archivos.TipoArchivo;

/**
 * Clase principal que inicia la aplicación.
 * Utiliza la configuración proporcionada por la clase Config
 * y lista los archivos según las opciones especificadas.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Punto de entrada de la aplicación.
     * Inicializa la configuración y lista los archivos según las opciones proporcionadas.
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        Config config = Config.create(args);

        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        rootLogger.setLevel(config.getLogLevel());


        try(Stream<FileInfo> archivos = obtenerArchivos()) {
            archivos.forEach(a -> System.out.println(generarLinea(a)));
        }
        catch (IOException e) {
            logger.error("Error al listar archivos", e);
            System.exit(1);
        }
    }

    /**
     * Obtiene el flujo de archivos según la configuración actual.
     * Filtra los archivos por tipo si se especificaron tipos en la configuración.
     * @return Flujo de objetos FileInfo que representan los archivos encontrados.
     * @throws IOException Si ocurre un error al acceder al directorio de búsqueda.
     */
    private static Stream<FileInfo> obtenerArchivos() throws IOException {
        Config config = Config.getInstance();
        return FileInfo.list(config.getDirectory(), config.getDepth())
           .limit(config.getLimit())
           .filter(fileinfo -> {
                // Filtramos por tipo de archivo si se especificó alguno
                if (config.getTipos() != null && config.getTipos().length > 0) {
                    TipoArchivo tipo = TipoArchivo.fromPath(fileinfo.getPath());
                    return Arrays.stream(config.getTipos()).anyMatch(tipo::equals);
                }
                return true; // No se especificaron tipos, incluir todos
            });
    }

    /**
     * Obtiene la representación legible de una cantidad de información
     * expresada en bytes. Por ejemplo, "1.23 MB", "456 KB", etc.
     * @param cantidad La cantidad de información expresada en bytes.
     * @return Una cadena que representa el tamaño en un formato legible.
     */
    private static String generarCantidadLegible(long cantidad) {
        String[] unidades = {"B", "KB", "MB", "GB"};
        double tamanno = cantidad;
        for(String unidad: unidades) {
            if(tamanno < 1024) {
                return String.format("%.2f %s", tamanno, unidad);
            }
            tamanno /= 1024;
        }
        return String.format("%.2f %s", tamanno, "TB");
    }

    private static String generarLinea(FileInfo fileInfo) {
        return String.format("%-12s  %12s  %-25s  %s", 
            fileInfo.getTipo(), 
            generarCantidadLegible(fileInfo.getTamanno()),
            fileInfo.getPropietario(), 
            fileInfo.getPath());
    }
}