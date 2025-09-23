package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    /**
     * Captura la excepciones que genera el Stream para imprimer el error
     * y continuar con el siguiente elemento.
     * @param <T> Tipo de elemento en el Stream
     * @param stream Flujo de elementos a procesar
     * @return El mismo flujo pero protegido contra excepciones (los elementos que
     *         generen excepciones serán omitidos y se registrará el error)
     */
    public static <T> Stream<T> safeStream(Stream<T> stream) {
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
     * @return El flujo de archivos encontrados que cumplen con los filtros.
     * @throws IOException Si ocurre un error al interactuar con el sistema de archivos.
     */
    public static Stream<Path> obtenerArchivos() throws IOException {
       Config config = Config.getInstance();
        // Protegemos Files.walk con safeStream para evitar excepciones.
        // cuando se intenta acceder a archivos o directorios sin permisos.
       return safeStream(Files.walk(config.getDirectory(), config.getDepth(), FileVisitOption.FOLLOW_LINKS))
           .limit(config.getLimit())
           .filter(path -> {
                // Filtramos por tipo de archivo si se especificó alguno
                if (config.getTipos() != null && config.getTipos().length > 0) {
                    TipoArchivo tipo = TipoArchivo.fromPath(path);
                    return Arrays.stream(config.getTipos()).anyMatch(tipo::equals);
                }
                return true; // No se especificaron tipos, incluir todos
            });
    }

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        Config config = Config.create(args);

        try(Stream<Path> archivos = obtenerArchivos()) {
            archivos
                .map(FileInfo::new)
                .forEach(System.out::println);
        }
        catch (IOException e) {
            logger.error("Error al listar archivos", e);
            System.exit(1);
        }
    }
}