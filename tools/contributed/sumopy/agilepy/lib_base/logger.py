# Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
# Copyright (C) 2016-2018 German Aerospace Center (DLR) and others.
# SUMOPy module
# Copyright (C) 2012-2017 University of Bologna - DICAM
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
# SPDX-License-Identifier: EPL-2.0

# @file    logger.py
# @author  Joerg Schweizer
# @date
# @version $Id$

import types
from time import gmtime, strftime


class Logger:
    def __init__(self, filepath=None, is_stdout=True,
                 timeformat="%a, %d %b %Y %H:%M:%S"):
        self._filepath = filepath
        self._logfile = None
        self._callbacks = {}
        self._is_stdout = is_stdout
        self._timeformat = timeformat

    def start(self, text="Start logging."):
        # print 'Logger.start:',self._filepath,self._filepath is not None
        if self._filepath is not None:
            ttext = strftime(self._timeformat, gmtime())+' '+text
            self._logfile = open(self._filepath, 'w')
            self._logfile.write(ttext+'\n')
        if self._is_stdout:
            print text

    def add_callback(self, function, key='message'):
        self._callbacks[key] = function

    def get_clallbackfunc(self, key):

        return self._callbacks.get(key, None)

    def del_callback(self, key):
        del self._callbacks[key]

    def progress(self, percent):
        pass

    def w(self, data, key='message', **kwargs):
        # print 'Logger.w:',self._logfile is not None,self._is_stdout,data

        if key == 'progress':
            text = '%d %% completed.' % data
        else:
            text = str(data)

        if self._logfile is not None:
            self._logfile.write(strftime(self._timeformat, gmtime())+' '+text+'\n')

        elif self._callbacks.has_key(key):
            kwargs['key'] = key
            self._callbacks[key](data, **kwargs)
        # elif type(data)==types.StringType:
        #    print data
        if self._is_stdout:
            print text

    def stop(self, text="End logging."):

        if self._logfile is not None:
            ttext = strftime(self._timeformat, gmtime())+' '+text
            self._logfile.write(ttext+'\n')
            self._logfile.close()
            self._logfile = None

        if self._is_stdout:
            print text
