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
package floetteroed.cadyts.calibrators;

import java.io.OutputStream;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

/**
 * 
 * @author Gunnar Flötteröd
 * 
 */
public class StreamFlushHandler extends StreamHandler {

	// -------------------- CONSTRUCTION --------------------

	public StreamFlushHandler(final OutputStream out, final Formatter formatter) {
		super(out, formatter);
	}

	// -------------------- OVERRIDING OF StreamHandler --------------------

	@Override
	public void publish(final LogRecord record) {
		super.publish(record);
		this.flush();
	}
}
