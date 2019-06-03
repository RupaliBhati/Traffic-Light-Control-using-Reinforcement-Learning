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
/// @file    CommandProcessor.java
/// @author  Mario Krumnow
/// @author  Evamarie Wiessner
/// @date    2016
/// @version $Id$
///
//
/****************************************************************************/
package de.tudresden.sumo.util;

import it.polito.appeal.traci.TraCIException.UnexpectedData;
import it.polito.appeal.traci.protocol.Command;
import it.polito.appeal.traci.protocol.ResponseContainer;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;

import de.tudresden.sumo.config.Constants;
import de.tudresden.sumo.subscription.Subscription;
import de.tudresden.ws.container.SumoBestLanes;
import de.tudresden.ws.container.SumoColor;
import de.tudresden.ws.container.SumoGeometry;
import de.tudresden.ws.container.SumoLeader;
import de.tudresden.ws.container.SumoLink;
import de.tudresden.ws.container.SumoLinkList;
import de.tudresden.ws.container.SumoNextTLS;
import de.tudresden.ws.container.SumoObject;
import de.tudresden.ws.container.SumoPosition2D;
import de.tudresden.ws.container.SumoPosition3D;
import de.tudresden.ws.container.SumoPrimitive;
import de.tudresden.ws.container.SumoStopFlags;
import de.tudresden.ws.container.SumoStringList;
import de.tudresden.ws.container.SumoTLSProgram;
import de.tudresden.ws.container.SumoVehicleData;
import de.uniluebeck.itm.tcpip.Storage;
import de.tudresden.ws.container.SumoTLSPhase;
import de.tudresden.ws.container.SumoTLSController;

/**
 * 
 * @author Mario Krumnow
 *
 */

public class CommandProcessor extends Query{
 
	int temp;
	public CommandProcessor(Socket sock) throws IOException {
		super(sock);
	}
	
	public synchronized void do_job_set(SumoCommand sc) throws IOException {
		queryAndVerifySingle(sc.cmd);
	}
	
	public void do_subscription(Subscription cs) throws IOException {
		fireAndForget(cs.getCommand());
	}
	
	public synchronized void do_SimulationStep(double targetTime) throws IOException {
		doSimulationStep(targetTime);
	}
	
	public static SumoObject read(int type, Storage s){
		
		SumoObject output = null;
		
		if(type == Constants.TYPE_INTEGER){output = new SumoPrimitive(s.readInt());
		}else if(type == Constants.TYPE_DOUBLE){output = new SumoPrimitive(s.readDouble());
		}else if(type == Constants.TYPE_STRING){output = new SumoPrimitive(s.readStringUTF8());
		}else if(type == Constants.POSITION_2D){
			double x = s.readDouble();
			double y = s.readDouble();
			output = new SumoPosition2D(x,y);
		}else if(type == Constants.POSITION_3D){
			double x = s.readDouble();
			double y = s.readDouble();
			double z = s.readDouble();
			output = new SumoPosition3D(x,y,z);
		}else if(type == Constants.TYPE_STRINGLIST){
			
			SumoStringList ssl = new SumoStringList();
			int laenge = s.readInt();
			for(int i=0; i<laenge; i++){
				ssl.add(s.readStringASCII());
			}
			output = ssl;
		
		}else if(type == Constants.VAR_STOPSTATE){
			
			short s0 = s.readByte();
			SumoStopFlags sf = new SumoStopFlags((byte) s0);
			output = sf;
			
			//if(s0.info.equals("isStopped")){output = sf.stopped;}
			//if(sc.info.equals("isStoppedTriggered")){output = sf.triggered;}
			//if(sc.info.equals("isAtContainerStop")){output = sf.isContainerStop;}
			//if(sc.info.equals("isStoppedParking")){output = sf.getID() == 12;}
			//if(sc.info.equals("isAtBusStop")){output = sf.isBusStop;}
			
			
		}else if(type == Constants.TL_CONTROLLED_LINKS){
				
				SumoLinkList sll = new SumoLinkList();
				
				//read length
				s.readUnsignedByte();
				s.readInt();
				
				int laenge =s.readInt();
				for(int i=0; i<laenge; i++){
				
					s.readUnsignedByte();
					int anzahl = s.readInt();
					
					for(int i1=0; i1<anzahl; i1++){
						
						s.readUnsignedByte();
						s.readInt(); //length
						
						String from = s.readStringASCII();
						String to =s.readStringASCII();
						String over = s.readStringASCII();
						sll.add(new SumoLink(from, to, over));
						
					}
					
				}
				
				output = sll;
			
			}else if(type == Constants.TL_COMPLETE_DEFINITION_RYG){
				
				s.readUnsignedByte();
				s.readInt();
				
				int length = s.readInt();
				
				SumoTLSController sp = new SumoTLSController();
				for(int i=0; i<length; i++){
					
					s.readUnsignedByte();
					String subID = s.readStringASCII();
					
					s.readUnsignedByte();
					int type0 = s.readInt();
					
					s.readUnsignedByte();
					int subParameter = s.readInt();
					
					s.readUnsignedByte();
					int currentPhaseIndex = s.readInt();
					
					SumoTLSProgram stl = new SumoTLSProgram(subID, type0, subParameter, currentPhaseIndex);
					
					s.readUnsignedByte();
					int nbPhases = s.readInt();
					
					for(int i1=0; i1<nbPhases; i1++){
						
						s.readUnsignedByte();
						int duration = s.readInt();
						
						s.readUnsignedByte();
						int duration1 =s.readInt();
						
						s.readUnsignedByte();
						int duration2 = s.readInt();
						
						s.readUnsignedByte();
						String phaseDef = s.readStringASCII();
						
						stl.add(new  SumoTLSPhase(duration, duration1, duration2, phaseDef));
						
					}

					sp.addProgram(stl);
					
				}
				
				output = sp;
				
			}else if(type == Constants.LANE_LINKS){
			
				s.readUnsignedByte();
				s.readInt();
				
				//number of links
				int length = s.readInt();
				SumoLinkList links = new SumoLinkList();
				for(int i=0; i<length; i++){
					
					s.readUnsignedByte();
					String notInternalLane = s.readStringASCII();
					
					s.readUnsignedByte();
					String internalLane = s.readStringASCII();
					
					s.readUnsignedByte();
					byte hasPriority = (byte) s.readUnsignedByte();
					
					s.readUnsignedByte();
					byte isOpened = (byte) s.readUnsignedByte();
					
					s.readUnsignedByte();
					byte hasFoes = (byte) s.readUnsignedByte();
					
					//not implemented
					s.readUnsignedByte();
					String state =s.readStringASCII();
					
					s.readUnsignedByte();
					String direction = s.readStringASCII();
					
					s.readUnsignedByte();
					double laneLength = s.readDouble();
					
					
					links.add(new SumoLink(notInternalLane,internalLane,hasPriority,isOpened,hasFoes,laneLength, state, direction));
				}
				output = links;
				
			}else if(type == Constants.VAR_NEXT_TLS){
			
				s.readUnsignedByte();
				s.readInt();
				
				SumoNextTLS sn = new SumoNextTLS();
				
				int length = s.readInt();
				for(int i=0; i<length; i++){
					
					s.readUnsignedByte();
					String tlsID = s.readStringASCII();
					
					s.readUnsignedByte();
					int ix = s.readInt();
					
					s.readUnsignedByte();
					double dist = s.readDouble();
					
					s.readUnsignedByte();
					int k = s.readUnsignedByte();
					String state = Character.toString ((char) k);
					
					sn.add(tlsID, ix, dist, state);
					
				}
				
				output = sn;
				
			}else if(type == Constants.VAR_LEADER){
				
				s.readUnsignedByte();
				s.readInt();
				
				String vehID = s.readStringASCII();
				s.readUnsignedByte();
				double dist = s.readDouble();
				output = new SumoLeader(vehID, dist);
			
			}else if(type == Constants.VAR_BEST_LANES){
				
				s.readUnsignedByte();
				s.readInt();
				
				int l = s.readInt();
			
				SumoBestLanes sl = new SumoBestLanes();
				for(int i=0; i<l; i++){
				
					s.readUnsignedByte();
					String laneID =s.readStringASCII();
					
					s.readUnsignedByte();
					double length = s.readDouble();
					
					s.readUnsignedByte();
					double occupation = s.readDouble();
					
					s.readUnsignedByte();
					int offset = s.readByte();
					
					s.readUnsignedByte();
					int allowsContinuation = s.readUnsignedByte();
					
					s.readUnsignedByte();
					int nextLanesNo = s.readInt();
					
					LinkedList<String> ll = new LinkedList<String>();
					for(int i1=0; i1<nextLanesNo; i1++){
						String lane = s.readStringASCII();
						ll.add(lane);
					}
					
					sl.add(laneID, length, occupation, offset, allowsContinuation, ll);
				}
			
				output = sl;
				
			}else if(type == Constants.TYPE_POLYGON){
			
			int laenge = s.readUnsignedByte();
			
			SumoGeometry sg = new SumoGeometry();
			for(int i=0; i<laenge; i++){
				double x =  s.readDouble();;
				double y = s.readDouble();;
				sg.add(new SumoPosition2D(x,y));
			}
			
			output = sg;
		
		} else if(type == Constants.TYPE_COLOR){
			
			int r = s.readUnsignedByte();
			int g = s.readUnsignedByte();
			int b = s.readUnsignedByte();
			int a = s.readUnsignedByte();
			
			output = new SumoColor(r, g, b, a);

		}else if(type == Constants.TYPE_UBYTE){
			output = new SumoPrimitive(s.readUnsignedByte());
		}
		
		return output;
	}
	
	public synchronized Object do_job_get(SumoCommand sc) throws IOException{
		
		Object output = null;
		ResponseContainer rc = queryAndVerifySingle(sc.cmd);
		Command resp = rc.getResponse();
	
		verifyGetVarResponse(resp, sc.response, sc.input2, sc.input3);
		verify("", sc.output_type, (int)resp.content().readUnsignedByte());
		
		if(sc.output_type == Constants.TYPE_INTEGER){output = resp.content().readInt();
		}else if(sc.output_type == Constants.TYPE_DOUBLE){output = resp.content().readDouble();
		}else if(sc.output_type == Constants.TYPE_STRING){output = resp.content().readStringUTF8();
		}else if(sc.output_type == Constants.POSITION_2D || sc.output_type == Constants.POSITION_LON_LAT){
			double x = resp.content().readDouble();
			double y = resp.content().readDouble();
			output = new SumoPosition2D(x,y);
		}else if(sc.output_type == Constants.POSITION_3D){
			double x = resp.content().readDouble();
			double y = resp.content().readDouble();
			double z = resp.content().readDouble();
			output = new SumoPosition3D(x,y,z);
		}else if(sc.output_type == Constants.TYPE_STRINGLIST){
			
			SumoStringList ssl = new SumoStringList();
			int laenge = resp.content().readInt();
			for(int i=0; i<laenge; i++){
				ssl.add(resp.content().readStringASCII());
			}
			output = ssl;
		
		}else if(sc.input2 == Constants.VAR_STOPSTATE){
			short s = resp.content().readByte();
			SumoStopFlags sf = new SumoStopFlags((byte) s);
			output = sf;
			
			if(sc.info.equals("isStopped")){output = sf.stopped;}
			if(sc.info.equals("isStoppedTriggered")){output = sf.triggered;}
			if(sc.info.equals("isAtContainerStop")){output = sf.isContainerStop;}
			if(sc.info.equals("isStoppedParking")){output = sf.getID() == 12;}
			if(sc.info.equals("isAtBusStop")){output = sf.isBusStop;}
			
			
		}else if(sc.output_type == Constants.TYPE_COMPOUND){
			
			Object[] obj = null;
			
			//decision making
			if(sc.input2 == Constants.TL_CONTROLLED_LINKS){
				
				SumoLinkList sll = new SumoLinkList();
				
				//read length
				resp.content().readUnsignedByte();
				resp.content().readInt();
				
				int laenge = resp.content().readInt();
				obj = new StringList[laenge];
				
				for(int i=0; i<laenge; i++){
				
					resp.content().readUnsignedByte();
					int anzahl = resp.content().readInt();
					
					for(int i1=0; i1<anzahl; i1++){
						
						resp.content().readUnsignedByte();
						resp.content().readInt(); //length
						
						String from = resp.content().readStringASCII();
						String to = resp.content().readStringASCII();
						String over = resp.content().readStringASCII();
						sll.add(new SumoLink(from, to, over));
						
					}
					
				}
				
				output = sll;
			
			}else if(sc.input2 == Constants.TL_COMPLETE_DEFINITION_RYG){
				
				resp.content().readUnsignedByte();
				resp.content().readInt();
				
				int length = resp.content().readInt();
				
				SumoTLSController sp = new SumoTLSController();
				for(int i=0; i<length; i++){
					
					resp.content().readUnsignedByte();
					String subID = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					int type = resp.content().readInt();
					
					resp.content().readUnsignedByte();
					int subParameter = resp.content().readInt();
					
					resp.content().readUnsignedByte();
					int currentPhaseIndex = resp.content().readInt();
					
					SumoTLSProgram stl = new SumoTLSProgram(subID, type, subParameter, currentPhaseIndex);
					
					resp.content().readUnsignedByte();
					int nbPhases = resp.content().readInt();
					
					for(int i1=0; i1<nbPhases; i1++){
						
						resp.content().readUnsignedByte();
						int duration = resp.content().readInt();
						
						resp.content().readUnsignedByte();
						int duration1 = resp.content().readInt();
						
						resp.content().readUnsignedByte();
						int duration2 = resp.content().readInt();
						
						resp.content().readUnsignedByte();
						String phaseDef = resp.content().readStringASCII();
						
						stl.add(new  SumoTLSPhase(duration, duration1, duration2, phaseDef));
						
					}

					sp.addProgram(stl);
					
				}
				
				output = sp;
				
			}else if(sc.input2 == Constants.LANE_LINKS){
			
				resp.content().readUnsignedByte();
				resp.content().readInt();
				
				//number of links
				int length = resp.content().readInt();
				SumoLinkList links = new SumoLinkList();
				for(int i=0; i<length; i++){
					
					resp.content().readUnsignedByte();
					String notInternalLane = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					String internalLane = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					byte hasPriority = (byte)resp.content().readUnsignedByte();
					
					resp.content().readUnsignedByte();
					byte isOpened = (byte)resp.content().readUnsignedByte();
					
					resp.content().readUnsignedByte();
					byte hasFoes = (byte)resp.content().readUnsignedByte();
					
					//not implemented
					resp.content().readUnsignedByte();
					String state = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					String direction = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					double laneLength = resp.content().readDouble();
					
					
					links.add(new SumoLink(notInternalLane,internalLane,hasPriority,isOpened,hasFoes,laneLength, state, direction));
				}
				output = links;
			}else if(sc.input2 == Constants.VAR_NEXT_TLS){
			
				resp.content().readUnsignedByte();
				resp.content().readInt();
				
				SumoNextTLS sn = new SumoNextTLS();
				
				int length = resp.content().readInt();
				for(int i=0; i<length; i++){
					
					resp.content().readUnsignedByte();
					String tlsID = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					int ix = resp.content().readInt();
					
					resp.content().readUnsignedByte();
					double dist = resp.content().readDouble();
					
					resp.content().readUnsignedByte();
					int k = resp.content().readUnsignedByte();
					String state = Character.toString ((char) k);
					
					sn.add(tlsID, ix, dist, state);
					
				}
				
				output = sn;
				
			}else if(sc.input2 == Constants.VAR_LEADER){
				
				resp.content().readUnsignedByte();
				resp.content().readInt();
				
				String vehID = resp.content().readStringASCII();
				resp.content().readUnsignedByte();
				double dist = resp.content().readDouble();
				output = new SumoLeader(vehID, dist);
			
			}else if(sc.input2 == Constants.VAR_BEST_LANES){
				
				resp.content().readUnsignedByte();
				resp.content().readInt();
				
				int l = resp.content().readInt();
			
				SumoBestLanes sl = new SumoBestLanes();
				for(int i=0; i<l; i++){
				
					resp.content().readUnsignedByte();
					String laneID = resp.content().readStringASCII();
					
					resp.content().readUnsignedByte();
					double length = resp.content().readDouble();
					
					resp.content().readUnsignedByte();
					double occupation = resp.content().readDouble();
					
					resp.content().readUnsignedByte();
					int offset = resp.content().readByte();
					
					resp.content().readUnsignedByte();
					int allowsContinuation = resp.content().readUnsignedByte();
					
					resp.content().readUnsignedByte();
					int nextLanesNo = resp.content().readInt();
					
					LinkedList<String> ll = new LinkedList<String>();
					for(int i1=0; i1<nextLanesNo; i1++){
						String lane = resp.content().readStringASCII();
						ll.add(lane);
					}
					
					sl.add(laneID, length, occupation, offset, allowsContinuation, ll);
				}
			
				output = sl;

            }else if(sc.input2 == Constants.LAST_STEP_VEHICLE_DATA){

                resp.content().readUnsignedByte();
                resp.content().readInt();

                SumoVehicleData vehData  = new SumoVehicleData();

                int numItems = resp.content().readInt();
                for(int i=0; i<numItems; i++){

                    resp.content().readUnsignedByte();
                    String vehID = resp.content().readStringASCII();

                    resp.content().readUnsignedByte();
                    double length = resp.content().readDouble();

                    resp.content().readUnsignedByte();
                    double entryTime = resp.content().readDouble();

                    resp.content().readUnsignedByte();
                    double leaveTime = resp.content().readDouble();

                    resp.content().readUnsignedByte();
                    String typeID = resp.content().readStringASCII();

                    vehData.add(vehID, length, entryTime, leaveTime, typeID);
                }
                output = vehData;
				
			}else if(sc.input2 == Constants.FIND_ROUTE){
				
				resp.content().readInt();
				resp.content().readUnsignedByte();
				resp.content().readInt();
				resp.content().readUnsignedByte();
				
				resp.content().readStringASCII();
				resp.content().readUnsignedByte();
				resp.content().readStringASCII();
				resp.content().readUnsignedByte();
				
				SumoStringList ssl = new SumoStringList();
				int size = resp.content().readInt();
				for(int i=0; i<size; i++){
					ssl.add(resp.content().readStringASCII());
				}
				
				resp.content().readDouble();
				output = ssl;
				
			}else if(sc.input2 == Constants.FIND_INTERMODAL_ROUTE){
				
				LinkedList<SumoStringList> ll = new LinkedList<SumoStringList>();
				int l = resp.content().readInt();
				System.out.println("l: " + l);
				
				for(int i1=0; i1<l; i1++) {
				
					resp.content().readInt();
					resp.content().readUnsignedByte();
					resp.content().readInt();
					resp.content().readUnsignedByte();
					
					resp.content().readStringASCII();
					resp.content().readUnsignedByte();
					resp.content().readStringASCII();
					resp.content().readUnsignedByte();
					
					SumoStringList ssl = new SumoStringList();
					int size = resp.content().readInt();
					for(int i=0; i<size; i++){
						ssl.add(resp.content().readStringASCII());
					}
					
					resp.content().readDouble();
					ll.add(ssl);
				}
				
				output = ll;
					
			}else{
				
				int size = resp.content().readInt();
				obj = new Object[size];
				
				for(int i=0; i<size; i++){
					
					//int k = resp.content().readUnsignedByte();
					//obj[i] = this.get_value(k, resp);
					
				}
				
				output = obj;
			}
			
			
		}
		else if(sc.output_type == Constants.TYPE_POLYGON){
			
			int laenge = resp.content().readUnsignedByte();
			
			SumoGeometry sg = new SumoGeometry();
			for(int i=0; i<laenge; i++){
				double x = resp.content().readDouble();
				double y = resp.content().readDouble();
				sg.add(new SumoPosition2D(x,y));
			}
			
			output = sg;
		
		}
		else if(sc.output_type == Constants.TYPE_COLOR){
			
			int r = resp.content().readUnsignedByte();
			int g = resp.content().readUnsignedByte();
			int b = resp.content().readUnsignedByte();
			int a = resp.content().readUnsignedByte();
			
			output = new SumoColor(r, g, b, a);
		
		}else if(sc.output_type == Constants.TYPE_UBYTE){
			
			output = resp.content().readUnsignedByte();
			
		}
		
		
			
		return output;
	}

	protected static String verifyGetVarResponse(Command resp, int commandID, int variable, String objectID) throws UnexpectedData {
		verify("response code", commandID, resp.id());
		verify("variable ID", variable, (int)resp.content().readUnsignedByte());
		String respObjectID = resp.content().readStringASCII();
		if (objectID != null) {
			verify("object ID", objectID, respObjectID);
		}
		return respObjectID;
	}

	
}
