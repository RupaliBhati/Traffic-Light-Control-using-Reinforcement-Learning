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
package floetteroed.utilities.networks.basic;

import floetteroed.utilities.networks.construction.AbstractNode;

/**
 * 
 * @author Gunnar Flötteröd
 *
 */
public class BasicNode extends AbstractNode<BasicNode, BasicLink> {

	public BasicNode(String id) {
		super(id);
	}

}
