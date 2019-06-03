#!/usr/bin/env python
# Eclipse SUMO, Simulation of Urban MObility; see https://eclipse.org/sumo
# Copyright (C) 2008-2018 German Aerospace Center (DLR) and others.
# This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v20.html
# SPDX-License-Identifier: EPL-2.0

# @file    dailyBuildMSVC.py
# @author  Michael Behrisch
# @author  Jakob Erdmann
# @author  Laura Bieker
# @date    2008
# @version $Id$

"""
Does the nightly git pull on the windows server and the visual
studio build. The script is also used for the meso build.
Some paths especially for the names of the texttest output dirs are
hard coded into this script.
"""
from __future__ import absolute_import
from __future__ import print_function
import io
from datetime import date
import optparse
import os
import glob
import subprocess
import zipfile
import shutil
import sys

import status
import version
import wix

BINARIES = ("activitygen", "emissionsDrivingCycle", "emissionsMap",
            "dfrouter", "duarouter", "jtrrouter", "marouter",
            "netconvert", "netedit", "netgenerate",
            "od2trips", "polyconvert", "sumo", "sumo-gui",
            "TraCITestClient")


def repositoryUpdate(options, repoLogFile):
    if not options.update:
        return ""
    with open(repoLogFile, 'w') as log:
        cwd = os.getcwd()
        os.chdir(os.path.join(options.rootDir, "git"))
        subprocess.call(["git", "pull"], stdout=log, stderr=subprocess.STDOUT)
        gitrev = version.gitDescribe()
        os.chdir(cwd)
    return gitrev


def runTests(options, env, gitrev, withNetedit, debugSuffix=""):
    if not options.tests:
        return
    prefix = env["FILEPREFIX"] + debugSuffix
    env["SUMO_BATCH_RESULT"] = os.path.join(
        options.rootDir, prefix + "batch_result")
    env["SUMO_REPORT"] = os.path.join(options.remoteDir, prefix + "report")
    testLog = os.path.join(options.remoteDir, prefix + "test.log")
    env["TEXTTEST_TMP"] = os.path.join(
        options.rootDir, prefix + "texttesttmp")
    env["TEXTTEST_HOME"] = os.path.join(options.rootDir, options.testsDir)
    shutil.rmtree(env["TEXTTEST_TMP"], True)
    if not os.path.exists(env["SUMO_REPORT"]):
        os.makedirs(env["SUMO_REPORT"])
    for name in BINARIES:
        image = name + debugSuffix + ".exe"
        subprocess.call(["taskkill", "/f", "/im", image])
        binary = os.path.join(options.rootDir, options.binDir, image)
        if name == "sumo-gui":
            if os.path.exists(binary):
                env["GUISIM_BINARY"] = binary
        elif os.path.exists(binary):
            env[name.upper() + "_BINARY"] = binary
    log = open(testLog, 'w')
    # provide more information than just the date:
    fullOpt = ["-b", prefix, "-name", "%sr%s" %
               (date.today().strftime("%d%b%y"), gitrev)]
    ttBin = "texttestc.py"
    if options.suffix == "extra":
        runInternalTests.runInternal(
            debugSuffix, fullOpt, log, True, True, debugSuffix == "")
    else:
        subprocess.call([ttBin] + fullOpt, env=env,
                        stdout=log, stderr=subprocess.STDOUT, shell=True)
        if withNetedit:
            subprocess.call([ttBin, "-a", "netedit.daily"] + fullOpt, env=env,
                            stdout=log, stderr=subprocess.STDOUT, shell=True)
        subprocess.call([ttBin, "-a", "sumo.gui"] + fullOpt, env=env,
                        stdout=log, stderr=subprocess.STDOUT, shell=True)
    subprocess.call([ttBin, "-b", env["FILEPREFIX"], "-coll"], env=env,
                    stdout=log, stderr=subprocess.STDOUT, shell=True)
    for name in BINARIES:
        subprocess.call(["taskkill", "/f", "/im", name + debugSuffix + ".exe"])
    log.close()


def generateCMake(generator, log, checkOptionalLibs, python):
    buildDir = os.path.join(env["SUMO_HOME"], "build", "cmake-build-" + generator.replace(" ", "-"))
    cmakeOpt = ["-DCOMPILE_DEFINITIONS=MSVC_TEST_SERVER", "-DCHECK_OPTIONAL_LIBS=%s" % checkOptionalLibs]
    if python:
        cmakeOpt += ["-DPYTHON_EXECUTABLE=%s" % python]
    # Create directory or clear it if already exists
    if os.path.exists(buildDir):
        print("Cleaning directory of", generator, file=log)
        shutil.rmtree(buildDir)
    os.makedirs(buildDir)
    print("Creating solution for", generator, file=log)
    subprocess.call(["cmake", "../..", "-G", generator] + cmakeOpt, cwd=buildDir, stdout=log, stderr=subprocess.STDOUT)
    return buildDir


optParser = optparse.OptionParser()
optParser.add_option("-r", "--root-dir", dest="rootDir",
                     default=r"D:\Sumo", help="root for git and log output")
optParser.add_option("-s", "--suffix", default="", help="suffix to the fileprefix")
optParser.add_option("-b", "--bin-dir", dest="binDir", default=r"git\bin",
                     help="directory containg the binaries, relative to the root dir")
optParser.add_option("-t", "--tests-dir", dest="testsDir", default=r"git\tests",
                     help="directory containg the tests, relative to the root dir")
optParser.add_option("-m", "--remote-dir", dest="remoteDir", default="S:\\daily",
                     help="directory to move the results to")
optParser.add_option("-d", "--dll-dirs", dest="dllDirs",
                     default=r"Win32:bin,x64:bin64",
                     help="path to dependency dlls for the relevant platforms")
optParser.add_option("-u", "--no-update", dest="update", action="store_false",
                     default=True, help="skip repository update")
optParser.add_option("-n", "--no-tests", dest="tests", action="store_false",
                     default=True, help="skip tests")
optParser.add_option("-e", "--no-extended-tests", dest="extended_tests", action="store_false",
                     default=True, help="skip netedit tests and tests for the debug build")
optParser.add_option("-p", "--python", help="path to python interpreter to use")
(options, args) = optParser.parse_args()

sys.path.append(os.path.join(options.rootDir, options.testsDir))
import runInternalTests  # noqa

env = os.environ
if "SUMO_HOME" not in env:
    env["SUMO_HOME"] = os.path.dirname(
        os.path.dirname(os.path.dirname(__file__)))
env["PYTHON"] = "python"
env["SMTP_SERVER"] = "smtprelay.dlr.de"
msvcVersion = "msvc12"
gitrev = repositoryUpdate(options, os.path.join(
    options.remoteDir, msvcVersion + options.suffix + "Update.log"))

maxTime = 0
sumoAllZip = None
for fname in glob.glob(os.path.join(options.remoteDir, "sumo-all-*.zip")):
    if os.path.getmtime(fname) > maxTime:
        maxTime = os.path.getmtime(fname)
        sumoAllZip = fname
platformDlls = [entry.split(":") for entry in options.dllDirs.split(",")]
for platform, dllDir in platformDlls:
    env["FILEPREFIX"] = msvcVersion + options.suffix + platform
    prefix = os.path.join(options.remoteDir, env["FILEPREFIX"])
    makeLog = prefix + "Release.log"
    makeAllLog = prefix + "Debug.log"
    statusLog = prefix + "status.log"
    binDir = "sumo-git/bin/"

    toClean = [makeLog, makeAllLog]
    for ext in ("*.exe", "*.ilk", "*.pdb", "*.py", "*.pyd"):
        toClean += glob.glob(os.path.join(options.rootDir, options.binDir, ext))
    for f in toClean:
        try:
            os.remove(f)
        except WindowsError:
            pass
    with open(makeLog, 'a') as log:
        generator = "Visual Studio 12 2013"
        if platform == "x64":
            generator += " Win64"
        buildDir = generateCMake(generator, log, options.suffix == "extra", options.python)
        subprocess.call(["cmake", "--build", ".", "--config", "Release"],
                        cwd=buildDir, stdout=log, stderr=subprocess.STDOUT)
    envSuffix = ""
    if platform == "x64":
        envSuffix = "_64"
    # we need to use io.open here due to http://bugs.python.org/issue16273
    log = io.open(makeLog, 'a')
    if sumoAllZip:
        try:
            binaryZip = sumoAllZip.replace("-all-", "-%s-" % env["FILEPREFIX"])
            zipf = zipfile.ZipFile(binaryZip, 'w', zipfile.ZIP_DEFLATED)
            srcZip = zipfile.ZipFile(sumoAllZip)
            write = False
            for f in srcZip.namelist():
                if f.count('/') == 1:
                    write = f.endswith(".md") or os.path.basename(f) in ["AUTHORS", "ChangeLog", "LICENSE"]
                if f.endswith('/') and f.count('/') == 2:
                    write = (f.endswith('/bin/') or
                             f.endswith('/tools/') or f.endswith('/data/') or f.endswith('/docs/'))
                    if f.endswith('/bin/'):
                        binDir = f
                elif f.endswith('/') and '/docs/' in f and f.count('/') == 3:
                    write = not f.endswith('/doxygen/')
                elif write:
                    zipf.writestr(f, srcZip.read(f))
            srcZip.close()
            dllPath = os.path.join(options.rootDir, dllDir)
            for f in glob.glob(os.path.join(dllPath, "*.dll")) + glob.glob(os.path.join(dllPath, "*", "*.dll")):
                zipf.write(f, os.path.join(binDir, f[len(dllPath) + 1:]))
            for ext in ("*.exe", "*.bat", "*.py", "*.pyd", "*.dll", "*.jar"):
                for f in glob.glob(os.path.join(options.rootDir, options.binDir, ext)):
                    nameInZip = os.path.join(binDir, os.path.basename(f))
                    if nameInZip not in srcZip.namelist():
                        zipf.write(f, nameInZip)
            zipf.close()
            if options.suffix == "":
                # installers only for the vanilla build
                wix.buildMSI(binaryZip, binaryZip.replace(".zip", ".msi"), log=log)
        except IOError as ziperr:
            (errno, strerror) = ziperr.args
            print("Warning: Could not zip to %s!" % binaryZip, file=log)
            print("I/O error(%s): %s" % (errno, strerror), file=log)
    try:
        setup = os.path.join(env["SUMO_HOME"], 'tools', 'game', 'setup.py')
        subprocess.call(['python', setup, binaryZip], stdout=log, stderr=subprocess.STDOUT)
    except Exception as e:
        print("Warning: Could not create nightly sumo-game.zip! (%s)" % e, file=log)
    log.close()
    with open(makeAllLog, 'a') as log:
        subprocess.call(["cmake", "--build", ".", "--config", "Debug"],
                        cwd=buildDir, stdout=log, stderr=subprocess.STDOUT)
    if sumoAllZip:
        try:
            debugZip = sumoAllZip.replace("-all-", "Debug-%s-" % env["FILEPREFIX"])
            zipf = zipfile.ZipFile(debugZip, 'w', zipfile.ZIP_DEFLATED)
            debugDllPath = os.path.join(options.rootDir, "..", "debugDll")
            if platform == "x64":
                debugDllPath += "64"
            for dllPath in (os.path.join(options.rootDir, dllDir), debugDllPath):
                for f in glob.glob(os.path.join(dllPath, "*.dll")) + glob.glob(os.path.join(dllPath, "*", "*.dll")):
                    zipf.write(f, os.path.join(binDir, f[len(dllPath) + 1:]))
            for f in (glob.glob(os.path.join(options.rootDir, options.binDir, "*D.exe")) +
                      glob.glob(os.path.join(options.rootDir, options.binDir, "*D.pdb"))):
                zipf.write(f, os.path.join(binDir, os.path.basename(f)))
            zipf.close()
        except IOError as ziperr:
            (errno, strerror) = ziperr.args
            print("Warning: Could not zip to %s!" % binaryZip, file=log)
            print("I/O error(%s): %s" % (errno, strerror), file=log)
    runTests(options, env, gitrev, options.extended_tests and platform == "x64")
    with open(statusLog, 'w') as log:
        status.printStatus(makeLog, makeAllLog, env["SMTP_SERVER"], log)
if options.extended_tests:
    runTests(options, env, gitrev, True, "D")
    with open(prefix + "Dstatus.log", 'w') as log:
        status.printStatus(makeAllLog, makeAllLog, env["SMTP_SERVER"], log)
