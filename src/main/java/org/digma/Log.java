package org.digma;

import org.digma.configuration.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unused")
public class Log {

    //all logging use DigmaAgent logger name but all methods can take a Logger argument if you want to log with another name

    private static final String PREFIX = "DigmaAgent";

    private static final Logger LOGGER = Logger.getLogger(DigmaAgent.class.getName());


    public static void info(String message) {
        info(LOGGER, message);
    }

    public static void info(Logger logger, String message) {
        logger.info(PREFIX + ": " + message);
    }

    public static void debug(String message) {
        debug(LOGGER, message);
    }

    public static void debug(Logger logger, String message) {
        if (Configuration.getInstance().isDebug()) {
            logger.info(PREFIX + ": " + message);
        }
    }


    public static void error(String message, Throwable e) {
        error(LOGGER, message, e);
    }

    public static void error(Logger logger, String message, Throwable e) {
        logger.log(Level.SEVERE, PREFIX + ": " + message, e);
    }

    public static void error(String message) {
        error(LOGGER, message);
    }

    public static void error(Logger logger, String message) {
        logger.log(Level.SEVERE, PREFIX + ": " + message);
    }


}
