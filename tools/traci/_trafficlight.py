# -*- coding: utf-8 -*-
# Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
# Copyright (C) 2011-2018 German Aerospace Center (DLR) and others.
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
# SPDX-License-Identifier: EPL-2.0

# @file    _trafficlight.py
# @author  Michael Behrisch
# @date    2011-03-16
# @version $Id$

from __future__ import absolute_import
import struct
from .domain import Domain
from .storage import Storage
from . import constants as tc
from .exceptions import TraCIException


class Phase:

    def __init__(self, duration, state, minDur=-1, maxDur=-1, next=-1):
        self.duration = duration
        self.state = state
        self.minDur = minDur  # minimum duration (only for actuated tls)
        self.maxDur = maxDur  # maximum duration (only for actuated tls)
        self.next = next

    def __repr__(self):
        return ("Phase(duration=%s, state='%s', minDur=%s, maxDur=%s, next=%s)" %
                (self.duration, self.state, self.minDur, self.maxDur, self.next))


class Logic:

    def __init__(self, programID, type, currentPhaseIndex, phases=None, subParameter=None):
        self.programID = programID
        self.type = type
        self.currentPhaseIndex = currentPhaseIndex
        self.phases = phases if phases is not None else []
        self.subParameter = subParameter if subParameter is not None else {}

    def getPhases(self):
        return self.phases

    def getSubID(self):
        return self.programID

    def getType(self):
        return self.type

    def getParameters(self):
        return self.subParameter

    def getParameter(self, key, default=None):
        return self.subParameter.get(key, default)

    def __repr__(self):
        return ("Logic(programID='%s', type=%s, currentPhaseIndex=%s, phases=%s, subParameter=%s)" %
                (self.programID, self.type, self.currentPhaseIndex, self.phases, self.subParameter))


def _readLogics(result):
    numLogics = result.readInt()
    logics = []
    for _ in range(numLogics):
        result.readCompound(5)
        programID = result.readTypedString()
        type = result.readTypedInt()
        currentPhaseIndex = result.readTypedInt()
        logic = Logic(programID, type, currentPhaseIndex)
        numPhases = result.readCompound()
        for __ in range(numPhases):
            result.readCompound(5)
            duration = result.readTypedDouble()
            state = result.readTypedString()
            minDur = result.readTypedDouble()
            maxDur = result.readTypedDouble()
            next = result.readTypedInt()
            logic.phases.append(Phase(duration, state, minDur, maxDur, next))
        numParams = result.readCompound()
        for __ in range(numParams):
            key, value = result.readTypedStringList()
            logic.subParameter[key] = value
        logics.append(logic)
    return logics


def _readLinks(result):
    result.readLength()
    numSignals = result.readInt()  # Length
    signals = []
    for _ in range(numSignals):
        # Type of Number of Controlled Links
        result.read("!B")
        # Number of Controlled Links
        nbControlledLinks = result.read("!i")[0]
        controlledLinks = []
        for __ in range(nbControlledLinks):
            result.read("!B")                       # Type of Link j
            link = result.readStringList()          # Link j
            controlledLinks.append(link)
        signals.append(controlledLinks)
    return signals


_RETURN_VALUE_FUNC = {tc.TL_RED_YELLOW_GREEN_STATE: Storage.readString,
                      tc.TL_COMPLETE_DEFINITION_RYG: _readLogics,
                      tc.TL_CONTROLLED_LANES: Storage.readStringList,
                      tc.TL_CONTROLLED_LINKS: _readLinks,
                      tc.TL_CURRENT_PROGRAM: Storage.readString,
                      tc.TL_CURRENT_PHASE: Storage.readInt,
                      tc.TL_NEXT_SWITCH: Storage.readDouble,
                      tc.TL_PHASE_DURATION: Storage.readDouble}


class TrafficLightDomain(Domain):

    Phase = Phase
    Logic = Logic

    def __init__(self, name="trafficlight", deprecatedFor=None):
        Domain.__init__(self, name, tc.CMD_GET_TL_VARIABLE, tc.CMD_SET_TL_VARIABLE,
                        tc.CMD_SUBSCRIBE_TL_VARIABLE, tc.RESPONSE_SUBSCRIBE_TL_VARIABLE,
                        tc.CMD_SUBSCRIBE_TL_CONTEXT, tc.RESPONSE_SUBSCRIBE_TL_CONTEXT,
                        _RETURN_VALUE_FUNC, deprecatedFor)

    def getRedYellowGreenState(self, tlsID):
        """getRedYellowGreenState(string) -> string

        Returns the named tl's state as a tuple of light definitions from
        rugGyYoO, for red, yed-yellow, green, yellow, off, where lower case letters mean that the stream
        has to decelerate.
        """
        return self._getUniversal(tc.TL_RED_YELLOW_GREEN_STATE, tlsID)

    def getCompleteRedYellowGreenDefinition(self, tlsID):
        """getCompleteRedYellowGreenDefinition(string) -> list(Logic)

        Returns a list of Logic objects.
        Each Logic encodes a traffic light program for the given tlsID.
        """
        return self._getUniversal(tc.TL_COMPLETE_DEFINITION_RYG, tlsID)

    def getControlledLanes(self, tlsID):
        """getControlledLanes(string) -> c

        Returns the list of lanes which are controlled by the named traffic light.
        """
        return self._getUniversal(tc.TL_CONTROLLED_LANES, tlsID)

    def getControlledLinks(self, tlsID):
        """getControlledLinks(string) -> list(list(list(string)))

        Returns the links controlled by the traffic light, sorted by the signal index and described by giving
        the incoming, outgoing, and via lane.
        """
        return self._getUniversal(tc.TL_CONTROLLED_LINKS, tlsID)

    def getProgram(self, tlsID):
        """getProgram(string) -> string

        Returns the id of the current program.
        """
        return self._getUniversal(tc.TL_CURRENT_PROGRAM, tlsID)

    def getPhase(self, tlsID):
        """getPhase(string) -> integer

        Returns the index of the current phase within the list of all phases of
        the current program.
        """
        return self._getUniversal(tc.TL_CURRENT_PHASE, tlsID)

    def getNextSwitch(self, tlsID):
        """getNextSwitch(string) -> double

        Returns the absolute simulation time at which the traffic light is
        schedule to switch to the next phase (in seconds).
        """
        return self._getUniversal(tc.TL_NEXT_SWITCH, tlsID)

    def getPhaseDuration(self, tlsID):
        """getPhaseDuration(string) -> double

        Returns the total duration of the current phase (in seconds). This value
        is not affected by the elapsed or remaining duration of the current phase.
        """
        return self._getUniversal(tc.TL_PHASE_DURATION, tlsID)

    def setRedYellowGreenState(self, tlsID, state):
        """setRedYellowGreenState(string, string) -> None

        Sets the named tl's state as a tuple of light definitions from
        rugGyYuoO, for red, red-yellow, green, yellow, off, where lower case letters mean that the stream has
        to decelerate.
        """
        self._connection._sendStringCmd(
            tc.CMD_SET_TL_VARIABLE, tc.TL_RED_YELLOW_GREEN_STATE, tlsID, state)

    def setLinkState(self, tlsID, tlsLinkIndex, state):
        """setLinkState(string, string, int, string) -> None
        Sets the state for the given tls and link index. The state must be one
        of rRgGyYoOu for red, red-yellow, green, yellow, off, where lower case letters mean that the stream has
        to decelerate.
        The link index is shown the gui when setting the appropriate junctino
        visualization optin.
        """
        fullState = list(self.getRedYellowGreenState(tlsID))
        if tlsLinkIndex >= len(fullState):
            raise TraCIException("Invalid tlsLinkIndex %s for tls '%s' with maximum index %s." % (
                tlsLinkIndex, tlsID, len(fullState) - 1))
        else:
            fullState[tlsLinkIndex] = state
            self.setRedYellowGreenState(tlsID, ''.join(fullState))

    def setPhase(self, tlsID, index):
        """setPhase(string, integer) -> None

        Switches to the phase with the given index in the list of all phases for
        the current program.
        """
        self._connection._sendIntCmd(
            tc.CMD_SET_TL_VARIABLE, tc.TL_PHASE_INDEX, tlsID, index)

    def setProgram(self, tlsID, programID):
        """setProgram(string, string) -> None

        Switches to the program with the given programID. The program must have
        been loaded earlier. The special value 'off' can always be used to
        switch off the traffic light.
        """
        self._connection._sendStringCmd(
            tc.CMD_SET_TL_VARIABLE, tc.TL_PROGRAM, tlsID, programID)

    def setPhaseDuration(self, tlsID, phaseDuration):
        """setPhaseDuration(string, double) -> None

        Set the remaining phase duration of the current phase in seconds.
        This value has no effect on subsquent repetitions of this phase.
        """
        self._connection._sendDoubleCmd(
            tc.CMD_SET_TL_VARIABLE, tc.TL_PHASE_DURATION, tlsID, phaseDuration)

    def setCompleteRedYellowGreenDefinition(self, tlsID, tls):
        """setCompleteRedYellowGreenDefinition(string, Logic) -> None

        Sets a new program for the given tlsID from a Logic object.
        See getCompleteRedYellowGreenDefinition.
        """
        length = 1 + 4 + 1 + 4 + \
            len(tls.programID) + 1 + 4 + 1 + 4 + 1 + 4  # tls parameter
        for p in tls.phases:
            length += 1 + 4 + 1 + 8 + 1 + 4 + len(p.state) + 1 + 8 + 1 + 8 + 1 + 4
        length += 1 + 4  # subparams
        for k, v in tls.subParameter.items():
            length += 1 + 4 + 4 + len(k) + 4 + len(v)
        self._connection._beginMessage(
            tc.CMD_SET_TL_VARIABLE, tc.TL_COMPLETE_PROGRAM_RYG, tlsID, length)
        self._connection._string += struct.pack("!Bi", tc.TYPE_COMPOUND, 5)
        self._connection._packString(tls.programID)
        self._connection._string += struct.pack("!Bi", tc.TYPE_INTEGER, tls.type)
        self._connection._string += struct.pack("!Bi", tc.TYPE_INTEGER, tls.currentPhaseIndex)
        self._connection._string += struct.pack("!Bi", tc.TYPE_COMPOUND, len(tls.phases))
        for p in tls.phases:
            self._connection._string += struct.pack("!BiBd", tc.TYPE_COMPOUND, 5, tc.TYPE_DOUBLE, p.duration)
            self._connection._packString(p.state)
            self._connection._string += struct.pack("!BdBdBi", tc.TYPE_DOUBLE, p.minDur, tc.TYPE_DOUBLE, p.maxDur,
                                                    tc.TYPE_INTEGER, p.next)
        # subparams
        self._connection._string += struct.pack("!Bi", tc.TYPE_COMPOUND, len(tls.subParameter))
        for par in tls.subParameter.items():
            self._connection._packStringList(par)
        self._connection._sendExact()


TrafficLightDomain()
TrafficLightDomain("trafficlights", "trafficlight")
