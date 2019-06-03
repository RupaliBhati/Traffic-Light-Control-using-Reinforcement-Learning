/*
 * Cadyts - Calibration of dynamic traffic simulations
 *
 * Copyright 2009-2016 Gunnar Flötteröd
 * 
 *
 * This file is part of Cadyts.
 *
 * Cadyts is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cadyts is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cadyts.  If not, see <http://www.gnu.org/licenses/>.
 *
 * contact: gunnar.floetteroed@abe.kth.se
 *
 */ 
package floetteroed.cadyts.interfaces.defaults;

import floetteroed.cadyts.calibrators.Calibrator;

/**
 * 
 * @author Gunnar Flötteröd
 * 
 */
public class BasicMeasurementLoaderStringLinks extends
		BasicMeasurementLoader<String> {

	// -------------------- CONSTRUCTION --------------------

	public BasicMeasurementLoaderStringLinks(final Calibrator<String> calibrator) {
		super(calibrator);
	}

	// --------------- IMPLEMENTATION OF BasicMeasurementLoader ---------------

	@Override
	protected String label2link(String label) {
		return label;
	}

}
