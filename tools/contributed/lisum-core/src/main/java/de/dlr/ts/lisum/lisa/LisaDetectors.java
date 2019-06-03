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

import de.dlr.ts.commons.logger.DLRLogger;
import de.dlr.ts.lisum.interfaces.DetectorInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author @author <a href="mailto:maximiliano.bottazzi@dlr.de">Maximiliano
 * Bottazzi</a>
 */
class LisaDetectors {

    private final List<LisaDetector> detectors = new ArrayList<>();

    /**
     *
     */
    public LisaDetectors() {
    }

    /**
     *
     */
    public void sort() {
        Collections.sort(detectors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Detectors").append("\n");

        for (LisaDetector det : detectors) {
            sb.append(det).append("\n");
        }

        return sb.toString();
    }

    /**
     *
     * @param cu
     */
    void load(LisaConfigurationFiles.ControlUnit cu) {
        for (LisaConfigurationFiles.ControlUnit.Detector de : cu.detectors) {
            DLRLogger.info(this, "Adding detector: " + de.fullName + " (" + de.objNr + ")");

            LisaDetector lisaDetector = new LisaDetector(de.objNr, de.bezeichnung);
            detectors.add(lisaDetector);
        }
    }

    /**
     *
     * @param detectorId
     * @return
     */
    public DetectorInterface getDetector(String detectorId) {
        for (LisaDetector detector : detectors) {
            if (detector.getId().equals(detectorId)) {
                return detector;
            }
        }

        return null;
    }

    /**
     *
     */
    public void clear() {
        for (LisaDetector detector : detectors) {
            detector.clear();
        }
    }

    /**
     *
     *
     * {71/22/(4)14!-75/9!-49} 71/22/(4)14!-75/9!49
     *
     * @return
     */
    public String getLisaString() {
        StringBuilder sb = new StringBuilder();

        boolean addIndex = false;
        for (int i = 0; i < detectors.size(); i++) {
            LisaDetector detector = detectors.get(i);

            if (detector.isEmpty()) {
                addIndex = true;
            } else {
                sb.append(detector.getLisaString(addIndex)).append("/");
                addIndex = false;
            }
        }

        String ts = sb.toString();

        if (ts.isEmpty()) {
            return "";
        }

        return ts.substring(0, ts.length() - 1);
    }

    /**
     *
     * @return
     */
    public int detectorsCount() {
        return this.detectors.size();
    }

}
