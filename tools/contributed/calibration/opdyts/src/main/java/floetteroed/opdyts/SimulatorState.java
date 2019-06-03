/*
 * Opdyts - Optimization of dynamic traffic simulations
 *
 * Copyright 2015, 2016 Gunnar Flötteröd
 * 
 *
 * This file is part of Opdyts.
 *
 * Opdyts is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Opdyts is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Opdyts.  If not, see <http://www.gnu.org/licenses/>.
 *
 * contact: gunnar.floetteroed@abe.kth.se
 *
 */ 
package floetteroed.opdyts;

import floetteroed.utilities.math.Vector;

/**
 * Represents a simulator state.
 * 
 * @author Gunnar Flötteröd
 * 
 */
public interface SimulatorState {

	/**
	 * Returns a reference to a real-valued, fixed-dimensional vector
	 * representation of this state.
	 * 
	 * @return a reference to a real-valued, fixed-dimensional vector
	 *         representation of this state
	 */
	public Vector getReferenceToVectorRepresentation();

	/**
	 * Sets the simulator to this SimulatorState, meaning that the next
	 * simulation transition starts out from this state.
	 */
	public void implementInSimulation();

}
