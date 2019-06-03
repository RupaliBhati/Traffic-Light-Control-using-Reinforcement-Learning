/** ************************************************************************* */
// Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
// Copyright (C) 2016-2018 German Aerospace Center (DLR) and others.
// This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v2.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v20.html
// SPDX-License-Identifier: EPL-2.0
/** ************************************************************************* */
/// @file    Constants.java
/// @author  Maximiliano Bottazzi
/// @date    2016
/// @version $Id$
///
//
/** ************************************************************************* */
package de.dlr.ts.lisum.lisa;

/**
 *
 * @author @author <a href="mailto:maximiliano.bottazzi@dlr.de">Maximiliano
 * Bottazzi</a>
 */
class PutMessageResponse {

    private long zeit;
    private double tx;
    private IstVektorType istVektor;
    private String signalsStateVector;
    //AusgangsVektor ;    

    /**
     *
     * @param line
     */
    public PutMessageResponse(String line) {
        this.parse(line);
    }

    @Override
    public String toString() {
        return "PutMessageResponse: Time=" + zeit + ", tx=" + tx + ", istVektor=" + istVektor + ", " + signalsStateVector;
    }        
    
    /**
     * 9:{9.0}{0;9;1;0;1;0;1;1;1;1}{48/3}{}{}{}
     *
     * @param line
     */
    public final void parse(String line) {
        /**
         * Extract value of the xml string Result:
         * 9:{9.0}{0;9;1;0;1;0;1;1;1;1}{48/3}{}{}{}
         */
        int start = line.indexOf(">");
        line = line.substring(start + 1);
        int end = line.indexOf("<");
        line = line.substring(0, end);

        /**
         * Separates components Result: 9 {9.0} {0;9;1;0;1;0;1;1;1;1} {48/3} {}
         * {} {}
         */
        line = line.replace(":", "");
        line = line.replaceAll("\\{", " \\{");

        /**
         * Extracts fields spliting with " "
         */
        String[] split = line.split(" ");

        /**
         * Extracting time
         */
        zeit = new Long(split[0]);

        /**
         * Extracting tx
         */
        String gg = split[1].replaceAll("\\{", "").replaceAll("\\}", "");
        tx = new Double(gg);

        /**
         * Extracting istVektor
         */
        istVektor = new IstVektorType(split[2]);

        /**
         * Extracting signal states
         */
        signalsStateVector = split[3];
    }

    /**
     *
     * @return
     */
    public long getZeit() {
        return zeit;
    }

    /**
     *
     * @return
     */
    public double getTx() {
        return tx;
    }

    /**
     *
     * @return
     */
    public IstVektorType getIstVektor() {
        return istVektor;
    }

    /**
     *
     * @return
     */
    public String getSignalsStateVector() {
        return signalsStateVector;
    }

}
