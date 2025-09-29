package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal que inicia la aplicación.
 * Utiliza la configuración proporcionada por la clase Config
 * y lista los archivos según las opciones especificadas.
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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
     * Punto de entrada de la aplicación.
     * Inicializa la configuración y lista los archivos según las opciones proporcionadas.
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        @SuppressWarnings("unused")
        Config config = Config.create(args);

        try(Stream<FileInfo> archivos = obtenerArchivos()) {
            archivos.forEach(System.out::println);
        }
        catch (IOException e) {
            logger.error("Error al listar archivos", e);
            System.exit(1);
        }
    }
}