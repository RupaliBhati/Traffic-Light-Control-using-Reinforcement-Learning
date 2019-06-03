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

/**
 * A decision variable is a set of parameters that may be chosen such that an
 * objective function, represented by an instance of ObjectiveFunction, is
 * minimized.
 * 
 * @author Gunnar Flötteröd
 *
 */
public interface DecisionVariable {

	/**
	 * Implements the effect of this decision variable in the simulation,
	 * meaning that the next simulation transition is according to this decision
	 * variable.  
	 * <br/><br/>
	 * Notes:<ul>
	 * <li> This needs to influence the simulation in each iteration, not just at the start iterations. </li>
	 * </ul>
	 */
	public void implementInSimulation();

}
