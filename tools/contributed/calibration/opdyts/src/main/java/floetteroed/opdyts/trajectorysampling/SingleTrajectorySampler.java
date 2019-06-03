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
package floetteroed.opdyts.trajectorysampling;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import floetteroed.opdyts.DecisionVariable;
import floetteroed.opdyts.ObjectiveFunction;
import floetteroed.opdyts.SimulatorState;
import floetteroed.opdyts.convergencecriteria.ConvergenceCriterion;
import floetteroed.opdyts.convergencecriteria.ConvergenceCriterionResult;
import floetteroed.utilities.statisticslogging.Statistic;

/**
 * 
 * @author Gunnar Flötteröd
 *
 */
public class SingleTrajectorySampler<U extends DecisionVariable> implements TrajectorySampler<U> {

	// -------------------- MEMBERS --------------------

	private final U decisionVariable;

	private final ObjectiveFunction objectiveFunction;

	private final ConvergenceCriterion convergenceCriterion;

	// private boolean initialized = false;

	private ConvergenceCriterionResult convergenceResult = null;

	private SimulatorState fromState = null;

	private TransitionSequence<U> transitionSequence = null;

	private int totalTransitionCnt = 0;

	// -------------------- CONSTRUCTION --------------------

	public SingleTrajectorySampler(final U decisionVariable, final ObjectiveFunction objectiveFunction,
			final ConvergenceCriterion convergenceCriterion) {
		this.decisionVariable = decisionVariable;
		this.objectiveFunction = objectiveFunction;
		this.convergenceCriterion = convergenceCriterion;
	}

	// --------------- IMPLEMENTATION OF TrajectorySampler ---------------

	@Override
	public ObjectiveFunction getObjectiveFunction() {
		return this.objectiveFunction;
	}

	@Override
	public boolean foundSolution() {
		return ((this.convergenceResult != null) && this.convergenceResult.converged);
	}

	@Override
	public Map<U, ConvergenceCriterionResult> getDecisionVariable2convergenceResultView() {
		final Map<U, ConvergenceCriterionResult> result = new LinkedHashMap<>();
		result.put(this.decisionVariable, this.convergenceResult);
		return Collections.unmodifiableMap(result);
	}

	@Override
	public U getCurrentDecisionVariable() {
		return this.decisionVariable;
	}

	// @Override
	// public void initialize() {
	// if (this.initialized) {
	// throw new RuntimeException(
	// "Create new instance instead of re-initializing.");
	// }
	// this.initialized = true;
	// this.decisionVariable.implementInSimulation();
	// }

	@Override
	public void afterIteration(SimulatorState newState) {
		this.totalTransitionCnt++;
		if (this.fromState != null) {
			if (this.transitionSequence == null) {
				this.transitionSequence = new TransitionSequence<U>(this.fromState, this.decisionVariable, newState,
						this.objectiveFunction.value(newState));
			} else {
				this.transitionSequence.addTransition(this.fromState, this.decisionVariable, newState,
						this.objectiveFunction.value(newState));
			}
			this.convergenceResult = this.convergenceCriterion.evaluate(this.transitionSequence.getTransitions(),
					this.transitionSequence.additionCnt());
		}
		this.fromState = newState;
		this.decisionVariable.implementInSimulation();
	}

	@Override
	public int getTotalTransitionCnt() {
		return this.totalTransitionCnt;
		// if (this.transitionSequence != null) {
		// return this.transitionSequence.size();
		// } else {
		// return 0;
		// }
	}

	@Override
	public void addStatistic(final String logFileName, final Statistic<SamplingStage<U>> statistic) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setStandardLogFileName(final String logFileName) {
		throw new UnsupportedOperationException();
	}
}
