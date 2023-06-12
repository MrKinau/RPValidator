package dev.kinau.resourcepackvalidator;

import dev.kinau.resourcepackvalidator.validator.ValidatorRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

@Slf4j
public class ResourcePackValidator {

    private CommandLine commandLine;

    public static void main(String[] args) {
        new ResourcePackValidator(args);
    }

    public ResourcePackValidator(String[] args) {
        initLogging();
        try {
            this.commandLine = initCLI(args);
            if (commandLine == null) return;
        } catch (ParseException ex) {
            log.error("Could not parse start arguments", ex);
        }
        if (commandLine.hasOption("verbose"))
            adjustLogLevel();

        File rootDir = getRootDirectory();
        if (rootDir == null) return;

        ValidatorRegistry registry = new ValidatorRegistry();
        validate(rootDir, registry);
    }

    private void initLogging() {
        try {
            LogManager.getLogManager().readConfiguration(ResourcePackValidator.class.getClassLoader().getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void adjustLogLevel() {
        try {
            LogManager.getLogManager().updateConfiguration((key) -> (oldVal, newVal) -> {
                if (key.equals(".level"))
                    return "FINEST";
                if (key.equals("java.util.logging.ConsoleHandler.level"))
                    return "FINEST";
                return oldVal;
            });
        } catch (IOException ex) {
            log.error("Could not adjust the log level", ex);
        }
    }

    private CommandLine initCLI(String[] args) throws ParseException {
        Options options = new Options();
        options.addOption("help", false, "prints the help");
        options.addOption("rp", "resourcepack", true, "specifies the path to the resourcepack to be validated");
        options.addOption("v", "verbose", false, "sets the log level to DEBUG");

        CommandLineParser parser = new DefaultParser();
        CommandLine commandLine = parser.parse(options, args);
        if (commandLine.hasOption("help")) {
            new HelpFormatter().printHelp("Resource Pack Validator", options);
            return null;
        }
        return commandLine;
    }

    private File getRootDirectory() {
        File rootDir = new File("resourcepack");
        if (commandLine.hasOption("rp"))
            rootDir = new File(commandLine.getOptionValue("rp"));

        if (!rootDir.exists()) {
            log.error("Could not find directory {}", rootDir.getAbsolutePath());
            return null;
        }

        if (!rootDir.isDirectory()) {
            log.error("Path {} points to a file, but needs to be a directory", rootDir.getAbsolutePath());
            return null;
        }

        return rootDir;
    }

    private void validate(File rootDir, ValidatorRegistry registry) {
        log.info("Starting validation of {}", rootDir.getPath());
        ValidationJob validation = new ValidationJob(rootDir, registry);
        validation.validate();
    }

}