package edu.acceso.tarea1_1_2;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static Config instance;

    private boolean recursive = false;
    // Límite por defecto es 0, lo que significa sin límite
    private long limit = 0;
    private int depth = Integer.MAX_VALUE;
    private Path directory = Path.of(System.getProperty("user.home"));

    private Config(String[] args) {
        Options options = new Options();
        options.addOption("r", "recursive", false, "Recursivo");
        options.addOption("l", "limit", true, "Límite máximo (0 para sin límite)");
        options.addOption("d", "depth", true, "Profundidad (0 para sin límite, requiere -r)");
        options.addOption("h", "help", false, "Muestra esta ayuda");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if(cmd.hasOption("help")) {
                new HelpFormatter().printHelp(
                    "java -jar listado.jar",
                    "Lista archivos y directorios con opciones de recursividad, límite y profundidad.\nOpciones disponibles:",
                    options,
                    "\nEjemplo: java -jar listado.jar -r -l 10 -d 2",
                    true
                );
                System.exit(0);
            }

            this.recursive = cmd.hasOption("r");
            if(!this.recursive) {
                this.depth = 1; 
            }

            if (cmd.hasOption("l")) {
                String limitStr = cmd.getOptionValue("l");
                this.limit = Integer.parseInt(limitStr);
                if (this.limit < 0) throw new IllegalArgumentException("El límite debe ser un número natural.");
            }
            if(this.limit == 0) this.limit = Long.MAX_VALUE;

            if (cmd.hasOption("d")) {
                if(!cmd.hasOption("r")) throw new IllegalArgumentException("La opción -d requiere que se use -r.");
                String depthStr = cmd.getOptionValue("d");
                int val = Integer.parseInt(depthStr);
                if (val < 0) throw new IllegalArgumentException("La profundidad debe ser un número natural o 0.");
                this.depth = val;
            }

            String[] posicionales = cmd.getArgs();
            if(posicionales.length == 0) {
                logger.info("No se especificó un directorio, se usará el directorio personal: {}", this.directory);
            }
            else {
                if(posicionales.length > 1) {
                    logger.warn("Sólo se considerará el primer argumento posicional como directorio.");
                }
                if (posicionales[0].isEmpty()) {
                    logger.info("No se especificó un directorio, se usará el directorio personal: {}", this.directory);
                }
                else {
                    this.directory = Path.of(posicionales[0]);
                    if(!Files.exists(this.directory)) {
                        logger.error("La ruta '{}' no existe", this.directory);
                        System.exit(1);
                    }
                    else if(!Files.isDirectory(this.directory)) {
                        logger.error("La ruta '{}' no es un directorio", this.directory);
                        System.exit(1);
                    } 
                }
            }
        } catch (ParseException | NumberFormatException e) {
            throw new IllegalArgumentException("Error al analizar los argumentos: " + e.getMessage(), e);
        }
    }

    public static Config initialize(String[] args) {
        if (instance == null) {
            instance = new Config(args);
            return instance;
        }
        throw new IllegalStateException("La configuración ya fue inicializada.");
    }

    public Config getInstance() {
        if (instance == null) throw new IllegalStateException("La configuración no ha sido inicializada.");
        return instance;   
    }

    public boolean isRecursive() {
        return recursive;
    }

    public long getLimit() {
        return limit;
    }

    public int getDepth() {
        return depth;
    }

    public Path getDirectory() {
        return directory;
    }
}