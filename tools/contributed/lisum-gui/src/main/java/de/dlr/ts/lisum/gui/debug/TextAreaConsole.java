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
package de.dlr.ts.lisum.gui.debug;

import java.io.IOException;
import java.io.OutputStream;
import javafx.application.Platform;
import javafx.scene.control.TextArea;

/**
 *
 */
class TextAreaConsole extends OutputStream
{    
    private final TextArea txtArea;
    private final StringBuilder buffer = new StringBuilder(128);

    
    /**
     *
     * @param txtArea
     */
    public TextAreaConsole(TextArea txtArea)
    {
        this.txtArea = txtArea;
    }

    /**
     * 
     * @param b
     * @throws IOException 
     */
    @Override
    public synchronized void write(int b) throws IOException
    {
        buffer.append((char) b);
        
        if(b == 10)
        {
            String aa = buffer.toString();
            buffer.delete(0, buffer.length());
            Platform.runLater(() -> { txtArea.appendText(aa); });
        }
    }
}
