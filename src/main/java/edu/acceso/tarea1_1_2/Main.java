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
     * @param stream Stream de Paths a recorrer
     * @return Stream de Paths accesibles
     */
    public static Stream<Path> safeStream(Stream<Path> stream) {
        Iterator<Path> iterator = stream.iterator();
        Iterable<Path> iterable = () -> new Iterator<>() {
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
            public Path next() {
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
     * Obtiene un Stream de FileInfo representando los archivos en el directorio
     * especificado en la configuración.
     * 
     * @return Stream de FileInfo con información de los archivos.
     * @throws IOException Si ocurre un error al acceder a los archivos.
     */
    public static Stream<FileInfo> obtenerArchivos() throws IOException {
       Config config = Config.getInstance();
        // Protegemos Files.walk con safeStream para evitar excepciones.
       return safeStream(Files.walk(config.getDirectory(), config.getDepth(), FileVisitOption.FOLLOW_LINKS))
           .limit(config.getLimit())
           .filter(path -> {
                // Filtramos por tipo de archivo si se especificó alguno
                if (config.getTipos() != null && config.getTipos().length > 0) {
                    TipoArchivo tipo = TipoArchivo.fromPath(path);
                    return Arrays.stream(config.getTipos()).anyMatch(tipo::equals);
                }
                return true; // No se especificaron tipos, incluir todos
            })
           .map(FileInfo::new);
    }

    /**
     * Genera una línea de información para un archivo.
     * @param archivo El objeto FileInfo que contiene la información del archivo.
     * @return Una cadena formateada con la información del archivo.
     */
    public static String generarLinea(FileInfo archivo) {
        return String.format("%s\t%s\t%s\t%s", 
            archivo.getPath(), 
            archivo.getTipo(), 
            archivo.getPropietario(), 
            archivo.getTamanhoLegible());
    }

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        Config config = Config.create(args);

        try(Stream<FileInfo> archivos = obtenerArchivos()) {
            archivos.map(Main::generarLinea)
                .forEach(System.out::println);
        }
        catch (Exception e) {
            logger.error("Error al listar archivos", e);
            System.exit(1);
        }
    }
}