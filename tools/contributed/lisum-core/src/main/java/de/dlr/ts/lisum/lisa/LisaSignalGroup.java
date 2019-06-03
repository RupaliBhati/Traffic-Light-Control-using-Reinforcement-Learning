/****************************************************************************/
// Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
// Copyright (C) 2016-2018 German Aerospace Center (DLR) and others.
// This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v2.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v20.html
// SPDX-License-Identifier: EPL-2.0
/****************************************************************************/
/// @file    Constants.java
/// @author  Maximiliano Bottazzi
/// @date    2016
/// @version $Id$
///
//
/****************************************************************************/
package de.dlr.ts.lisum.lisa;

import de.dlr.ts.commons.logger.ToString;
import de.dlr.ts.lisum.enums.LightColor;
import de.dlr.ts.lisum.interfaces.SignalGroupInterface;

/**
 *
 */
class LisaSignalGroup implements Comparable<LisaSignalGroup>, SignalGroupInterface
{
    private final String name;
    private int index;
    private LightColor color = LightColor.OFF;

    
    /**
     *
     * @param index
     * @param name
     */
    public LisaSignalGroup(int index, String name)
    {
        this.name = name;
        this.index = index;
    }

    public int getIndex() {
        return index;
    }    
    
    /**
     *
     * @param color
     */
    @Override
    public void setColor(LightColor color)
    {
        if(color != null)
            this.color = color;
        else
            this.color = LightColor.OFF;
    }

    /**
     * 
     * @return 
     */
    public String getName()
    {
        return name;
    }
    
    /**
     *
     * @return
     */
    @Override
    public LightColor getColor()
    {
        return color;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString()
    {
        ToString tos = new ToString("SignalGruppe");
        tos.add("name", name);
        tos.add("color", color.toString());
        return tos.toString();
    }

    /**
     *
     * @param t
     * @return
     */
    @Override
    public int compareTo(LisaSignalGroup t)
    {
        if (t.index > this.index)
        {
            return -1;
        }
        if (t.index == this.index)
        {
            return 0;
        }
        return 1;
    }
    
}
