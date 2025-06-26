package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * @throws IOException  Si ocurre un error al acceder a los archivos
     * @implNote Este método es útil para evitar que el programa se detenga
     * al encontrar un archivo inaccesible, permitiendo que el procesamiento
     * continúe con los demás archivos. 
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

    public static void main(String[] args) {
        Config config = Config.initialize(args);
        System.out.println(config.getLimit());
        try {
            // Protegemos Files.walk con safeStream para evitar excepciones.
            Stream<FileInfo> archivos = safeStream(Files.walk(config.getDirectory(), config.getDepth(), FileVisitOption.FOLLOW_LINKS))
                .limit(config.getLimit())
                .map(FileInfo::new);

            archivos.forEach(archivo ->{
                // Imprimir información de cada archivo
                System.out.printf("%s\t%s\t%s\t%s%n", 
                    archivo.getPath(), 
                    archivo.isDirectory() ? "DIRECTORIO" : "ARCHIVO", 
                    archivo.getPropietario(), 
                    archivo.getTamanhoLegible());
            });

            archivos.close();
        }
        catch (Exception e) {
            logger.error("Error al listar archivos", e);
            System.exit(1);
        }
    }
}