/*
 * Copyright 2015, 2016 Gunnar Flötteröd
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * contact: gunnar.floetteroed@abe.kth.se
 *
 */ 
package floetteroed.utilities.visualization;

import java.util.TimerTask;

/**
 * <u><b>The entire utilitis.visualization package is experimental!</b></u>
 * 
 * @author Gunnar Flötteröd
 * 
 */
class MoviePlayer extends TimerTask {

	// -------------------- CONSTANTS --------------------

	private final RenderableDynamicData<VisLink> data;

	private final NetVis vis;

	// -------------------- CONSTRUCTION --------------------

	MoviePlayer(final RenderableDynamicData<VisLink> data, final NetVis viz) {
		this.data = data;
		this.vis = viz;
	}

	// -------------------- IMPLEMENTATION OF TimerTask --------------------

	public void run() {
		this.data.fwd();
		this.vis.repaintForMovie();
	}
}
