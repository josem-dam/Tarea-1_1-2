package edu.acceso.tarea1_1_2;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Config config = Config.initialize(args);
        System.out.println(config.getLimit());
        try {
            Stream<FileInfo> archivos = Files.walk(config.getDirectory(), config.getDepth(), FileVisitOption.FOLLOW_LINKS)
                .limit(config.getLimit())
                .filter(path -> {
                    // No funciona.
                    try {
                        Files.readAttributes(path, BasicFileAttributes.class);
                        return true;
                    }
                    catch(IOException e) {
                        logger.warn("Error al acceder al archivo {}", path);
                        return false;
                    }
                })
                .map(FileInfo::new);

            for(FileInfo archivo : (Iterable<FileInfo>) archivos::iterator) {
                System.out.printf("%s\t%s\t%s\t%s%n", 
                    archivo.getPath(), 
                    archivo.isDirectory() ? "DIRECTORIO" : "ARCHIVO", 
                    archivo.getPropietario(), 
                    archivo.getTamanhoLegible());
            }

            archivos.close();
        }
        catch (Exception e) {
            logger.error("Error al listar archivos", e);
            System.exit(1);
        }
    }
}