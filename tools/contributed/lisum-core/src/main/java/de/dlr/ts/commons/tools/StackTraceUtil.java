/*
 * Copyright (C) 2016
 * Deutsches Zentrum fuer Luft- und Raumfahrt e.V.
 * Institut fuer Verkehrssystemtechnik
 * 
 * German Aerospace Center
 * Institute of Transportation Systems
 * 
 */
package de.dlr.ts.commons.tools;

import de.dlr.ts.commons.logger.DLRLogger;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 * @author @author <a href="mailto:maximiliano.bottazzi@dlr.de">Maximiliano
 * Bottazzi</a>
 */
/**
 * Simple utilities to return the stack trace of an exception as a String.
 */
public final class StackTraceUtil {

    /**
     *
     * @param aThrowable
     * @return
     */
    public static String getStackTrace(Throwable aThrowable) {
        Writer result = new StringWriter();
        PrintWriter printWriter = new PrintWriter(result);
        aThrowable.printStackTrace(printWriter);
        return result.toString();
    }

    public static void toDLRLogger(Throwable aThrowable) {
        DLRLogger.severe(getStackTrace(aThrowable));
    }

    /**
     * Defines a custom format for the stack trace as String.
     */
    @Deprecated
    public static String getCustomStackTrace(Throwable aThrowable) {
        //add the class name and any message passed to constructor
        StringBuilder result = new StringBuilder("BOO-BOO: ");
        result.append(aThrowable.toString());
        String NEW_LINE = System.getProperty("line.separator");
        result.append(NEW_LINE);

        //add each element of the stack trace
        for (StackTraceElement element : aThrowable.getStackTrace()) {
            result.append(element);
            result.append(NEW_LINE);
        }
        return result.toString();

    }
}
