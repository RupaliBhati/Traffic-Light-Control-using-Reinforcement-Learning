#!/usr/bin/env python
# Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
# Copyright (C) 2010-2018 German Aerospace Center (DLR) and others.
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
# SPDX-License-Identifier: EPL-2.0

# @file    poi_at_stops.py
# @author  Jakob Erdmann
# @date    2018-08-31
# @version $Id$

"""
Generates a PoI-file containing a PoI for each bus stop in the given net.
"""
from __future__ import absolute_import
from __future__ import print_function

import os
import sys
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
import sumolib.net  # noqa
from sumolib.xml import parse


if len(sys.argv) < 2:
    print("Usage: " + sys.argv[0] + " <NET> <STOPS>", file=sys.stderr)
    sys.exit()

print("Reading net...")
net = sumolib.net.readNet(sys.argv[1])
stops = sys.argv[2]


print("Writing output...")
with open('pois.add.xml', 'w') as f:
    f.write('<?xml version="1.0"?>\n')
    f.write('<additional>\n')
    for stop in parse(stops, 'busStop'):
        lane = net.getLane(stop.lane)
        pos = (float(stop.startPos) + float(stop.endPos)) / 2
        xypos = sumolib.geomhelper.positionAtShapeOffset(lane.getShape(), pos)
        lon, lat = net.convertXY2LonLat(xypos[0], xypos[1])
        f.write('    <poi id="%s" type="%s" color="1,0,0" layer="100" lon="%s" lat="%s"/>\n' % (
            stop.id, stop.name, lon, lat))
    f.write('</additional>\n')
