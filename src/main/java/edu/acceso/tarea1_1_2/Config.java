package edu.acceso.tarea1_1_2;

import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase de configuración que maneja las opciones de línea de comandos
 * y proporciona acceso a la configuración del programa.
 * Implementa el patrón Singleton para asegurar que solo haya una instancia de configuración.
 */
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    /**
     * Instancia única de Configuración.
     * Se inicializa con los argumentos de línea de comandos.
     */
    private static Config instance;

    /**
     * Indica si se debe realizar una búsqueda recursiva.
     * Por defecto es false, lo que significa que no es recursivo.
     */
    private boolean recursive = false;
    /** Límite máximo de archivos a procesar (0 para sin límite) */
    private long limit = 0;
    /** Profundidad máxima de búsqueda (0 para sin límite) */
    private int depth = Integer.MAX_VALUE;
    /** Directorio a buscar (por defecto es el directorio personal) */
    private Path directory = Path.of(System.getProperty("user.home"));
    /** Tipos de archivo a incluir (por defecto incluye todos) */
    private TipoArchivo[] tipos = null;

    private Config(String[] args) {
        Options options = new Options();
        options.addOption("r", "recursive", false, "Recursivo");
        options.addOption("l", "limit", true, "Límite máximo (0 para sin límite)");
        options.addOption("d", "depth", true, "Profundidad (0 para sin límite, requiere -r)");
        options.addOption("h", "help", false, "Muestra esta ayuda");
        options.addOption("t", "type", true, "Tipo de archivo a incluir (puede usarse varias veces)");

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

            recursive = cmd.hasOption("r");
            if(!recursive) {
                depth = 1; 
            }

            if (cmd.hasOption("l")) {
                String limitStr = cmd.getOptionValue("l");
                limit = Integer.parseInt(limitStr);
                if (limit < 0) throw new IllegalArgumentException("El límite debe ser un número natural.");
            }
            if(limit == 0) limit = Long.MAX_VALUE;

            if (cmd.hasOption("d")) {
                if(!cmd.hasOption("r")) throw new IllegalArgumentException("La opción -d requiere que se use -r.");
                String depthStr = cmd.getOptionValue("d");
                int val = Integer.parseInt(depthStr);
                if (val < 0) throw new IllegalArgumentException("La profundidad debe ser un número natural o 0.");
                depth = val;
            }

            String[] posicionales = cmd.getArgs();
            if(posicionales.length == 0) {
                logger.info("No se especificó un directorio, se usará el directorio personal: {}", directory);
            }
            else {
                if(posicionales.length > 1) {
                    logger.warn("Sólo se considerará el primer argumento posicional como directorio.");
                }
                if (posicionales[0].isEmpty()) {
                    logger.info("No se especificó un directorio, se usará el directorio personal: {}", directory);
                }
                else {
                    directory = Path.of(posicionales[0]);
                    if(!Files.exists(directory)) {
                        logger.error("La ruta '{}' no existe", directory);
                        System.exit(1);
                    }
                    else if(!Files.isDirectory(directory)) {
                        logger.error("La ruta '{}' no es un directorio", directory);
                        System.exit(1);
                    } 
                }
            }

            String[] tiposStr = cmd.getOptionValues("t");
            if(tiposStr != null) {
                tipos = Arrays.stream(tiposStr)
                    .map(TipoArchivo::fromString)
                    .toArray(TipoArchivo[]::new);
                for(TipoArchivo tipo: tipos) {
                    if(tipo == null) {
                        logger.error("Tipo de archivo no válido especificado. Los tipos válidos son: {}", Arrays.toString(TipoArchivo.values()));
                        System.exit(1);
                    }
                }
            }
        } catch (ParseException | NumberFormatException e) {
            throw new IllegalArgumentException("Error al analizar los argumentos: " + e.getMessage(), e);
        }
    }

    /**
     * Inicializa la configuración con los argumentos proporcionados.
     * @param args Argumentos de línea de comandos.
     * @return La instancia de configuración inicializada.
     */
    public static Config create(String[] args) {
        if (instance == null) {
            instance = new Config(args);
            return instance;
        }
        throw new IllegalStateException("La configuración ya fue inicializada.");
    }

    /**
     * Obtiene la instancia de configuración.
     * @return La instancia de configuración.
     */
    public static Config getInstance() {
        if (instance == null) throw new IllegalStateException("La configuración no ha sido inicializada.");
        return instance;   
    }

    /**
     * Verifica si la búsqueda es recursiva.
     * @return true si es recursivo, false en caso contrario.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Obtiene el límite máximo de archivos a procesar.
     * @return El límite máximo de archivos (0 para sin límite).
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Obtiene la profundidad máxima de búsqueda.
     * @return La profundidad máxima (0 para sin límite).
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Obtiene el directorio donde se realizará la búsqueda.
     * @return La ruta del directorio.
     */
    public Path getDirectory() {
        return directory;
    }

    /**
     * Obtiene los tipos de archivo a incluir en la búsqueda.
     * @return Un arreglo de tipos de archivo, o null si no se especificaron tipos.
     */
    public TipoArchivo[] getTipos() {
        return tipos;
    }
}