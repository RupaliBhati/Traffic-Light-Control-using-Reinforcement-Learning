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
package floetteroed.cadyts.interfaces.sumo;

import static floetteroed.utilities.XMLHelpers.DOUBLE2INT_EXTRACTOR;
import static floetteroed.utilities.XMLHelpers.extractItems;
import static java.lang.Double.parseDouble;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.xml.sax.Attributes;

import floetteroed.cadyts.calibrators.filebased.xml.PopulationHandler;
import floetteroed.cadyts.demand.PlanBuilder;
import floetteroed.utilities.DynamicData;
import floetteroed.utilities.Tuple;

/**
 * 
 * @author Gunnar Flötteröd
 * 
 */
class SumoPopulationHandler extends PopulationHandler<SumoAgent> {

	// -------------------- CONSTANTS --------------------

	static final String VEHICLE_ELEM = "vehicle";

	static final String VEHICLEID_ATTR = "id";

	static final String DEPART_ATTR = "depart";

	static final String FROMTAZ_ATTR = "fromtaz";

	static final String TOTAZ_ATTR = "totaz";

	static final String ROUTE_ELEM = "route";

	static final String EDGES_ATTR = "edges";

	static final String EXITTIMES_ATTR = "exitTimes";

	static final String CHOICEPROB_ATTR = "probability";

	// -------------------- MEMBERS --------------------

	private final DynamicData<String> travelTimes;

	private final PlanBuilder<String> planBuilder;

	private final double demandScale;

	private final double stayAtHomeProb;

	private Set<Object> clonedAgentIDs = null;

	private String clonePostfix = null;

	private SumoAgent tempAgent = null;

	// -------------------- CONSTRUCTION --------------------

	SumoPopulationHandler(final PlanBuilder<String> planBuilder,
			final DynamicData<String> travelTimes, final double demandScale) {

		if (planBuilder == null) {
			throw new IllegalArgumentException("planBuilder is null");
		}
		this.planBuilder = planBuilder;

		this.travelTimes = travelTimes;
		Logger.getLogger(this.getClass().getName()).fine(
				"travelTimes is " + (this.travelTimes == null ? "" : "not ")
						+ "null");

		if (demandScale < 1 || demandScale > 2) {
			throw new IllegalArgumentException("demandScale of " + demandScale
					+ " is not in [1,2]");
		}
		this.demandScale = demandScale;
		this.stayAtHomeProb = (demandScale - 1.0) / demandScale;
	}

	void setClonedAgentIDs(final Set<Object> clonedAgentIDs,
			final String clonePostfix) {
		if (clonePostfix == null && clonedAgentIDs != null) {
			throw new IllegalArgumentException("clonePostfix is null");
		}
		this.clonePostfix = clonePostfix;
		this.clonedAgentIDs = clonedAgentIDs;
	}

	// -------------------- OVERRIDING OF DefaultHandler --------------------

	private List<Tuple<String, String>> extractMiscAttrs(
			final Attributes allAttrs) {
		final List<Tuple<String, String>> result = new ArrayList<Tuple<String, String>>();
		for (int i = 0; i < allAttrs.getLength(); i++) {
			final String name = allAttrs.getQName(i);
			if ((!VEHICLEID_ATTR.equals(name)) && (!DEPART_ATTR.equals(name))
					&& (!FROMTAZ_ATTR.equals(name))
					&& (!TOTAZ_ATTR.equals(name))) {
				final String value = allAttrs.getValue(i);
				result.add(new Tuple<String, String>(name, value));
			}
		}
		return result;
	}

	@Override
	public void startElement(String namespaceURI, String sName, String qName,
			Attributes attrs) {
		if (VEHICLE_ELEM.equals(qName)) {
			final String agentId = attrs.getValue(VEHICLEID_ATTR);
			final int dpt_s = DOUBLE2INT_EXTRACTOR.extract(attrs
					.getValue(DEPART_ATTR));
			final String fromTAZ = attrs.getValue(FROMTAZ_ATTR);
			final String toTAZ = attrs.getValue(TOTAZ_ATTR);
			final List<Tuple<String, String>> miscAttrs = this
					.extractMiscAttrs(attrs);
			this.tempAgent = new SumoAgent(agentId, dpt_s, fromTAZ, toTAZ,
					miscAttrs);
			if (this.stayAtHomeProb > 0) {
				final SumoPlan stayAtHomePlan = new SumoPlan(this.tempAgent
						.getPlans().size(), dpt_s, new ArrayList<String>(0),
						new ArrayList<Integer>(0), this.travelTimes,
						this.planBuilder);
				this.tempAgent.addPlan(stayAtHomePlan, this.stayAtHomeProb);
			}
		} else if (ROUTE_ELEM.equals(qName)) {
			final List<String> edges = extractItems(attrs.getValue(EDGES_ATTR));
			final List<Integer> exits = extractItems(attrs
					.getValue(EXITTIMES_ATTR), DOUBLE2INT_EXTRACTOR);
			if (edges.size() != exits.size()) {
				throw new RuntimeException("vehicle " + this.tempAgent.getId()
						+ ": there are " + edges.size() + " edges but "
						+ exits.size() + " exit times in its route.");
			}
			final int routeId = this.tempAgent.getPlans().size();
			final double prob = parseDouble(attrs.getValue(CHOICEPROB_ATTR))
					/ this.demandScale;
			final SumoPlan newPlan = new SumoPlan(routeId, this.tempAgent
					.getDptTime_s(), edges, exits, this.travelTimes,
					this.planBuilder);
			this.tempAgent.addPlan(newPlan, prob);
		}
	}

	@Override
	public void endElement(String uri, String lName, String qName) {
		if (VEHICLE_ELEM.equals(qName)) {
			this.putNextAgent(this.tempAgent);
			if (this.clonedAgentIDs != null
					&& this.clonedAgentIDs.contains(this.tempAgent.getId())) {
				this.putNextAgent(new SumoAgent(this.tempAgent, "-CLONE"
						+ this.clonePostfix));
			}
		}
	}
}
