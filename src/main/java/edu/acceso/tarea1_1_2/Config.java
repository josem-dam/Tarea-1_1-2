package edu.acceso.tarea1_1_2;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import edu.acceso.tarea1_1_2.archivos.TipoArchivo;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Clase que gestiona la configuración de la aplicación. Usa el patrón Singleton para generar
 * una única instancia de configuración y que pueda obtenerse desde cualquier parte del programa.
 * Implementa el análisis de argumentos de línea de comandos con la librería picocli.
 */
@Command(name = "tarea1_1_2", mixinStandardHelpOptions = true, version = "1.0.0",
         description = "Herramienta para buscar archivos con diferentes criterios")
public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);

    /** La instancia única de configuración (patrón Singleton) */
    private static Config instance;

    private static final String DEFAULT_DIRECTORY = System.getProperty("user.home");

    /** 
     * Conversor para el TipoArchivo. En realidad, picocli puede manejar enums sin necesidad
     * de crer un conversor personalizado, pero en {@link TipoArchivo} se implementó un método
     * {@link TipoArchivo#fromString} para que la cadena no tuviera que ser forzosamente en mayúsculas.
     */
    private static class TipoArchivoConverter implements CommandLine.ITypeConverter<TipoArchivo> {
        @Override
        public TipoArchivo convert(String value) throws Exception {
            TipoArchivo tipo = TipoArchivo.fromString(value);
            if (tipo == null) throw new CommandLine.TypeConversionException("Tipo de archivo inválido: " + value);
            return tipo;
        }
    }

    // Estas son las opciones de línea de comandos

    @Option(names = {"-r", "--recursive"}, 
            description = "Habilita la recursividad en la búsqueda")
    private boolean recursive = false;

    @Option(names = {"-l", "--limit"}, paramLabel = "LIMITE",
            description = "Máxima cantidad de archivos mostrados. 0, sin límite")
    private long limit = Long.MAX_VALUE;

    @Option(names = {"-d", "--depth"}, paramLabel = "PROFUNDIDAD",
            description = "Máxima profundidad de recursión, 0, sin límite")
    private int depth = 1;

    @Option(names = {"-s", "--silent"}, 
            description = "Modo silencioso, sólo muestra errores. Incompatible con -v")
    private boolean silent = false;

    @Option(names = {"-v", "--verbose"},
            description = "Verbosidad de los mensajes de información. Puede repetirse para aumentarla")
    private boolean[] verbosity;

    @Option(names = {"-t", "--type"}, paramLabel = "TIPO", converter = TipoArchivoConverter.class,
            description = "Tipo de archivo a mostrar (puede usarse varias veces)")
    private TipoArchivo[] tipos;

    // Estos los parámetros posicionales.

    @Parameters(paramLabel = "DIRECTORIO", arity = "0..1",
            description = "Directorio base para la búsqueda")
    private Optional<Path> directory;

    /**
     * Constructor privado para evitar instanciación externa (patrón Singleton).
     */
    private Config() {
        super();
    }

    /**
     * Crea la instancia única de Config a partir de los argumentos de línea de comandos.
     * @param args Argumentos de línea de comandos.
     * @return La instancia con la configuración.
     */
    public static Config create(String[] args) {
        if (instance != null) {
            throw new IllegalStateException("Ya existe una configuración. Use getInstance() para obtenerla");
        }

        instance = new Config();
        CommandLine cmd = new CommandLine(instance);

        try {
            cmd.parseArgs(args);

            if (cmd.isUsageHelpRequested()) {
                cmd.usage(System.out);
                System.exit(0);
            }

            if (cmd.isVersionHelpRequested()) {
                cmd.printVersionHelp(System.out);
                System.exit(0);
            }

            // Validaciones y reasignaciones de valor adicionales
            instance.validate();
            
        } catch (CommandLine.ParameterException e) {
            logger.error("Error en parámetros: {}", e.getMessage());
            cmd.usage(System.err);
            System.exit(2);
        }

        return instance;
    }

    /**
     * Obtiene la instancia única de Config.
     * @return La instancia con la configuración.
     */
    public static Config getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Configuración no inicializada. Llama a create() primero.");
        }
        return instance;
    }

    /**
     * Valida y ajusta los parámetros de configuración, y define valores por defecto
     * o incompatibilidades entre opciones.
     */
    private void validate() {
        // 0 significa "sin límite".
        if (depth == 0) depth = Integer.MAX_VALUE;
        if (limit == 0) limit = Long.MAX_VALUE;

        // En caso de que la profundidad no sea 1, la búsqueda es recursiva.
        if (depth != 1) recursive = true;

        if (limit < 0) {
            throw new CommandLine.ParameterException(new CommandLine(this), "El límite no puede ser negativo");
        }

        if (depth < 0) {
            throw new CommandLine.ParameterException(new CommandLine(this), "La profundidad no puede ser negativa");
        }

        if (silent && verbosity.length > 0) {
            throw new CommandLine.ParameterException(new CommandLine(this), "Las opciones -s y -v son incompatibles");
        }
    }

    /**
     * Indica si la búsqueda es recursiva.
     * @return true, si la búsqueda es recursiva.
     */
    public boolean isRecursive() {
        return recursive;
    }

    /**
     * Obtiene el límite máximo de archivos a mostrar.
     * @return El límite máximo de archivos a mostrar.
     */
    public long getLimit() {
        return limit;
    }

    /**
     * Obtiene la profundidad máxima de recursión.
     * @return La profundidad máxima de recursión.
     */
    public int getDepth() {
        return depth;
    }

    /**
     * Obtiene el nivel de registro.
     * @return El nivel de registro.
     */
    public Level getLogLevel() {
        if(silent) return Level.ERROR;

        int verbose = (verbosity == null) ? 0 : verbosity.length;
        return switch (verbose) {
            case 0  -> Level.WARN;
            case 1  -> Level.INFO;
            case 2  -> Level.DEBUG;
            default -> Level.TRACE;
        };
    }

    /**
     * Obtiene el directorio base para la búsqueda.
     * @return El directorio base para la búsqueda.
     */
    public Path getDirectory() {
        return directory.orElse(Path.of(DEFAULT_DIRECTORY));
    }

    /**
     * Obtiene los tipos de archivo que se desea mostrar.
     * @return Los tipos de archivo que se desea mostrar.
     */
    public TipoArchivo[] getTipos() {
        return tipos;
    }
}