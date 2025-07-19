package org.marensovich.Bot.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {

    static {
        LoggerUtil.logInfo(LoggerUtil.class, "LoggerUtil initialized");
    }

    private LoggerUtil() {
    }

    public static void logError(Class<?> clazz, String message, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.error(message, throwable);
    }

    public static void logError(Class<?> clazz, String message) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.error(message);
    }

    public static void logWarn(Class<?> clazz, String message) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.warn(message);
    }

    public static void logInfo(Class<?> clazz, String message) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.info(message);
    }

    public static void logDebug(Class<?> clazz, String message) {
        Logger logger = LoggerFactory.getLogger(clazz);
        logger.debug(message);
    }

}
