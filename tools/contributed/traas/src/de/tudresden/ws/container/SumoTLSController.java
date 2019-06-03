/****************************************************************************/
// Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
// Copyright (C) 2017-2018 German Aerospace Center (DLR) and others.
// TraaS module
// Copyright (C) 2016-2017 Dresden University of Technology
// This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v2.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v20.html
// SPDX-License-Identifier: EPL-2.0
/****************************************************************************/
/// @file    SumoTLSController.java
/// @author  Mario Krumnow
/// @author  Evamarie Wiessner
/// @date    2016
/// @version $Id$
///
//
/****************************************************************************/
package de.tudresden.ws.container;

import java.util.HashMap;

/**
 * 
 * @author Mario Krumnow
 *
 */

public class SumoTLSController implements SumoObject {

	public HashMap<String, SumoTLSProgram> programs;
		
	public SumoTLSController(){
		this.programs = new HashMap<String, SumoTLSProgram>();
	}
	
	public void addProgram(SumoTLSProgram logic){
		this.programs.put(logic.subID, logic);
	}
	
	public SumoTLSProgram get(String prog_name){
		
		SumoTLSProgram tl = null;
		if(this.programs.containsKey(prog_name)){
			tl = this.programs.get(prog_name);
		}
		
		return tl;
		
	}
	
	public String toString(){
		return "";
	}
	
}
