/****************************************************************************/
// Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
// Copyright (C) 2017-2018 German Aerospace Center (DLR) and others.
// TraaS module
// Copyright (C) 2013-2017 Dresden University of Technology
// This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v2.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v20.html
// SPDX-License-Identifier: EPL-2.0
/****************************************************************************/
/// @file    Main.java
/// @author  Mario Krumnow
/// @date    2013
/// @version $Id$
///
//
/****************************************************************************/
import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Simulation;
import de.tudresden.sumo.cmd.Vehicle;
import de.tudresden.sumo.cmd.Inductionloop;
import de.tudresden.ws.container.SumoVehicleData;

public class Main {

	static String sumo_bin = "sumo-gui";
	static String config_file = "simulation/config.sumo.cfg";
	static double step_length = 0.01;		

	public static void main(String[] args) {
	
		
		try{
			
			SumoTraciConnection conn = new SumoTraciConnection(sumo_bin, config_file);
			conn.addOption("step-length", step_length+"");
			conn.addOption("start", "true"); //start sumo immediately
		
			//start Traci Server
			conn.runServer();
			
			for(int i=0; i<3600; i++){
			
				conn.do_timestep();
				conn.do_job_set(Vehicle.add("v"+i, "car", "r1", 0, 0, 13.8, (byte) 1));
                double timeSeconds = (int)conn.do_job_get(Simulation.getCurrentTime()) / 1000.0;
                System.out.println(String.format("Step %s", timeSeconds));

                SumoVehicleData vehData = (SumoVehicleData)conn.do_job_get(Inductionloop.getVehicleData("loop1"));
                for (SumoVehicleData.VehicleData d : vehData.ll) {
                    System.out.println(String.format("  veh=%s len=%s entry=%s leave=%s type=%s", d.vehID, d.length, d.entry_time, d.leave_time, d.typeID));
                }
			}
			
			conn.close();
			
		}catch(Exception ex){ex.printStackTrace();}
		
	}

}
